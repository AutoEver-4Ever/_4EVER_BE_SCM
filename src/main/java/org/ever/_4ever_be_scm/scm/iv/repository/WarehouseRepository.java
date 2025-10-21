package org.ever._4ever_be_scm.scm.iv.repository;

import org.ever._4ever_be_scm.scm.iv.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {

}
