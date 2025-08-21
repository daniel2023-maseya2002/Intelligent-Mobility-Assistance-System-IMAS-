package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.ArrivalStatus;
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
public class UpdateArrivalDTO {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledArrival;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualArrival;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime estimatedArrival;

    private ArrivalStatus status;

    @Min(value = 0, message = "Passenger count must be non-negative")
    private Integer boardingPassengers;

    @Min(value = 0, message = "Passenger count must be non-negative")
    private Integer alightingPassengers;

    private String weatherCondition;
    private String trafficCondition;

    @DecimalMin(value = "0.0", message = "Distance must be positive")
    private Double distanceFromStop;

    private String updatedBy;
}
