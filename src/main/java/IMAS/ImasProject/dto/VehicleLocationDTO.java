package IMAS.ImasProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleLocationDTO {
    private Long id;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private Double heading;
    private Integer passengerCount;
    private LocalDateTime timestamp;
}