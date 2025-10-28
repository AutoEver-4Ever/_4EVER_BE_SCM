package org.ever._4ever_be_scm.scm.iv.service;

import org.ever._4ever_be_scm.scm.iv.dto.InventoryItemDto;
import org.ever._4ever_be_scm.scm.iv.dto.InventoryItemDetailDto;
import org.ever._4ever_be_scm.scm.iv.dto.PagedResponseDto;
import org.ever._4ever_be_scm.scm.iv.dto.ShortageItemDto;
import org.ever._4ever_be_scm.scm.iv.dto.ShortageItemPreviewDto;
import org.ever._4ever_be_scm.scm.iv.dto.request.AddInventoryItemRequest;
import org.ever._4ever_be_scm.scm.iv.vo.InventoryFilterVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 재고 관리 서비스 인터페이스
 */
public interface InventoryService {

    /**
     * 재고 목록 조회 (필터링 포함)
     * 
     * @param type 검색 타입
     * @param keyword 검색 키워드
     * @param statusCode 상태 코드
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 재고 목록
     */
    PagedResponseDto<InventoryItemDto> getInventoryItemsWithFilters(String type, String keyword, String statusCode, Integer page, Integer size);
    
    /**
     * 재고 상세 정보 조회
     * 
     * @param itemId 재고 ID
     * @return 재고 상세 정보
     */
    InventoryItemDetailDto getInventoryItemDetail(String itemId);
    
    /**
     * 부족 재고 목록 조회
     * 
     * @param status 상태 필터 (주의, 긴급)
     * @param pageable 페이징 정보
     * @return 부족 재고 목록
     */
    Page<ShortageItemDto> getShortageItems(String status, Pageable pageable);
    
    /**
     * 부족 재고 간단 정보 조회
     * 
     * @param pageable 페이징 정보
     * @return 부족 재고 간단 정보 목록
     */
    Page<ShortageItemPreviewDto> getShortageItemsPreview(Pageable pageable);

    /**
     * 재고 추가
     * 
     * @param request 재고 추가 요청
     */
    void addInventoryItem(AddInventoryItemRequest request);

    /**
     * 안전재고 수정
     * 
     * @param itemId 제품 ID
     * @param safetyStock 안전재고 수량
     */
    void updateSafetyStock(String itemId, Integer safetyStock);
}
