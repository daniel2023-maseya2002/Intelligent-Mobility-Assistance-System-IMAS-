package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.ArrivalStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArrivalSearchCriteria {
    private Long stopId;
    private Long vehicleId;
    private List<ArrivalStatus> statuses;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledFrom;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTo;

    private Boolean isCancelled;
    private Integer minDelay;
    private Integer maxDelay;
    private Double maxDistanceFromStop;
    private Boolean isActive;

    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "scheduledArrival";
    private String sortDirection = "ASC";
}
