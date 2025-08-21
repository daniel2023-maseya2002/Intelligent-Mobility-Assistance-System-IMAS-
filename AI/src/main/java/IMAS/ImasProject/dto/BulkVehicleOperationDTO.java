package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkVehicleOperationDTO {
    private List<Long> vehicleIds;
    private String operation; // UPDATE_STATUS, ASSIGN_ROUTE, REMOVE_ROUTE, ACTIVATE, DEACTIVATE
    private VehicleStatus newStatus;
    private Long newRouteId;
}