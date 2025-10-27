package org.ever._4ever_be_scm.scm.mm.service;

import org.ever._4ever_be_scm.scm.mm.dto.SupplierDetailResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierListResponseDto;
import org.ever._4ever_be_scm.scm.mm.vo.SupplierSearchVo;
import org.springframework.data.domain.Page;

public interface SupplierService {
    Page<SupplierListResponseDto> getSupplierList(SupplierSearchVo searchVo);
    SupplierDetailResponseDto getSupplierDetail(String supplierId);
}
