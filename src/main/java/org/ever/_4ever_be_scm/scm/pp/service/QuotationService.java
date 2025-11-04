package org.ever._4ever_be_scm.scm.pp.service;

import org.ever._4ever_be_scm.scm.pp.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface QuotationService {

    /**
     * 견적 목록 조회 (그룹핑된 형태)
     */
    QuotationGroupListResponseDto getQuotationList(
            String statusCode,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );

    Page<QuotationSimulateResponseDto> simulateQuotations(QuotationSimulateRequestDto requestDto, Pageable pageable);
    List<MpsPreviewResponseDto> previewMps(List<String> quotationIds);
    void confirmQuotations(QuotationConfirmRequestDto requestDto);

    /**
     * MPS 조회 (주차별, bomId 기준)
     */
    MpsQueryResponseDto getMps(String bomId, LocalDate startDate, LocalDate endDate, int page, int size);

    /**
     * MRP 조회 (원자재별 그룹핑)
     */
    MrpQueryResponseDto getMrp(String bomId, String quotationId, String availableStatusCode, int page, int size);
}
