package org.ever._4ever_be_scm.scm.pp.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardWorkflowItemDto {
    private String itemId;
    private String itemTitle;
    private String itemNumber;
    private String name;
    private String statusCode;
    private String date;
}
