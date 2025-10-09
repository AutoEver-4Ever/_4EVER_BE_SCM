package org.ever._4ever_be_scm.scm.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScmRequestVo {

    private String orderId;
    private String productId;
    private Integer quantity;
    private String warehouseId;
    private String description;
}
