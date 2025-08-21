package IMAS.ImasProject.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleStatisticsDTO {
    private long totalVehicles;
    private long activeVehicles;
    private long inactiveVehicles;
    private long inTransitVehicles;
    private long maintenanceVehicles;
    private long breakdownVehicles;
    private double utilizationRate;
}
