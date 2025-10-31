package org.ever._4ever_be_scm.scm.pp.service;

import org.ever._4ever_be_scm.scm.pp.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuotationService {
    Page<QuotationSimulateResponseDto> simulateQuotations(QuotationSimulateRequestDto requestDto, Pageable pageable);
    List<MpsPreviewResponseDto> previewMps(List<String> quotationIds);
    void confirmQuotations(QuotationConfirmRequestDto requestDto);
}
