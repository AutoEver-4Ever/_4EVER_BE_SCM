package org.ever._4ever_be_scm.scm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SCM 요청 정보")
public class ScmRequestDto {

    @Schema(description = "주문 ID", example = "order-001", required = true)
    @NotBlank(message = "주문 ID는 필수입니다.")
    private String orderId;

    @Schema(description = "상품 ID", example = "product-001", required = true)
    @NotBlank(message = "상품 ID는 필수입니다.")
    private String productId;

    @Schema(description = "수량", example = "10", required = true)
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
    private Integer quantity;

    @Schema(description = "창고 ID", example = "warehouse-001", required = true)
    @NotBlank(message = "창고 ID는 필수입니다.")
    private String warehouseId;

    @Schema(description = "설명", example = "재고 예약")
    private String description;
}
