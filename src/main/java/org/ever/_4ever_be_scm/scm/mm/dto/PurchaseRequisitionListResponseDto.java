package org.ever._4ever_be_scm.scm.mm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime requestDate;
    private BigDecimal totalAmount;
}
