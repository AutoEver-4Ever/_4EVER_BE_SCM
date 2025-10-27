package org.ever._4ever_be_scm.scm.mm.repository;

import org.ever._4ever_be_scm.scm.mm.entity.ProductRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRequestItemRepository extends JpaRepository<ProductRequestItem, String> {
    List<ProductRequestItem> findByProductRequestId(String productRequestId);
}
