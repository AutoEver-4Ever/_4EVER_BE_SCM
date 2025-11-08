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

    // 버튼 활성화 제어
    private Boolean canStartMes;      // MES 시작 버튼 활성화 여부
    private Boolean canCompleteMes;   // MES 완료 버튼 활성화 여부

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
        private String mesOperationLogId;  // MesOperationLog의 ID (공정 시작/종료 시 사용)
        private String operationNumber;
        private String operationName;
        private Integer sequence;
        private String statusCode;
        private String startedAt;  // "09:00" 형식
        private String finishedAt;
        private Double durationHours;
        private ManagerDto manager;

        // 버튼 활성화 제어
        private Boolean canStart;      // 공정 시작 버튼 활성화 여부
        private Boolean canComplete;   // 공정 완료 버튼 활성화 여부
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
