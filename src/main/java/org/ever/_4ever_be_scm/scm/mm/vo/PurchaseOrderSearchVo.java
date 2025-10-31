package org.ever._4ever_be_scm.scm.mm.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderSearchVo {
    private String statusCode;
    private String type;
    private String keyword;
    private LocalDate startDate;
    private LocalDate endDate;
    private int page;
    private int size;
}
