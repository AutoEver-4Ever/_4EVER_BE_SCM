package org.ever._4ever_be_scm.scm.pp.service;

import org.ever._4ever_be_scm.scm.pp.dto.BomCreateRequestDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomDetailResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.BomListResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.ProductMapResponseDto;
import org.ever._4ever_be_scm.scm.pp.dto.ProductDetailResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BomService {
    void createBom(BomCreateRequestDto requestDto);
    Page<BomListResponseDto> getBomList(Pageable pageable);
    BomDetailResponseDto getBomDetail(String bomId);
    void updateBom(String bomId,BomCreateRequestDto requestDto);

    /**
     * Product ID와 이름 맵 조회 (전체 목록)
     */
    List<ProductMapResponseDto> getProductMap();

    /**
     * Product 상세 정보 조회
     */
    ProductDetailResponseDto getProductDetail(String productId);

    /**
     * Operation ID와 이름 맵 조회 (전체 목록)
     */
    List<ProductMapResponseDto> getOperationMap();
}
