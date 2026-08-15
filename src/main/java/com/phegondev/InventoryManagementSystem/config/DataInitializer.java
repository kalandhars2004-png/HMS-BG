package com.phegondev.InventoryManagementSystem.config;

import com.phegondev.InventoryManagementSystem.category.Category;
import com.phegondev.InventoryManagementSystem.category.CategoryRepository;
import com.phegondev.InventoryManagementSystem.enums.UserRole;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.rack.Rack;
import com.phegondev.InventoryManagementSystem.rack.RackRepository;
import com.phegondev.InventoryManagementSystem.user.User;
import com.phegondev.InventoryManagementSystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Never seed demo data — or a default admin/admin123 account — into production.
@Profile("!prod")
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final RackRepository rackRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .name("Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .phoneNumber("+91-9876543210")
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Created admin user: admin@example.com / admin123");
        }

        seedCategories();
        seedProducts();
        seedRacks();
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) return;

        List<SeedCategory> categories = List.of(
            new SeedCategory("General Medicine", "\uD83D\uDC8A", "#0F9291", 1),
            new SeedCategory("Analgesics & Pain Relief", "\uD83D\uDC8A", "#E67E22", 2),
            new SeedCategory("Antibiotics", "\uD83D\uDD2C", "#8E44AD", 3),
            new SeedCategory("Antifungal Medicines", "\uD83D\uDD2C", "#C0392B", 4),
            new SeedCategory("Antiviral Medicines", "\uD83D\uDD2C", "#2980B9", 5),
            new SeedCategory("Antimalarial Medicines", "\uD83D\uDD2C", "#D35400", 6),
            new SeedCategory("Antitubercular Medicines", "\uD83D\uDD2C", "#27AE60", 7),
            new SeedCategory("Antiparasitic Medicines", "\uD83D\uDD2C", "#F39C12", 8),
            new SeedCategory("Anthelmintics", "\uD83D\uDD2C", "#1ABC9C", 9),
            new SeedCategory("Anti-inflammatory Medicines", "\uD83D\uDC8A", "#E74C3C", 10),
            new SeedCategory("Antipyretics", "\uD83C\uDF21", "#3498DB", 11),
            new SeedCategory("Antihistamines", "\uD83D\uDC8A", "#9B59B6", 12),
            new SeedCategory("Antiallergic Medicines", "\uD83D\uDC8A", "#2ECC71", 13),
            new SeedCategory("Antacids", "\uD83C\uDF7D", "#E67E22", 14),
            new SeedCategory("Gastrointestinal Medicines", "\uD83C\uDF7D", "#1ABC9C", 15),
            new SeedCategory("Laxatives", "\uD83C\uDF7D", "#16A085", 16),
            new SeedCategory("Digestive Enzymes", "\uD83C\uDF7D", "#F39C12", 17),
            new SeedCategory("Antiemetics", "\uD83C\uDF7D", "#D35400", 18),
            new SeedCategory("Antidiarrheal Medicines", "\uD83C\uDF7D", "#C0392B", 19),
            new SeedCategory("Probiotics", "\uD83E\uDDC0", "#27AE60", 20),
            new SeedCategory("Diabetes Medicines", "\uD83D\uDC8A", "#8E44AD", 21),
            new SeedCategory("Insulin Products", "\uD83D\uDC8A", "#2980B9", 22),
            new SeedCategory("Cardiovascular Medicines", "\u2764\uFE0F", "#E74C3C", 23),
            new SeedCategory("Hypertension Medicines", "\u2764\uFE0F", "#C0392B", 24),
            new SeedCategory("Heart Failure Medicines", "\u2764\uFE0F", "#D35400", 25),
            new SeedCategory("Antiarrhythmics", "\u2764\uFE0F", "#E67E22", 26),
            new SeedCategory("Blood Thinners", "\uD83E\uDE78", "#3498DB", 27),
            new SeedCategory("Anticoagulants", "\uD83E\uDE78", "#2980B9", 28),
            new SeedCategory("Antiplatelets", "\uD83E\uDE78", "#1ABC9C", 29),
            new SeedCategory("Cholesterol Medicines", "\u2764\uFE0F", "#9B59B6", 30),
            new SeedCategory("Respiratory Medicines", "\uD83D\uDE41", "#0F9291", 31),
            new SeedCategory("Asthma Medicines", "\uD83D\uDE41", "#16A085", 32),
            new SeedCategory("COPD Medicines", "\uD83D\uDE41", "#27AE60", 33),
            new SeedCategory("Cough & Cold Medicines", "\uD83D\uDE41", "#F39C12", 34),
            new SeedCategory("Bronchodilators", "\uD83D\uDE41", "#E67E22", 35),
            new SeedCategory("Expectorants", "\uD83D\uDE41", "#D35400", 36),
            new SeedCategory("Neurology Medicines", "\uD83E\uDDE0", "#8E44AD", 37),
            new SeedCategory("Epilepsy Medicines", "\uD83E\uDDE0", "#C0392B", 38),
            new SeedCategory("Parkinson Medicines", "\uD83E\uDDE0", "#2980B9", 39),
            new SeedCategory("Migraine Medicines", "\uD83E\uDDE0", "#E74C3C", 40),
            new SeedCategory("Psychiatry Medicines", "\uD83E\uDDEC", "#9B59B6", 41),
            new SeedCategory("Antidepressants", "\uD83E\uDDEC", "#3498DB", 42),
            new SeedCategory("Antipsychotics", "\uD83E\uDDEC", "#27AE60", 43),
            new SeedCategory("Anxiety Medicines", "\uD83E\uDDEC", "#F39C12", 44),
            new SeedCategory("Sleeping Disorders", "\uD83D\uDE34", "#1ABC9C", 45),
            new SeedCategory("Dermatology Medicines", "\uD83E\uDDCD", "#E67E22", 46),
            new SeedCategory("Skin Care Medicines", "\uD83E\uDDCD", "#D35400", 47),
            new SeedCategory("Acne Treatment", "\uD83E\uDDCD", "#C0392B", 48),
            new SeedCategory("Eczema Treatment", "\uD83E\uDDCD", "#8E44AD", 49),
            new SeedCategory("Psoriasis Medicines", "\uD83E\uDDCD", "#2980B9", 50),
            new SeedCategory("Antifungal Skin Products", "\uD83E\uDDCD", "#16A085", 51),
            new SeedCategory("Eye Care Medicines", "\uD83D\uDC41", "#0F9291", 52),
            new SeedCategory("Eye Drops", "\uD83D\uDC41", "#27AE60", 53),
            new SeedCategory("Ear Care Medicines", "\uD83D\uDC42", "#E74C3C", 54),
            new SeedCategory("Ear Drops", "\uD83D\uDC42", "#F39C12", 55),
            new SeedCategory("ENT Medicines", "\uD83D\uDC42", "#9B59B6", 56),
            new SeedCategory("Nasal Sprays", "\uD83D\uDE41", "#3498DB", 57),
            new SeedCategory("Oral Care Medicines", "\uD83E\uDDB7", "#1ABC9C", 58),
            new SeedCategory("Dental Care Products", "\uD83E\uDDB7", "#E67E22", 59),
            new SeedCategory("Women's Health", "\uD83D\uDC69", "#E74C3C", 60),
            new SeedCategory("Gynecology Medicines", "\uD83D\uDC69", "#C0392B", 61),
            new SeedCategory("Pregnancy Care", "\uD83E\uDD30", "#3498DB", 62),
            new SeedCategory("Fertility Medicines", "\uD83D\uDC69", "#9B59B6", 63),
            new SeedCategory("Hormonal Medicines", "\uD83D\uDC8A", "#2980B9", 64),
            new SeedCategory("Men's Health", "\uD83D\uDC68", "#2ECC71", 65),
            new SeedCategory("Urology Medicines", "\uD83D\uDC68", "#16A085", 66),
            new SeedCategory("Kidney Medicines", "\uD83E\uDE7A", "#8E44AD", 67),
            new SeedCategory("Nephrology Medicines", "\uD83E\uDE7A", "#C0392B", 68),
            new SeedCategory("Orthopedic Medicines", "\uD83E\uDDC0", "#E67E22", 69),
            new SeedCategory("Bone Health", "\uD83E\uDDC0", "#D35400", 70),
            new SeedCategory("Calcium Supplements", "\uD83E\uDDC0", "#0F9291", 71),
            new SeedCategory("Vitamin D Supplements", "\uD83E\uDDC0", "#27AE60", 72),
            new SeedCategory("Pediatrics", "\uD83E\uDDD2", "#3498DB", 73),
            new SeedCategory("Neonatal Medicines", "\uD83E\uDDD2", "#2980B9", 74),
            new SeedCategory("Vaccines", "\uD83D\uDC89", "#E74C3C", 75),
            new SeedCategory("Immunization Products", "\uD83D\uDC89", "#C0392B", 76),
            new SeedCategory("Oncology Medicines", "\uD83C\uDFEB", "#8E44AD", 77),
            new SeedCategory("Chemotherapy Medicines", "\uD83C\uDFEB", "#9B59B6", 78),
            new SeedCategory("Immunology Medicines", "\uD83D\uDC8A", "#1ABC9C", 79),
            new SeedCategory("Endocrinology Medicines", "\uD83D\uDC8A", "#F39C12", 80),
            new SeedCategory("Thyroid Medicines", "\uD83D\uDC8A", "#E67E22", 81),
            new SeedCategory("Rheumatology Medicines", "\uD83E\uDDCD", "#D35400", 82),
            new SeedCategory("Critical Care Medicines", "\uD83D\uDE91", "#E74C3C", 83),
            new SeedCategory("Emergency Medicines", "\uD83D\uDE91", "#C0392B", 84),
            new SeedCategory("ICU Medicines", "\uD83D\uDE91", "#2980B9", 85),
            new SeedCategory("Anesthesia Medicines", "\uD83D\uDE91", "#8E44AD", 86),
            new SeedCategory("IV Fluids", "\uD83E\uDE78", "#3498DB", 87),
            new SeedCategory("Electrolytes", "\uD83E\uDE78", "#27AE60", 88),
            new SeedCategory("Blood Products", "\uD83E\uDE78", "#E74C3C", 89),
            new SeedCategory("Nutrition Products", "\uD83E\uDD57", "#2ECC71", 90),
            new SeedCategory("Protein Supplements", "\uD83E\uDD57", "#F39C12", 91),
            new SeedCategory("Health Supplements", "\uD83E\uDD57", "#16A085", 92),
            new SeedCategory("Vitamins", "\uD83D\uDC8A", "#E67E22", 93),
            new SeedCategory("Minerals", "\uD83D\uDC8A", "#D35400", 94),
            new SeedCategory("Multivitamins", "\uD83D\uDC8A", "#9B59B6", 95),
            new SeedCategory("Ayurvedic Medicines", "\uD83C\uDF3F", "#27AE60", 96),
            new SeedCategory("Homeopathic Medicines", "\uD83C\uDF3F", "#1ABC9C", 97),
            new SeedCategory("Herbal Medicines", "\uD83C\uDF3F", "#2ECC71", 98),
            new SeedCategory("Unani Medicines", "\uD83C\uDF3F", "#16A085", 99),
            new SeedCategory("Siddha Medicines", "\uD83C\uDF3F", "#0F9291", 100),
            new SeedCategory("Nutraceuticals", "\uD83D\uDC8A", "#2980B9", 101),
            new SeedCategory("Medical Devices", "\uD83C\uDF21", "#8E44AD", 102),
            new SeedCategory("Surgical Consumables", "\uD83C\uDF21", "#E74C3C", 103),
            new SeedCategory("First Aid Products", "\uD83D\uDE91", "#E67E22", 104),
            new SeedCategory("Bandages", "\uD83C\uDF21", "#F39C12", 105),
            new SeedCategory("Dressings", "\uD83C\uDF21", "#D35400", 106),
            new SeedCategory("Sutures", "\uD83C\uDF21", "#C0392B", 107),
            new SeedCategory("Diagnostic Products", "\uD83D\uDD2C", "#3498DB", 108),
            new SeedCategory("Glucometers", "\uD83D\uDD2C", "#2980B9", 109),
            new SeedCategory("Test Strips", "\uD83D\uDD2C", "#27AE60", 110),
            new SeedCategory("Thermometers", "\uD83C\uDF21", "#E74C3C", 111),
            new SeedCategory("Blood Pressure Monitors", "\uD83D\uDD2C", "#C0392B", 112),
            new SeedCategory("Nebulizers", "\uD83D\uDE41", "#8E44AD", 113),
            new SeedCategory("Oxygen Accessories", "\uD83D\uDE41", "#9B59B6", 114),
            new SeedCategory("Mobility Aids", "\uD83E\uDDBC", "#0F9291", 115),
            new SeedCategory("Baby Care", "\uD83D\uDC76", "#3498DB", 116),
            new SeedCategory("Infant Nutrition", "\uD83D\uDC76", "#1ABC9C", 117),
            new SeedCategory("Personal Care", "\uD83E\uDDCD", "#E67E22", 118),
            new SeedCategory("Hair Care", "\uD83D\uDC87", "#D35400", 119),
            new SeedCategory("Skin Care Products", "\uD83E\uDDCD", "#C0392B", 120),
            new SeedCategory("Cosmetics", "\uD83D\uDC84", "#9B59B6", 121),
            new SeedCategory("Hygiene Products", "\uD83E\uDDFC", "#2ECC71", 122),
            new SeedCategory("Sanitizers", "\uD83E\uDDFC", "#27AE60", 123),
            new SeedCategory("Masks", "\uD83D\uDE37", "#2980B9", 124),
            new SeedCategory("Gloves", "\uD83E\uDDFA", "#F39C12", 125),
            new SeedCategory("Veterinary Medicines", "\uD83D\uDC3E", "#8E44AD", 126),
            new SeedCategory("Cold Chain Products", "\uD83E\uDDCA", "#3498DB", 127),
            new SeedCategory("Controlled Medicines", "\uD83D\uDD12", "#E74C3C", 128),
            new SeedCategory("Prescription Medicines", "\uD83D\uDCDD", "#C0392B", 129),
            new SeedCategory("OTC Medicines", "\uD83D\uDCDD", "#27AE60", 130),
            new SeedCategory("Hospital Supplies", "\uD83C\uDFE5", "#16A085", 131),
            new SeedCategory("Pharmacy Consumables", "\uD83D\uDC8A", "#0F9291", 132)
        );

        for (SeedCategory sc : categories) {
            Category category = Category.builder()
                    .name(sc.name())
                    .slug(sc.name().toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", ""))
                    .code(sc.name().toUpperCase().replaceAll("\\s+", "_").replaceAll("[^A-Z0-9_]", ""))
                    .description(sc.name())
                    .icon(sc.icon())
                    .color(sc.color())
                    .displayOrder(sc.displayOrder())
                    .status(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            categoryRepository.save(category);
        }
        log.info("Seeded {} default categories", categories.size());
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        List<Category> cats = categoryRepository.findAll();
        if (cats.isEmpty()) return;

        List<Product> products = List.of(
            Product.builder().name("Paracetamol 500mg").sku("#PAR-500").category(cats.get(0)).stockQuantity(500).price(BigDecimal.valueOf(12.50)).build(),
            Product.builder().name("Amoxicillin 250mg").sku("#AMX-250").category(cats.get(0)).stockQuantity(300).price(BigDecimal.valueOf(25.00)).build(),
            Product.builder().name("Vitamin C 1000mg").sku("#VIT-C").category(cats.get(0)).stockQuantity(200).price(BigDecimal.valueOf(15.75)).build(),
            Product.builder().name("Cetirizine 10mg").sku("#CET-10").category(cats.get(0)).stockQuantity(400).price(BigDecimal.valueOf(8.50)).build(),
            Product.builder().name("Aspirin 75mg").sku("#ASP-75").category(cats.get(0)).stockQuantity(600).price(BigDecimal.valueOf(6.00)).build(),
            Product.builder().name("Omeprazole 20mg").sku("#OME-20").category(cats.get(0)).stockQuantity(350).price(BigDecimal.valueOf(18.00)).build(),
            Product.builder().name("Metformin 500mg").sku("#MET-500").category(cats.get(0)).stockQuantity(250).price(BigDecimal.valueOf(22.00)).build(),
            Product.builder().name("Atorvastatin 10mg").sku("#ATO-10").category(cats.get(0)).stockQuantity(180).price(BigDecimal.valueOf(35.00)).build(),
            Product.builder().name("Ibuprofen 400mg").sku("#IBU-400").category(cats.get(0)).stockQuantity(450).price(BigDecimal.valueOf(10.00)).build(),
            Product.builder().name("Azithromycin 500mg").sku("#AZI-500").category(cats.get(0)).stockQuantity(150).price(BigDecimal.valueOf(85.00)).build()
        );

        productRepository.saveAll(products);
        log.info("Seeded {} products", products.size());
    }

    private void seedRacks() {
        if (rackRepository.count() > 0) return;

        List<SeedRack> racks = List.of(
            new SeedRack("RCK001", "Antibiotics", 3, 2, 150, 20, "ROOM_TEMP", "ACTIVE", 90),
            new SeedRack("RCK002", "Antacids", 4, 4, 200, 20, "COOL_DRY", "INACTIVE", 80),
            new SeedRack("RCK003", "Antihistamines", 5, 4, 150, 30, "ROOM_TEMP", "ACTIVE", 60),
            new SeedRack("RCK004", "General Medicine", 4, 6, 250, 30, "REFRIGERATED", "MAINTENANCE", 125),
            new SeedRack("RCK005", "Analgesics & Pain Relief", 6, 6, 200, 30, "ROOM_TEMP", "ACTIVE", 100),
            new SeedRack("RCK006", "Gastrointestinal Medicines", 7, 8, 300, 40, "COOL_DRY", "ACTIVE", 120),
            new SeedRack("RCK007", "Supplements", 3, 4, 80, 10, "ROOM_TEMP", "ACTIVE", 64),
            new SeedRack("RCK008", "Devices", 4, 4, 50, 10, "ROOM_TEMP", "ACTIVE", 10),
            new SeedRack("RCK009", "Powders", 6, 4, 130, 20, "ROOM_TEMP", "ACTIVE", 52),
            new SeedRack("RCK010", "Devices Supplements", 5, 2, 60, 20, "ROOM_TEMP", "ACTIVE", 36),
            new SeedRack("RCK011", "Oinment", 8, 4, 100, 30, "COOL_DRY", "MAINTENANCE", 30),
            new SeedRack("RCK012", "Tablet Supplements", 6, 6, 150, 20, "ROOM_TEMP", "ACTIVE", 90),
            new SeedRack("RCK013", "Tablet", 4, 4, 130, 20, "REFRIGERATED", "INACTIVE", 65),
            new SeedRack("RCK014", "Syrup", 3, 4, 100, 30, "ROOM_TEMP", "ACTIVE", 60),
            new SeedRack("RCK015", "Tablet", 4, 4, 120, 30, "COOL_DRY", "MAINTENANCE", 84),
            new SeedRack("RCK016", "Tablet", 5, 4, 150, 20, "ROOM_TEMP", "INACTIVE", 90)
        );

        for (SeedRack r : racks) {
            rackRepository.save(Rack.builder()
                    .code(r.code())
                    .category(r.category())
                    .rowsCount(r.rows())
                    .columns(r.columns())
                    .bins(r.bins())
                    .alertThreshold(r.alertThreshold())
                    .temperature(r.temperature())
                    .status(r.status())
                    .assignedMedicines(r.assigned())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        log.info("Seeded {} racks", racks.size());
    }

    private record SeedRack(String code, String category, Integer rows, Integer columns, Integer bins,
                            Integer alertThreshold, String temperature, String status, Integer assigned) {}

    private record SeedCategory(String name, String icon, String color, Integer displayOrder) {}
}
