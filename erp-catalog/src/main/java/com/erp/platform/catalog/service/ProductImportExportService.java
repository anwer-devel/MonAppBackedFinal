package com.erp.platform.catalog.service;

import com.erp.platform.catalog.dto.response.ImportResultResponse;
import com.erp.platform.catalog.entity.Category;
import com.erp.platform.catalog.entity.Product;
import com.erp.platform.catalog.entity.Unit;
import com.erp.platform.catalog.repository.CategoryRepository;
import com.erp.platform.catalog.repository.ProductRepository;
import com.erp.platform.catalog.repository.UnitRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImportExportService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;

    @Transactional
    public ImportResultResponse importCsv(MultipartFile file) {
        List<ImportResultResponse.ImportError> errors = new ArrayList<>();
        int imported = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber > 5000) {
                    errors.add(new ImportResultResponse.ImportError(lineNumber, "", "Limite de 5000 lignes dépassée"));
                    skipped++;
                    break;
                }
                if (lineNumber == 1 && (line.toLowerCase().contains("ref") || line.toLowerCase().contains("name"))) {
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length < 4) {
                    errors.add(new ImportResultResponse.ImportError(lineNumber, parts.length > 0 ? parts[0] : "", "Colonnes insuffisantes (attendu: ref;name;purchasePriceHT;salePriceHT;taxRate;categoryCode;unitCode)"));
                    skipped++;
                    continue;
                }

                String ref = parts[0].trim();
                String name = parts[1].trim();
                String purchasePriceStr = parts[2].trim();
                String salePriceStr = parts[3].trim();
                String taxRateStr = parts.length > 4 ? parts[4].trim() : "19.0";
                String categoryCode = parts.length > 5 ? parts[5].trim() : null;
                String unitCode = parts.length > 6 ? parts[6].trim() : null;

                if (ref.isEmpty() || name.isEmpty()) {
                    errors.add(new ImportResultResponse.ImportError(lineNumber, ref, "Ref et Name sont obligatoires"));
                    skipped++;
                    continue;
                }

                if (productRepository.findByRefIgnoreCaseAndIsDeletedFalse(ref).isPresent()) {
                    errors.add(new ImportResultResponse.ImportError(lineNumber, ref, "Référence déjà existante"));
                    skipped++;
                    continue;
                }

                BigDecimal purchasePrice;
                BigDecimal salePrice;
                BigDecimal taxRate;
                try {
                    purchasePrice = new BigDecimal(purchasePriceStr.replace(",", "."));
                    salePrice = new BigDecimal(salePriceStr.replace(",", "."));
                    taxRate = !taxRateStr.isEmpty() ? new BigDecimal(taxRateStr.replace(",", ".")) : new BigDecimal("19.0");
                } catch (NumberFormatException ex) {
                    errors.add(new ImportResultResponse.ImportError(lineNumber, ref, "Format numérique invalide pour les prix/taxes"));
                    skipped++;
                    continue;
                }

                Category category = null;
                if (categoryCode != null && !categoryCode.isEmpty()) {
                    Optional<Category> catOpt = categoryRepository.findByCodeIgnoreCaseAndIsDeletedFalse(categoryCode);
                    if (catOpt.isPresent()) category = catOpt.get();
                }

                Unit unit = null;
                if (unitCode != null && !unitCode.isEmpty()) {
                    Optional<Unit> unitOpt = unitRepository.findByCodeIgnoreCaseAndIsDeletedFalse(unitCode);
                    if (unitOpt.isPresent()) unit = unitOpt.get();
                }

                Product product = Product.builder()
                        .ref(ref.toUpperCase())
                        .name(name)
                        .purchasePriceHT(purchasePrice)
                        .salePriceHT(salePrice)
                        .taxRate(taxRate)
                        .category(category)
                        .unit(unit)
                        .isActive(true)
                        .trackStock(true)
                        .build();

                productRepository.save(product);
                imported++;
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'import CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur de lecture du fichier CSV: " + e.getMessage());
        }

        return ImportResultResponse.builder()
                .imported(imported)
                .skipped(skipped)
                .errors(errors)
                .build();
    }

    @Transactional(readOnly = true)
    public void exportCsv(HttpServletResponse response) {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"products_export.csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ref;name;purchasePriceHT;salePriceHT;taxRate;categoryCode;unitCode");
            List<Product> products = productRepository.findAll();
            for (Product p : products) {
                if (p.isDeleted()) continue;
                String catCode = p.getCategory() != null ? p.getCategory().getCode() : "";
                String unitCode = p.getUnit() != null ? p.getUnit().getCode() : "";
                writer.printf("%s;%s;%s;%s;%s;%s;%s%n",
                        p.getRef(),
                        p.getName(),
                        p.getPurchasePriceHT() != null ? p.getPurchasePriceHT() : "0",
                        p.getSalePriceHT() != null ? p.getSalePriceHT() : "0",
                        p.getTaxRate() != null ? p.getTaxRate() : "19.0",
                        catCode,
                        unitCode);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'export CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'export CSV: " + e.getMessage());
        }
    }
}
