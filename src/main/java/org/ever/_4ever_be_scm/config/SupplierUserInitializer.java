package org.ever._4ever_be_scm.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierCompanyRepository;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierUserInitializer implements CommandLineRunner {

    private final SupplierUserRepository supplierUserRepository;
    private final SupplierCompanyRepository supplierCompanyRepository;

    private static final String[] COMPANY_NAMES = new String[] {
        "Ever Exterior Parts Co.",
        "한빛 오토 외장",
        "Seohan Body Panels",
        "국일 카 바디",
        "Prime Bumper & Grill",
        "성우 모터스 트림",
        "Aurora Auto Exterior",
        "동림 차체 솔루션",
        "Neo Fascia Tech",
        "현우 자동차 외장"
    };

    private static final String[] BASE_ADDRESSES = new String[] {
        "경기도 화성시 자동차산업로 123",
        "울산광역시 남구 산업로 45",
        "부산광역시 강서구 녹산산단 301",
        "충청남도 아산시 외장로 77",
        "인천광역시 남동구 공단로 12",
        "세종특별자치시 생산로 9",
        "대구광역시 달성군 테크노대로 88",
        "광주광역시 광산구 산업로 501",
        "경상북도 구미시 4공단로 33",
        "전라북도 익산시 국가산단 102"
    };

    private static final String[] DETAIL_ADDRESSES = new String[] {
        "R&D Center, 2F",
        "Assembly Plant A",
        "Press Shop Bldg.",
        "도장동 3라인",
        "물류센터 Zone C",
        "생산동 1층",
        "QA Lab, Room 204",
        "Mold Shop #5",
        "Warehouse D-12",
        "Office Tower 11F"
    };

    private static final String[] OFFICE_PHONES = new String[] {
        "02-1234-5678",
        "031-345-6789",
        "052-234-5566",
        "041-222-9090",
        "032-555-1212",
        "044-777-8888",
        "053-222-3344",
        "062-987-6543",
        "054-111-2222",
        "063-333-4444"
    };

    private static final Map<String, String> ACCOUNT_IDS = Map.ofEntries(
        // base
        Map.entry("supplier-admin@everp.com", "019a3df1-7843-7590-a5fd-94aa9aae7d0a"),
        // 1..15 (Auth AdminInitializer 기준)
        Map.entry("supplier-admin1@everp.com", "019a52d5-2824-7c7e-9826-e5c56987d189"),
        Map.entry("supplier-admin2@everp.com", "019a59a5-f5d5-7003-98b0-f8d77df4f031"),
        Map.entry("supplier-admin3@everp.com", "019a52d5-1ad8-754d-af67-e541f85473c4"),
        Map.entry("supplier-admin4@everp.com", "019a52d5-0df8-724b-a16f-7a9d3bcd5384"),
        Map.entry("supplier-admin5@everp.com", "019a52d5-01a5-758a-8d36-e2ef00d8ffb7"),
        Map.entry("supplier-admin6@everp.com", "019a52d4-f64f-7028-8715-365ab52e4879"),
        Map.entry("supplier-admin7@everp.com", "019a52d4-e876-709e-8646-d31b8db20a95"),
        Map.entry("supplier-admin8@everp.com", "019a52d4-dc52-7605-868b-0ed7486cb106"),
        Map.entry("supplier-admin9@everp.com", "019a52d4-cffd-7876-9a7e-34590cc2c447"),
        Map.entry("supplier-admin10@everp.com", "019a52d4-c49d-77d8-912d-960432b4565c"),
        Map.entry("supplier-admin11@everp.com", "019a52d4-b8d1-7509-9072-31d2e147055e"),
        Map.entry("supplier-admin12@everp.com", "019a52d4-ab46-7abe-9071-025222fb6144"),
        Map.entry("supplier-admin13@everp.com", "019a52d4-96be-72cb-85dd-19fbe3d80880"),
        Map.entry("supplier-admin14@everp.com", "019a52d4-8961-76f0-a2a8-0dbd756d30da"),
        Map.entry("supplier-admin15@everp.com", "019a52d4-7141-7a42-8674-a4c6597acfd7")
    );

    @Override
    public void run(String... args) {
        log.info("[Initializer] 공급사 사용자/회사 시드 시작");

        List<String> emails = new ArrayList<>();
        emails.add("supplier-admin@everp.com");
        for (int i = 1; i <= 15; i++) {
            emails.add("supplier-admin" + i + "@everp.com");
        }

        int idx = 0;
        for (String email : emails) {
            String userId = ACCOUNT_IDS.get(email);
            if (userId == null) {
                log.warn("[Initializer] supplier ACCOUNT_IDS에 매핑이 없습니다: {}", email);
                idx++;
                continue;
            }

            try {
                final int currentIndex = idx;
                SupplierUser user = supplierUserRepository.findByUserId(userId)
                    .orElseGet(() -> createSupplierUser(userId, email, currentIndex));

                supplierCompanyRepository.findBySupplierUser(user)
                    .orElseGet(() -> createSupplierCompany(user, userId, currentIndex));
            } catch (Exception e) {
                log.warn("[Initializer] 시드 실패 - email: {}, userId: {}, msg: {}", email, userId, e.getMessage());
            }

            idx++;
        }

        log.info("[Initializer] 공급사 사용자/회사 시드 완료");
    }

    private SupplierUser createSupplierUser(String userId, String email, int idx) {
        String displayName = (idx == 0) ? "공급사 관리자" : ("공급사 관리자" + idx);
        String phone = String.format(Locale.ROOT, "010-%04d-%04d", 8000 + (idx % 1000), 1 + idx);

        String customerCode = "CUC-" + trailing7(userId);

        SupplierUser user = SupplierUser.builder()
            .id(userId)          // Auth userId와 동일하게 고정
            .userId(userId)
            .supplierUserName(displayName)
            .supplierUserEmail(email)
            .supplierUserPhoneNumber(phone)
            .customerUserCode(customerCode)
            .build();
        SupplierUser saved = supplierUserRepository.save(user);
        log.info("[Initializer] SupplierUser 생성: {} ({})", email, userId);
        return saved;
    }

    private SupplierCompany createSupplierCompany(SupplierUser user, String userId, int idx) {
        String suffix = trailing7(userId);
        String companyCode = "SUP-" + suffix;

        String companyName = COMPANY_NAMES[idx % COMPANY_NAMES.length];
        String baseAddress = BASE_ADDRESSES[idx % BASE_ADDRESSES.length];
        String detailAddress = DETAIL_ADDRESSES[idx % DETAIL_ADDRESSES.length];
        String officePhone = OFFICE_PHONES[idx % OFFICE_PHONES.length];

        String category = (idx == 0 || idx == 1) ? "부품" : (idx == 2 ? "기타" : "원자재");

        int leadTimeSeconds;
        if (idx == 0) leadTimeSeconds = 10;          // 10초
        else if (idx == 1) leadTimeSeconds = 30;     // 30초
        else if (idx == 2) leadTimeSeconds = 300;    // 5분
        else leadTimeSeconds = 300 + ((idx - 3) % 6) * 60; // 5~10분 범위

        SupplierCompany company = SupplierCompany.builder()
            .supplierUser(user)
            .companyCode(companyCode)
            .companyName(companyName)
            .businessNumber("BN-" + suffix)
            .status("ACTIVE")
            .baseAddress(baseAddress)
            .detailAddress(detailAddress)
            .category(category)
            .officePhone(officePhone)
            .deliveryDays(Duration.ofSeconds(leadTimeSeconds))
            .build();

        SupplierCompany saved = supplierCompanyRepository.save(company);
        log.info("[Initializer] SupplierCompany 생성: {} (code: {}, leadTimeSec: {})", companyName, companyCode, leadTimeSeconds);
        return saved;
    }

    private String trailing7(String uuid) {
        String compact = uuid.replace("-", "");
        int len = compact.length();
        return (len <= 7) ? compact : compact.substring(len - 7);
    }
}
