package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleGPSTrackingDTO {

    private Long vehicleId;
    private String vehicleNumber;
    private String licensePlate;
    private VehicleStatus status;
    private VehicleLocationDTO currentLocation;
    private Double currentSpeed;
    private Integer currentPassengerCount;
    private Double occupancyRate;
    private Boolean isMoving;
    private LocalDateTime lastLocationUpdate;
    private String routeName;
    private Long routeId;

    // Explicit setter methods (Lombok @Data should generate these, but adding them explicitly for clarity)
    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public void setCurrentLocation(VehicleLocationDTO currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setCurrentSpeed(Double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public void setCurrentPassengerCount(Integer currentPassengerCount) {
        this.currentPassengerCount = currentPassengerCount;
    }

    public void setOccupancyRate(Double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public void setIsMoving(Boolean isMoving) {
        this.isMoving = isMoving;
    }

    public void setLastLocationUpdate(LocalDateTime lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    // Explicit getter methods (Lombok @Data should generate these, but adding them for clarity)
    public Long getVehicleId() {
        return vehicleId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public VehicleLocationDTO getCurrentLocation() {
        return currentLocation;
    }

    public Double getCurrentSpeed() {
        return currentSpeed;
    }

    public Integer getCurrentPassengerCount() {
        return currentPassengerCount;
    }

    public Double getOccupancyRate() {
        return occupancyRate;
    }

    public Boolean getIsMoving() {
        return isMoving;
    }

    public LocalDateTime getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public String getRouteName() {
        return routeName;
    }

    public Long getRouteId() {
        return routeId;
    }

    public Boolean getMoving() {
        return isMoving;
    }

    public void setMoving(Boolean moving) {
        isMoving = moving;
    }
}