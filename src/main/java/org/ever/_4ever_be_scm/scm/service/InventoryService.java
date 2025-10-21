package org.ever._4ever_be_scm.scm.service;

import org.ever._4ever_be_scm.scm.dto.InventoryItemDto;
import org.ever._4ever_be_scm.scm.dto.InventoryItemDetailDto;
import org.ever._4ever_be_scm.scm.dto.ShortageItemDto;
import org.ever._4ever_be_scm.scm.dto.ShortageItemPreviewDto;
import org.ever._4ever_be_scm.scm.vo.InventoryFilterVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 재고 관리 서비스 인터페이스
 */
public interface InventoryService {
    /**
     * 재고 목록 조회
     * 
     * @param filterVo 필터 조건
     * @param pageable 페이징 정보
     * @return 재고 목록
     */
    Page<InventoryItemDto> getInventoryItems(InventoryFilterVo filterVo, Pageable pageable);
    
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
}
