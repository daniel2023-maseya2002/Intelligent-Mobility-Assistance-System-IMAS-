// VehicleResponseDTO.java
package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.FuelType;
import IMAS.ImasProject.model.VehicleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponseDTO {

    private Long id;
    private String vehicleNumber;
    private String licensePlate;
    private Integer capacity;
    private String vehicleType;
    private Boolean isAccessible;
    private VehicleStatus status;
    private String manufacturer;
    private String model;
    private Integer year;
    private Double fuelCapacity;
    private FuelType fuelType;
    private Boolean hasAirConditioning;
    private Boolean hasWifi;
    private Boolean hasGps;
    private LocalDateTime lastMaintenance;
    private LocalDateTime nextMaintenance;
    private Double odometerReading;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Route information
    private Long routeId;
    private String routeName;

    // Current location and status
    private VehicleLocationDTO currentLocation;
    private Double currentSpeed;
    private Integer currentPassengerCount;
    private Double occupancyRate;
    private Boolean isMoving;
    private Boolean isOperational;
    private Boolean isMaintenanceRequired;
    private Long daysSinceLastMaintenance;

    // Features list
    private List<String> features;

    // Vehicle info string
    private String vehicleInfo;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Boolean getIsAccessible() {
        return isAccessible;
    }

    public void setIsAccessible(Boolean accessible) {
        isAccessible = accessible;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getFuelCapacity() {
        return fuelCapacity;
    }

    public void setFuelCapacity(Double fuelCapacity) {
        this.fuelCapacity = fuelCapacity;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Boolean getHasAirConditioning() {
        return hasAirConditioning;
    }

    public void setHasAirConditioning(Boolean hasAirConditioning) {
        this.hasAirConditioning = hasAirConditioning;
    }

    public Boolean getHasWifi() {
        return hasWifi;
    }

    public void setHasWifi(Boolean hasWifi) {
        this.hasWifi = hasWifi;
    }

    public Boolean getHasGps() {
        return hasGps;
    }

    public void setHasGps(Boolean hasGps) {
        this.hasGps = hasGps;
    }

    public LocalDateTime getLastMaintenance() {
        return lastMaintenance;
    }

    public void setLastMaintenance(LocalDateTime lastMaintenance) {
        this.lastMaintenance = lastMaintenance;
    }

    public LocalDateTime getNextMaintenance() {
        return nextMaintenance;
    }

    public void setNextMaintenance(LocalDateTime nextMaintenance) {
        this.nextMaintenance = nextMaintenance;
    }

    public Double getOdometerReading() {
        return odometerReading;
    }

    public void setOdometerReading(Double odometerReading) {
        this.odometerReading = odometerReading;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public VehicleLocationDTO getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(VehicleLocationDTO currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(Double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public Integer getCurrentPassengerCount() {
        return currentPassengerCount;
    }

    public void setCurrentPassengerCount(Integer currentPassengerCount) {
        this.currentPassengerCount = currentPassengerCount;
    }

    public Double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(Double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public Boolean getIsMoving() {
        return isMoving;
    }

    public void setIsMoving(Boolean moving) {
        isMoving = moving;
    }

    public Boolean getIsOperational() {
        return isOperational;
    }

    public void setIsOperational(Boolean operational) {
        isOperational = operational;
    }

    public Boolean getIsMaintenanceRequired() {
        return isMaintenanceRequired;
    }

    public void setIsMaintenanceRequired(Boolean maintenanceRequired) {
        isMaintenanceRequired = maintenanceRequired;
    }

    public Long getDaysSinceLastMaintenance() {
        return daysSinceLastMaintenance;
    }

    public void setDaysSinceLastMaintenance(Long daysSinceLastMaintenance) {
        this.daysSinceLastMaintenance = daysSinceLastMaintenance;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }
}