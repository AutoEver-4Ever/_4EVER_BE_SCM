package org.ever._4ever_be_scm.scm.repository;

import org.ever._4ever_be_scm.scm.entity.SupplierUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplierUserRepository extends JpaRepository<SupplierUser, String> {
}
