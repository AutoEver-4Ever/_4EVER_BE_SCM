package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomListResponseDto {
    private String bomId;
    private String bomNumber;
    private String productId;
    private String productNumber;
    private String productName;
    private String version;
    private String statusCode;
    private LocalDateTime lastModifiedAt;
}
