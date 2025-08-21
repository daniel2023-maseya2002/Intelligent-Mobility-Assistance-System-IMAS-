package IMAS.ImasProject.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateArrivalDTO {

    @NotNull(message = "Scheduled arrival time is required")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledArrival;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedArrival;

    @Min(value = 0, message = "Passenger count must be non-negative")
    private Integer boardingPassengers;

    @Min(value = 0, message = "Passenger count must be non-negative")
    private Integer alightingPassengers;

    private String weatherCondition;
    private String trafficCondition;

    @DecimalMin(value = "0.0", message = "Distance must be positive")
    private Double distanceFromStop;

    @NotNull(message = "Stop ID is required")
    private Long stopId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    private String createdBy;
}