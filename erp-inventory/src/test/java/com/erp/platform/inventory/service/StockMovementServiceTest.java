package com.erp.platform.inventory.service;

import com.erp.platform.catalog.entity.Product;
import com.erp.platform.catalog.repository.ProductRepository;
import com.erp.platform.core.exception.ConflictException;
import com.erp.platform.core.security.JwtUserPrincipal;
import com.erp.platform.iam.entity.LocalUnit;
import com.erp.platform.iam.repository.LocalUnitRepository;
import com.erp.platform.inventory.dto.request.CreateMovementRequest;
import com.erp.platform.inventory.dto.request.StockTransferRequest;
import com.erp.platform.inventory.dto.response.StockMovementResponse;
import com.erp.platform.inventory.entity.StockEntry;
import com.erp.platform.inventory.entity.StockMovement;
import com.erp.platform.inventory.enums.MovementType;
import com.erp.platform.inventory.repository.StockEntryRepository;
import com.erp.platform.inventory.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMovementServiceTest {

    @Mock
    private StockMovementRepository movementRepository;

    @Mock
    private StockEntryRepository entryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private LocalUnitRepository localUnitRepository;

    @Mock
    private StockAlertService stockAlertService;

    @InjectMocks
    private StockMovementService movementService;

    private UUID productId;
    private UUID sourceLocalId;
    private UUID targetLocalId;
    private Product product;
    private LocalUnit sourceLocal;
    private LocalUnit targetLocal;
    private JwtUserPrincipal principal;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        sourceLocalId = UUID.randomUUID();
        targetLocalId = UUID.randomUUID();

        product = new Product();
        product.setId(productId);
        product.setRef("PRD-001");
        product.setName("Test Product");
        product.setTrackStock(true);
        product.setPurchasePriceHT(new BigDecimal("10.0000"));
        product.setMinStockLevel(5);

        sourceLocal = new LocalUnit();
        sourceLocal.setId(sourceLocalId);
        sourceLocal.setName("Dépôt Principal");

        targetLocal = new LocalUnit();
        targetLocal.setId(targetLocalId);
        targetLocal.setName("Dépôt Secondaire");

        principal = JwtUserPrincipal.builder()
                .userId(UUID.randomUUID().toString())
                .email("user@example.com")
                .role("ROLE_STOCK_MANAGER")
                .build();
    }

    @Test
    void testCreateMovement_InOnEmptyStock_createsEntryWithQtyAndCost() {
        CreateMovementRequest req = CreateMovementRequest.builder()
                .productId(productId)
                .localId(sourceLocalId)
                .type(MovementType.IN)
                .quantity(new BigDecimal("100.000"))
                .unitCost(new BigDecimal("12.5000"))
                .build();

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(product));
        when(localUnitRepository.findByIdAndIsDeletedFalse(sourceLocalId)).thenReturn(Optional.of(sourceLocal));
        when(entryRepository.findByProductIdAndLocalIdAndIsDeletedFalse(productId, sourceLocalId)).thenReturn(Optional.empty());

        when(entryRepository.save(any(StockEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(inv -> {
            StockMovement m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        StockMovementResponse resp = movementService.createMovement(req, principal);

        assertNotNull(resp);
        assertEquals(new BigDecimal("100.000"), resp.getBalanceAfter());

        ArgumentCaptor<StockEntry> entryCaptor = ArgumentCaptor.forClass(StockEntry.class);
        verify(entryRepository).save(entryCaptor.capture());
        StockEntry savedEntry = entryCaptor.getValue();
        assertEquals(new BigDecimal("100.000"), savedEntry.getQuantity());
        assertEquals(new BigDecimal("12.5000"), savedEntry.getAvgUnitCost());
    }

    @Test
    void testCreateMovement_InWithCMPRecalculation() {
        StockEntry existingEntry = StockEntry.builder()
                .productId(productId)
                .localId(sourceLocalId)
                .quantity(new BigDecimal("100.000"))
                .avgUnitCost(new BigDecimal("10.0000"))
                .build();

        CreateMovementRequest req = CreateMovementRequest.builder()
                .productId(productId)
                .localId(sourceLocalId)
                .type(MovementType.IN)
                .quantity(new BigDecimal("100.000"))
                .unitCost(new BigDecimal("20.0000"))
                .build();

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(product));
        when(localUnitRepository.findByIdAndIsDeletedFalse(sourceLocalId)).thenReturn(Optional.of(sourceLocal));
        when(entryRepository.findByProductIdAndLocalIdAndIsDeletedFalse(productId, sourceLocalId)).thenReturn(Optional.of(existingEntry));

        when(entryRepository.save(any(StockEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(inv -> {
            StockMovement m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        movementService.createMovement(req, principal);

        ArgumentCaptor<StockEntry> entryCaptor = ArgumentCaptor.forClass(StockEntry.class);
        verify(entryRepository).save(entryCaptor.capture());
        StockEntry savedEntry = entryCaptor.getValue();

        // (100 * 10 + 100 * 20) / 200 = 3000 / 200 = 15.0000
        assertEquals(new BigDecimal("200.000"), savedEntry.getQuantity());
        assertEquals(new BigDecimal("15.0000"), savedEntry.getAvgUnitCost());
    }

    @Test
    void testCreateMovement_TrackStockFalse_throwsConflictException() {
        product.setTrackStock(false);

        CreateMovementRequest req = CreateMovementRequest.builder()
                .productId(productId)
                .localId(sourceLocalId)
                .type(MovementType.IN)
                .quantity(new BigDecimal("10.000"))
                .build();

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(product));

        assertThrows(ConflictException.class, () -> movementService.createMovement(req, principal));
        verify(entryRepository, never()).save(any());
        verify(movementRepository, never()).save(any());
    }

    @Test
    void testTransfer_createsOutAndInMovements() {
        StockEntry sourceEntry = StockEntry.builder()
                .productId(productId)
                .localId(sourceLocalId)
                .quantity(new BigDecimal("50.000"))
                .avgUnitCost(new BigDecimal("10.0000"))
                .build();

        StockTransferRequest req = StockTransferRequest.builder()
                .productId(productId)
                .sourceLocalId(sourceLocalId)
                .targetLocalId(targetLocalId)
                .quantity(new BigDecimal("20.000"))
                .build();

        when(productRepository.findByIdAndIsDeletedFalse(productId)).thenReturn(Optional.of(product));
        when(localUnitRepository.findByIdAndIsDeletedFalse(sourceLocalId)).thenReturn(Optional.of(sourceLocal));
        when(localUnitRepository.findByIdAndIsDeletedFalse(targetLocalId)).thenReturn(Optional.of(targetLocal));

        when(entryRepository.findByProductIdAndLocalIdAndIsDeletedFalse(productId, sourceLocalId))
                .thenReturn(Optional.of(sourceEntry));
        when(entryRepository.findByProductIdAndLocalIdAndIsDeletedFalse(productId, targetLocalId))
                .thenReturn(Optional.empty());

        when(entryRepository.save(any(StockEntry.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movementRepository.save(any(StockMovement.class))).thenAnswer(inv -> {
            StockMovement m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        List<StockMovementResponse> resps = movementService.transfer(req, principal);

        assertEquals(2, resps.size());
        assertEquals(MovementType.TRANSFER_OUT, resps.get(0).getType());
        assertEquals(MovementType.TRANSFER_IN, resps.get(1).getType());

        verify(entryRepository, times(2)).save(any(StockEntry.class));
        verify(movementRepository, times(2)).save(any(StockMovement.class));
    }
}
