package org.ever._4ever_be_scm.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.ever._4ever_be_scm.scm.iv.repository.SupplierUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplierUserInitializer implements CommandLineRunner {

    private final SupplierUserRepository supplierUserRepository;

    private static final List<SeedSupplierUser> SUPPLIER_USERS = List.of(
        new SeedSupplierUser(
            "1f0b4b1b-4890-688b-befc-ff061e0fbaad", // supplier-user@everp.com
            "supplier-user@everp.com",
            "공급사 사용자",
            "010-8000-0000",
            "CU-001"
        ),
        new SeedSupplierUser(
            "1f0b4b1b-4956-649c-befc-ff061e0fbaad", // supplier-admin@everp.com
            "supplier-admin@everp.com",
            "공급사 관리자",
            "010-8000-0001",
            "CU-ADMIN-001"
        )
    );

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[Initializer] 공급사 사용자 기본 데이터 점검 시작");

        for (SeedSupplierUser seed : SUPPLIER_USERS) {
            supplierUserRepository.findByUserId(seed.userId()).ifPresentOrElse(
                existing -> log.debug("[Initializer] 공급사 사용자 이미 존재: {}", existing.getUserId()),
                () -> createSupplierUser(seed)
            );
        }

        log.info("[Initializer] 공급사 사용자 기본 데이터 점검 완료");
    }

    private void createSupplierUser(SeedSupplierUser seed) {
        SupplierUser supplierUser = SupplierUser.builder()
            .userId(seed.userId())
            .supplierUserName(seed.displayName())
            .supplierUserEmail(seed.loginEmail())
            .supplierUserPhoneNumber(seed.phoneNumber())
            .customerUserCode(seed.customerUserCode())
            .build();
        supplierUserRepository.save(supplierUser);

        log.info("[Initializer] 공급사 사용자 생성 - userId: {}, name: {}", seed.userId(), seed.displayName());
    }

    private record SeedSupplierUser(
        String userId,
        String loginEmail,
        String displayName,
        String phoneNumber,
        String customerUserCode
    ) {
    }
}
