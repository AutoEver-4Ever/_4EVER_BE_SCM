package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product ID와 이름 맵 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMapResponseDto {
    /**
     * Product ID (key)
     */
    private String key;

    /**
     * Product 이름 (value)
     */
    private String value;
}
