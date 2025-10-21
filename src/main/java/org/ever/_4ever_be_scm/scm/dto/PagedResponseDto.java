package org.ever._4ever_be_scm.scm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 데이터 응답 Wrapper DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponseDto<T> {
    /**
     * 페이지 데이터
     */
    private List<T> content;
    
    /**
     * 페이지 정보
     */
    private PageResponseDto page;
    
    /**
     * Page 객체로부터 PagedResponseDto 생성
     */
    public static <T> PagedResponseDto<T> from(Page<T> page) {
        return PagedResponseDto.<T>builder()
                .content(page.getContent())
                .page(PageResponseDto.from(page))
                .build();
    }
}
