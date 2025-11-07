package org.ever._4ever_be_scm.scm.pp.service.impl;

import org.ever._4ever_be_scm.scm.iv.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProductRepository extends JpaRepository<Product, String> {

    Product getProductNameBySupplierCompanyId(String supplierCompanyId);
}
