package IMAS.ImasProject.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArrivalAnalyticsDTO {
    private Long totalArrivals;
    private Long onTimeArrivals;
    private Long delayedArrivals;
    private Long earlyArrivals;
    private Long cancelledArrivals;
    private Double averageDelay;
    private Double onTimePercentage;
    private Double delayedPercentage;
    private Integer maxDelay;
    private Integer minDelay;
    private Map<String, Long> statusDistribution;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
}