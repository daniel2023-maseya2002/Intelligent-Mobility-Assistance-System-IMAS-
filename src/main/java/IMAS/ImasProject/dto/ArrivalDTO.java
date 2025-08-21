package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.ArrivalStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArrivalDTO {
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledArrival;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualArrival;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedArrival;

    private Integer delay;
    private ArrivalStatus status;
    private Integer boardingPassengers;
    private Integer alightingPassengers;
    private Boolean isCancelled;
    private String cancellationReason;
    private String weatherCondition;
    private String trafficCondition;
    private Double distanceFromStop;
    private Boolean isActive;

    // Related entity IDs and basic info
    private Long stopId;
    private String stopName;
    private Long vehicleId;
    private String vehicleNumber;

    // Calculated fields
    private String delayDescription;
    private String statusDescription;
    private Integer netPassengerChange;
    private Long minutesUntilScheduled;
    private Long minutesUntilEstimated;
    private LocalDateTime expectedArrival;
    private Boolean isDelayed;
    private Boolean isEarly;
    private Boolean isOnTime;
    private Boolean hasArrived;
    private Boolean isUpcoming;
    private Boolean isPast;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;
}
