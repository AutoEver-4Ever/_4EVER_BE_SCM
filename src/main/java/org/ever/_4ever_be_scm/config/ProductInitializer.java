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
 * 공급사에 매핑되는 외장재 소재(Material) 목업 데이터를 생성한다.
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
        new ProductSeed("019a3df1-7843-7590-a5fd-94aa9aae7d0a", "dd406122-cbda-45c3-be70-0911a48d3efd", "Aluminum Panel Sheet (2.5mm)", "MATERIAL", "KG",
            new BigDecimal("75000"), new BigDecimal("95000")),
        new ProductSeed("019a52d5-2824-7c7e-9826-e5c56987d189", "8c29c196-3bbc-4d78-8a8a-0e752726121b", "High-Strength Steel Coil", "MATERIAL", "KG",
            new BigDecimal("82000"), new BigDecimal("102000")),
        new ProductSeed("019a59a5-f5d5-7003-98b0-f8d77df4f031", "1aab1ddf-c532-4010-92d9-61f3f2f212d2", "Galvanized Steel Panel", "MATERIAL", "KG",
            new BigDecimal("68000"), new BigDecimal("85000")),
        new ProductSeed("019a52d5-1ad8-754d-af67-e541f85473c4", "971789f2-da29-4716-814f-913272abd9b1", "Stainless Mesh Sheet", "MATERIAL", "M2",
            new BigDecimal("54000"), new BigDecimal("69000")),
        new ProductSeed("019a52d5-0df8-724b-a16f-7a9d3bcd5384", "ca3b872b-e950-4685-89a6-b99fd465505a", "ABS Resin Pellet", "MATERIAL", "KG",
            new BigDecimal("28000"), new BigDecimal("36000")),
        new ProductSeed("019a52d5-01a5-758a-8d36-e2ef00d8ffb7", "5b72ab09-bd63-4788-9b21-11ca574c5090", "Polypropylene Pellet", "MATERIAL", "KG",
            new BigDecimal("22000"), new BigDecimal("29000")),
        new ProductSeed("019a52d4-f64f-7028-8715-365ab52e4879", "bb6ab0f4-6d68-459a-825b-347b12cb0945", "Carbon Fiber Fabric", "MATERIAL", "M2",
            new BigDecimal("110000"), new BigDecimal("140000")),
        new ProductSeed("019a52d4-e876-709e-8646-d31b8db20a95", "a3b1c740-8bf9-4141-a113-f30bdc1dd488", "Glass Fiber Mat", "MATERIAL", "KG",
            new BigDecimal("36000"), new BigDecimal("47000")),
        new ProductSeed("019a52d4-dc52-7605-868b-0ed7486cb106", "e538f74c-669c-46d4-b70d-909442afbd08", "Tempered Glass Insert", "MATERIAL", "EA",
            new BigDecimal("120000"), new BigDecimal("150000")),
        new ProductSeed("019a52d4-cffd-7876-9a7e-34590cc2c447", "0b39d2a7-370c-44d1-aefa-b38e8de31625", "Polycarbonate Lens", "MATERIAL", "KG",
            new BigDecimal("45000"), new BigDecimal("58000")),
        new ProductSeed("019a52d4-c49d-77d8-912d-960432b4565c", "5d911685-2230-472b-b30e-051502e245e6", "Rubber Seal Strip", "MATERIAL", "M",
            new BigDecimal("15000"), new BigDecimal("22000")),
        new ProductSeed("019a52d4-b8d1-7509-9072-31d2e147055e", "70f5ad3c-d600-4400-9b72-25e6bc85dc33", "NVH Damping Laminate", "MATERIAL", "M2",
            new BigDecimal("26000"), new BigDecimal("34000")),
        new ProductSeed("019a52d4-ab46-7abe-9071-025222fb6144", "77baedf8-3810-46f5-aa7e-82f596dee60e", "Chrome Trim Strip", "MATERIAL", "M",
            new BigDecimal("33000"), new BigDecimal("43000")),
        new ProductSeed("019a52d4-96be-72cb-85dd-19fbe3d80880", "db5b6809-db60-4b60-b0d9-a51df9317c3b", "Hardware Fastening Kit", "MATERIAL", "SET",
            new BigDecimal("32000"), new BigDecimal("42000")),
        new ProductSeed("019a52d4-8961-76f0-a2a8-0dbd756d30da", "8900c53f-8040-460a-8f75-14bf53866cb3", "Surface Coating Pack", "MATERIAL", "L",
            new BigDecimal("30000"), new BigDecimal("39000")),
        new ProductSeed("019a52d4-7141-7a42-8674-a4c6597acfd7", "c9f41fc0-5c18-4d30-b155-2941239641c1", "Structural Adhesive Pack", "MATERIAL", "KG",
            new BigDecimal("29000"), new BigDecimal("38000"))
    );

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[Initializer] 외장재 소재 시드를 시작합니다.");

        Map<String, SupplierCompany> suppliersById = supplierCompanyRepository.findAll().stream()
            .collect(Collectors.toMap(SupplierCompany::getId, Function.identity()));
        if (suppliersById.isEmpty()) {
            log.warn("[Initializer] supplier_company 데이터가 존재하지 않아 제품 시드를 건너뜁니다.");
            return;
        }

        Set<String> existingMaterials = productRepository.findAll().stream()
            .map(product -> {
                SupplierCompany supplier = product.getSupplierCompany();
                String supplierId = supplier != null ? supplier.getId() : "";
                return supplierId + ":" + product.getProductName();
            })
            .collect(Collectors.toSet());

        int created = 0;
        for (ProductSeed seed : PRODUCT_SEEDS) {
            SupplierCompany company = suppliersById.get(seed.supplierCompanyId());
            if (company == null) {
                log.warn("[Initializer] 공급사를 찾을 수 없어 소재 생성을 건너뜁니다. supplierCompanyId={}, materialName={}",
                    seed.supplierCompanyId(), seed.productName());
                continue;
            }
            String uniqueKey = company.getId() + ":" + seed.productName();
            if (existingMaterials.contains(uniqueKey)) {
                log.debug("[Initializer] 이미 존재하는 소재를 건너뜁니다. supplierCompanyId={}, productName={}",
                    seed.supplierCompanyId(), seed.productName());
                continue;
            }

            Product product = Product.builder()
                .id(seed.productId())
                .category(seed.category())
                .supplierCompany(company)
                .productName(seed.productName())
                .unit(seed.unit())
                .originPrice(seed.originPrice())
                .sellingPrice(seed.sellingPrice())
                .build();

            productRepository.save(product);
            created++;
            log.info("[Initializer] 소재 생성 완료 - materialName={}, supplierCompanyId={}",
                seed.productName(), company.getId());
        }

        log.info("[Initializer] 외장재 소재 시드 완료 (생성 {}건, 계획 {}건)", created, PRODUCT_SEEDS.size());
    }

    private record ProductSeed(
        String supplierCompanyId,
        String productId,
        String productName,
        String category,
        String unit,
        BigDecimal originPrice,
        BigDecimal sellingPrice
    ) {
    }
}
