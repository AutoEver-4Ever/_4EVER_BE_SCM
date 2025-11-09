package org.ever._4ever_be_scm.scm.pp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PpStatisticsResponseDto {
    private PpStatisticPeriodDto week;
    private PpStatisticPeriodDto month;
    private PpStatisticPeriodDto quarter;
    private PpStatisticPeriodDto year;
}
