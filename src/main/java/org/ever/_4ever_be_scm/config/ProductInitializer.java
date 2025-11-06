package org.ever._4ever_be_scm.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.repository.ProductRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공급사에 매핑되는 외장재 제품 목업 데이터를 생성한다.
 * SupplierUserInitializer가 선행되어 supplier_company 16개가 준비되어 있다는 전제하에 동작한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(2)
public class ProductInitializer implements CommandLineRunner {

    private final SupplierCompanyRepository supplierCompanyRepository;
    private final ProductRepository productRepository;

    private static final List<ProductSeed> PRODUCT_SEEDS = List.of(
        new ProductSeed("019a3df1-7843-7590-a5fd-94aa9aae7d0a", "AUTO-EXT-001", "Exterior Door Panel", "MATERIAL", "EA",
            new BigDecimal("420000"), new BigDecimal("525000")),
        new ProductSeed("019a52d5-2824-7c7e-9826-e5c56987d189", "AUTO-EXT-002", "Rear Bumper Assembly", "MATERIAL", "EA",
            new BigDecimal("380000"), new BigDecimal("472500")),
        new ProductSeed("019a59a5-f5d5-7003-98b0-f8d77df4f031", "AUTO-EXT-003", "Front Fender Panel", "MATERIAL", "EA",
            new BigDecimal("310000"), new BigDecimal("395000")),
        new ProductSeed("019a52d5-1ad8-754d-af67-e541f85473c4", "AUTO-EXT-004", "Side Skirt Kit", "MATERIAL", "SET",
            new BigDecimal("260000"), new BigDecimal("325000")),
        new ProductSeed("019a52d5-0df8-724b-a16f-7a9d3bcd5384", "AUTO-EXT-005", "Roof Outer Skin", "MATERIAL", "EA",
            new BigDecimal("500000"), new BigDecimal("625000")),
        new ProductSeed("019a52d5-01a5-758a-8d36-e2ef00d8ffb7", "AUTO-EXT-006", "Trunk Lid Assembly", "MATERIAL", "EA",
            new BigDecimal("340000"), new BigDecimal("422500")),
        new ProductSeed("019a52d4-f64f-7028-8715-365ab52e4879", "AUTO-EXT-007", "Hood Outer Panel", "MATERIAL", "EA",
            new BigDecimal("360000"), new BigDecimal("450000")),
        new ProductSeed("019a52d4-e876-709e-8646-d31b8db20a95", "AUTO-EXT-008", "Front Radiator Grille", "MATERIAL", "EA",
            new BigDecimal("220000"), new BigDecimal("286000")),
        new ProductSeed("019a52d4-dc52-7605-868b-0ed7486cb106", "AUTO-EXT-009", "Left Quarter Panel", "MATERIAL", "EA",
            new BigDecimal("280000"), new BigDecimal("357000")),
        new ProductSeed("019a52d4-cffd-7876-9a7e-34590cc2c447", "AUTO-EXT-010", "Right Quarter Panel", "MATERIAL", "EA",
            new BigDecimal("280000"), new BigDecimal("357000")),
        new ProductSeed("019a52d4-c49d-77d8-912d-960432b4565c", "AUTO-EXT-011", "Side Mirror Housing Set", "MATERIAL", "SET",
            new BigDecimal("180000"), new BigDecimal("235000")),
        new ProductSeed("019a52d4-b8d1-7509-9072-31d2e147055e", "AUTO-EXT-012", "Front Spoiler Lip", "MATERIAL", "EA",
            new BigDecimal("150000"), new BigDecimal("198000")),
        new ProductSeed("019a52d4-ab46-7abe-9071-025222fb6144", "AUTO-EXT-013", "Rear Diffuser Module", "MATERIAL", "EA",
            new BigDecimal("210000"), new BigDecimal("273000")),
        new ProductSeed("019a52d4-96be-72cb-85dd-19fbe3d80880", "AUTO-EXT-014", "Wheel Arch Cover Set", "MATERIAL", "SET",
            new BigDecimal("140000"), new BigDecimal("189000")),
        new ProductSeed("019a52d4-8961-76f0-a2a8-0dbd756d30da", "AUTO-EXT-015", "Door Sill Guard Set", "MATERIAL", "SET",
            new BigDecimal("120000"), new BigDecimal("162000")),
        new ProductSeed("019a52d4-7141-7a42-8674-a4c6597acfd7", "AUTO-EXT-016", "Fuel Filler Door Cover", "MATERIAL", "EA",
            new BigDecimal("90000"), new BigDecimal("125000"))
    );

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[Initializer] 외장재 제품 시드를 시작합니다.");

        Map<String, SupplierCompany> suppliersById = supplierCompanyRepository.findAll().stream()
            .collect(Collectors.toMap(SupplierCompany::getId, Function.identity()));
        if (suppliersById.isEmpty()) {
            log.warn("[Initializer] supplier_company 데이터가 존재하지 않아 제품 시드를 건너뜁니다.");
            return;
        }

        Set<String> existingCodes = productRepository.findAll().stream()
            .map(Product::getProductCode)
            .collect(Collectors.toSet());

        int created = 0;
        for (ProductSeed seed : PRODUCT_SEEDS) {
            SupplierCompany company = suppliersById.get(seed.supplierCompanyId());
            if (company == null) {
                log.warn("[Initializer] 공급사를 찾을 수 없어 제품 생성을 건너뜁니다. supplierCompanyId={}, productCode={}",
                    seed.supplierCompanyId(), seed.productCode());
                continue;
            }
            if (existingCodes.contains(seed.productCode())) {
                log.debug("[Initializer] 이미 존재하는 제품을 건너뜁니다. productCode={}", seed.productCode());
                continue;
            }

            Product product = Product.builder()
                .productCode(seed.productCode())
                .category(seed.category())
                .supplierCompany(company)
                .productName(seed.productName())
                .unit(seed.unit())
                .originPrice(seed.originPrice())
                .sellingPrice(seed.sellingPrice())
                .build();

            productRepository.save(product);
            created++;
            log.info("[Initializer] 제품 생성 완료 - productCode={}, supplierCompanyId={}",
                seed.productCode(), company.getId());
        }

        log.info("[Initializer] 외장재 제품 시드 완료 (생성 {}건, 계획 {}건)", created, PRODUCT_SEEDS.size());
    }

    private record ProductSeed(
        String supplierCompanyId,
        String productCode,
        String productName,
        String category,
        String unit,
        BigDecimal originPrice,
        BigDecimal sellingPrice
    ) {
    }
}
