package org.ever._4ever_be_scm.scm.mm.service;

import org.ever._4ever_be_scm.common.response.ApiResponse;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierDetailResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.SupplierListResponseDto;
import org.ever._4ever_be_scm.scm.mm.dto.supplier.SupplierCreateRequestDto;
import org.ever._4ever_be_scm.scm.mm.dto.supplier.SupplierUpdateRequestDto;
import org.ever._4ever_be_scm.scm.mm.vo.SupplierSearchVo;
import org.ever.event.CreateAuthUserResultEvent;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

public interface SupplierService {
    Page<SupplierListResponseDto> getSupplierList(SupplierSearchVo searchVo);
    SupplierDetailResponseDto getSupplierDetail(String supplierId);
    void createSupplier(
        SupplierCreateRequestDto dto,
        DeferredResult<ResponseEntity<ApiResponse<CreateAuthUserResultEvent>>> deferredResult
    );
    void updateSupplier(String supplierId, SupplierUpdateRequestDto dto);
}
