package org.ever._4ever_be_scm.scm.mm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequisitionListResponseDto {
    private String purchaseRequisitionId;
    private String purchaseRequisitionNumber;
    private String requesterId;
    private String requesterName;
    private String departmentId;
    private String departmentName;
    private String statusCode;
    private LocalDateTime requestDate;
    private String createdBy;
    private BigDecimal totalAmount;
}
