package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MesDetailResponseDto {
    private String mesId;
    private String mesNumber;
    private String productId;
    private String productName;
    private Integer quantity;
    private String uomName;
    private Integer progressPercent;
    private String statusCode;
    private PlanDto plan;
    private String currentOperation;
    private List<OperationDto> operations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanDto {
        private LocalDate startDate;
        private LocalDate dueDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OperationDto {
        private String operationNumber;
        private String operationName;
        private Integer sequence;
        private String statusCode;
        private String startedAt;  // "09:00" 형식
        private String finishedAt;
        private Double durationHours;
        private ManagerDto manager;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManagerDto {
        private String id;
        private String name;
    }
}
