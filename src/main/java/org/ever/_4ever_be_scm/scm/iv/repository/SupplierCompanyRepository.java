package org.ever._4ever_be_scm.scm.iv.repository;

import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierCompanyRepository extends JpaRepository<SupplierCompany, String> {
    Optional<SupplierCompany> findByCompanyName(String companyName);

    Optional<SupplierCompany> findBySupplierUser(SupplierUser supplierUser);

    Optional<SupplierCompany> findBySupplierUser_UserId(String userId);
}
