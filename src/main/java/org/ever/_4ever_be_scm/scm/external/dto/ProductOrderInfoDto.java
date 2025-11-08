package org.ever._4ever_be_scm.scm.external.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductOrderInfoDto {
    private String productOrderId;
    private String productOrderNumber;
    private BigDecimal totalAmount;
}
