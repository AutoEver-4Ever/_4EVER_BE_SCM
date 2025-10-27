package org.ever._4ever_be_scm.scm.mm.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierSearchVo {
    private String statusCode;
    private String category;
    private int page;
    private int size;
}
