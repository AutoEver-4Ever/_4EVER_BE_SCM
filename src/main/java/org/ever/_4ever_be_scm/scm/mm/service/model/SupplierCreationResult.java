package org.ever._4ever_be_scm.scm.mm.service.model;

import org.ever._4ever_be_scm.scm.iv.entity.SupplierCompany;
import org.ever._4ever_be_scm.scm.iv.entity.SupplierUser;

public record SupplierCreationResult(
    SupplierCompany supplierCompany,
    SupplierUser supplierUser
) {
}
