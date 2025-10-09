package org.ever._4ever_be_scm.scm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SCM 응답 정보")
public class ScmResponseDto {

    @Schema(description = "SCM 트랜잭션 ID", example = "scm-abc123-def456")
    private String scmId;

    @Schema(description = "주문 ID", example = "order-001")
    private String orderId;

    @Schema(description = "상품 ID", example = "product-001")
    private String productId;

    @Schema(description = "수량", example = "10")
    private Integer quantity;

    @Schema(description = "창고 ID", example = "warehouse-001")
    private String warehouseId;

    @Schema(description = "상태", example = "RESERVED", allowableValues = {"RESERVED", "RELEASED", "SHIPPED", "DELIVERED"})
    private String status;

    @Schema(description = "설명", example = "재고 예약 완료")
    private String description;

    @Schema(description = "생성 시각", example = "2025-10-09T12:34:56")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시각", example = "2025-10-09T12:34:56")
    private LocalDateTime updatedAt;
}
