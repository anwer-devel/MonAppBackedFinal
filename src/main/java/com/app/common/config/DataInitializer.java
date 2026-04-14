package com.app.common.config;

import com.app.auth.entity.User;
import com.app.auth.repository.UserRepository;
import com.app.partner.entity.Partner;
import com.app.partner.repository.PartnerRepository;
import com.app.zone.entity.Zone;
import com.app.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Initialize test data for development and testing.
 * Only active in 'dev', 'test', or 'local' profiles.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "test", "local"})
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PartnerRepository partnerRepository;
    private final ZoneRepository zoneRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🚀 Initializing test data...");

        // Check if data already exists
        if (userRepository.count() > 0) {
            log.info("✅ Test data already exists, skipping initialization");
            return;
        }

        try {
            initUsers();
            initZoneAndPartner();
            log.info("✅ Test data initialized successfully");
        } catch (Exception e) {
            log.error("❌ Failed to initialize test data: {}", e.getMessage(), e);
        }
    }

    private void initUsers() {
        log.info("Creating test users...");

        // Admin user - ID auto-generated, isActive hérité de BaseEntity
        User admin = User.builder()
                .email("admin@quizapp.com")
                .password(passwordEncoder.encode("Admin123!"))
                .role(User.UserRole.ADMIN)
                .username("SuperAdmin")
                .emailVerified(true)
                .build();
        admin.setIsActive(true);
        userRepository.save(admin);
        log.info("  ✅ Admin created: admin@quizapp.com (ID: {})", admin.getId());

        // Partner user - ID auto-generated
        User partner = User.builder()
                .email("partner@cafe-lune.com")
                .password(passwordEncoder.encode("Partner123!"))
                .role(User.UserRole.PARTNER_OWNER)
                .username("CafeLune")
                .emailVerified(true)
                .build();
        partner.setIsActive(true);
        userRepository.save(partner);
        log.info("  ✅ Partner created: partner@cafe-lune.com (ID: {})", partner.getId());

        // Regular users - IDs auto-generated
        User alice = User.builder()
                .email("alice@example.com")
                .password(passwordEncoder.encode("Alice123!"))
                .role(User.UserRole.USER)
                .username("Alice")
                .emailVerified(true)
                .build();
        alice.setIsActive(true);
        userRepository.save(alice);
        log.info("  ✅ User created: alice@example.com (ID: {})", alice.getId());

        User bob = User.builder()
                .email("bob@example.com")
                .password(passwordEncoder.encode("Bob123!"))
                .role(User.UserRole.USER)
                .username("Bob")
                .emailVerified(true)
                .build();
        bob.setIsActive(true);
        userRepository.save(bob);
        log.info("  ✅ User created: bob@example.com (ID: {})", bob.getId());

        User charlie = User.builder()
                .email("charlie@example.com")
                .password(passwordEncoder.encode("Charlie123!"))
                .role(User.UserRole.USER)
                .username("Charlie")
                .emailVerified(true)
                .build();
        charlie.setIsActive(true);
        userRepository.save(charlie);
        log.info("  ✅ User created: charlie@example.com (ID: {})", charlie.getId());
    }

    private void initZoneAndPartner() {
        log.info("Creating zone and partner...");

        // Find partner user
        User partnerUser = userRepository.findByEmail("partner@cafe-lune.com")
                .orElseThrow(() -> new RuntimeException("Partner user not found"));

        // Create a zone - ID auto-generated (Zone n'a pas de city/country/isActive)
        Zone zone = Zone.builder()
                .name("Paris Centre")
                .description("Zone centrale de Paris")
                .latitude(48.8566)
                .longitude(2.3522)
                .build();
        zone.setIsActive(true);
        zoneRepository.save(zone);
        log.info("  ✅ Zone created: Paris Centre (ID: {})", zone.getId());

        // Create partner establishment - ID auto-generated
        Partner partner = Partner.builder()
                .name("Café de la Lune")
                .description("Un café cozy pour les amateurs de quiz")
                .type(Partner.PartnerType.CAFE)
                .owner(partnerUser)
                .zone(zone)
                .isVerified(true)
                .phone("+33 1 23 45 67 89")
                .isOpen(true)
                .build();
        partner.setIsActive(true);
        partnerRepository.save(partner);
        log.info("  ✅ Partner created: Café de la Lune (ID: {})", partner.getId());
    }
}
