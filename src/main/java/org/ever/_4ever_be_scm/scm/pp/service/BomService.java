package org.ever._4ever_be_scm.scm.pp.service;

import org.ever._4ever_be_scm.scm.pp.dto.BomCreateRequestDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomListResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BomService {
    void createBom(BomCreateRequestDto requestDto);
    Page<BomListResponseDto> getBomList(Pageable pageable);
    BomDetailResponseDto getBomDetail(String bomId);
    void updateBom(String bomId,BomCreateRequestDto requestDto);
}
