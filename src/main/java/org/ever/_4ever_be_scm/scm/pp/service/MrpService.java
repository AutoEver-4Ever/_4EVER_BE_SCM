package org.ever._4ever_be_scm.scm.pp.service;

import org.ever._4ever_be_scm.scm.mm.dto.ToggleCodeLabelDto;
import org.ever._4ever_be_scm.scm.pp.dto.MrpRunConvertRequestDto;
import org.ever._4ever_be_scm.scm.pp.dto.MrpRunQueryResponseDto;

import java.util.List;

public interface MrpService {

    /**
     * MRP → MRP_RUN 계획주문 전환
     */
    void convertToMrpRun(MrpRunConvertRequestDto requestDto);

    /**
     * MRP 계획주문 목록 조회
     */
    MrpRunQueryResponseDto getMrpRunList(String status, String quotationId, int page, int size);

    /**
     * MRP Run 견적 목록 조회 (Toggle용)
     */
    List<ToggleCodeLabelDto> getMrpRunQuotationList();
}
