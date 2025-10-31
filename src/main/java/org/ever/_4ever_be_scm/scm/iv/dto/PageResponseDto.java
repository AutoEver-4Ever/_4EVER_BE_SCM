package org.ever._4ever_be_scm.scm.iv.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * 페이징 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResponseDto {
    /**
     * 페이지 번호 (0부터 시작)
     */
    private int number;
    
    /**
     * 페이지 크기
     */
    private int size;
    
    /**
     * 총 요소 수
     */
    private long totalElements;
    
    /**
     * 총 페이지 수
     */
    private int totalPages;
    
    /**
     * 다음 페이지 존재 여부
     */
    private boolean hasNext;
    
    /**
     * Page 객체로부터 PageResponseDto 생성
     */
    public static <T> PageResponseDto from(Page<T> page) {
        return PageResponseDto.builder()
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .build();
    }
}
