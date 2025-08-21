package IMAS.ImasProject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "vehicles", indexes = {
        @Index(name = "idx_vehicle_number", columnList = "vehicleNumber"),
        @Index(name = "idx_license_plate", columnList = "licensePlate"),
        @Index(name = "idx_vehicle_route", columnList = "route_id"),
        @Index(name = "idx_vehicle_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Vehicle number cannot be blank")
    @Size(max = 50, message = "Vehicle number must not exceed 50 characters")
    @Column(name = "vehicle_number", nullable = false, unique = true, length = 50)
    private String vehicleNumber;

    @NotBlank(message = "License plate cannot be blank")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 200, message = "Capacity cannot exceed 200")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @NotBlank(message = "Vehicle type cannot be blank")
    @Size(max = 50, message = "Vehicle type must not exceed 50 characters")
    @Column(name = "vehicle_type", nullable = false, length = 50)
    private String vehicleType;

    @Column(name = "is_accessible")
    @Builder.Default
    private Boolean isAccessible = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.INACTIVE;

    @Size(max = 50, message = "Manufacturer must not exceed 50 characters")
    @Column(name = "manufacturer", length = 50)
    private String manufacturer;

    @Size(max = 50, message = "Model must not exceed 50 characters")
    @Column(name = "model", length = 50)
    private String model;

    @Min(value = 1900, message = "Year must be valid")
    @Max(value = 2030, message = "Year cannot be in the future")
    @Column(name = "year")
    private Integer year;

    @DecimalMin(value = "0.0", message = "Fuel capacity must be positive")
    @Column(name = "fuel_capacity")
    private Double fuelCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type")
    private FuelType fuelType;

    @Column(name = "has_air_conditioning")
    @Builder.Default
    private Boolean hasAirConditioning = false;

    @Column(name = "has_wifi")
    @Builder.Default
    private Boolean hasWifi = false;

    @Column(name = "has_gps")
    @Builder.Default
    private Boolean hasGps = true;

    @Column(name = "last_maintenance")
    private LocalDateTime lastMaintenance;

    @Column(name = "next_maintenance")
    private LocalDateTime nextMaintenance;

    @DecimalMin(value = "0.0", message = "Odometer reading must be positive")
    @Column(name = "odometer_reading")
    private Double odometerReading;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    @JsonBackReference
    private Route route;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<VehicleLocation> locations = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Arrival> arrivals = new ArrayList<>();

    // Explicit getters and setters for fields referenced in errors
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

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public void addLocation(VehicleLocation location) {
        locations.add(location);
        location.setVehicle(this);
    }

    public void removeLocation(VehicleLocation location) {
        locations.remove(location);
        location.setVehicle(null);
    }

    public void addArrival(Arrival arrival) {
        arrivals.add(arrival);
        arrival.setVehicle(this);
    }

    public void removeArrival(Arrival arrival) {
        arrivals.remove(arrival);
        arrival.setVehicle(null);
    }

    public VehicleLocation getCurrentLocation() {
        if (locations == null || locations.isEmpty()) return null;

        return locations.stream()
                .max((l1, l2) -> l1.getTimestamp().compareTo(l2.getTimestamp()))
                .orElse(null);
    }

    public List<VehicleLocation> getRecentLocations(int limit) {
        if (locations == null) return new ArrayList<>();

        return locations.stream()
                .sorted((l1, l2) -> l2.getTimestamp().compareTo(l1.getTimestamp()))
                .limit(limit)
                .toList();
    }

    public double getCurrentSpeed() {
        VehicleLocation current = getCurrentLocation();
        return current != null ? current.getSpeed() : 0.0;
    }

    public Integer getCurrentPassengerCount() {
        VehicleLocation current = getCurrentLocation();
        return current != null ? current.getPassengerCount() : 0;
    }

    public double getOccupancyRate() {
        Integer currentPassengers = getCurrentPassengerCount();
        return currentPassengers != null && capacity > 0 ?
                (double) currentPassengers / capacity : 0.0;
    }

    public boolean isOverCapacity() {
        Integer currentPassengers = getCurrentPassengerCount();
        return currentPassengers != null && currentPassengers > capacity;
    }

    public boolean isNearCapacity(double threshold) {
        return getOccupancyRate() >= threshold;
    }

    public boolean isMoving() {
        return getCurrentSpeed() > 0;
    }

    public boolean isOperational() {
        return isActive &&
                (status == VehicleStatus.ACTIVE || status == VehicleStatus.IN_TRANSIT) &&
                !isMaintenanceRequired();
    }

    public boolean isMaintenanceRequired() {
        if (nextMaintenance == null) return false;
        return LocalDateTime.now().isAfter(nextMaintenance);
    }

    public long getDaysSinceLastMaintenance() {
        if (lastMaintenance == null) return -1;
        return java.time.temporal.ChronoUnit.DAYS.between(lastMaintenance.toLocalDate(),
                LocalDateTime.now().toLocalDate());
    }

    public void performMaintenance() {
        this.lastMaintenance = LocalDateTime.now();
        this.nextMaintenance = LocalDateTime.now().plusMonths(3);
        this.status = VehicleStatus.MAINTENANCE;
        this.updatedAt = LocalDateTime.now();
    }

    public void completeMaintenance() {
        if (this.status == VehicleStatus.MAINTENANCE) {
            this.status = VehicleStatus.INACTIVE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void assignToRoute(Route newRoute) {
        if (this.route != null) {
            this.route.removeVehicle(this);
        }
        this.route = newRoute;
        if (newRoute != null) {
            newRoute.addVehicle(this);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void startService() {
        if (isOperational()) {
            this.status = VehicleStatus.ACTIVE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void endService() {
        this.status = VehicleStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void startTrip() {
        if (status == VehicleStatus.ACTIVE) {
            this.status = VehicleStatus.IN_TRANSIT;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void endTrip() {
        if (status == VehicleStatus.IN_TRANSIT) {
            this.status = VehicleStatus.ACTIVE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void reportBreakdown() {
        this.status = VehicleStatus.BREAKDOWN;
        this.updatedAt = LocalDateTime.now();
    }

    public void resolveBreakdown() {
        if (this.status == VehicleStatus.BREAKDOWN) {
            this.status = VehicleStatus.INACTIVE;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public String getVehicleInfo() {
        StringBuilder info = new StringBuilder();
        info.append(vehicleNumber);

        if (manufacturer != null && model != null) {
            info.append(" (").append(manufacturer).append(" ").append(model);
            if (year != null) {
                info.append(" ").append(year);
            }
            info.append(")");
        }

        return info.toString();
    }

    public List<String> getFeatures() {
        List<String> features = new ArrayList<>();

        if (isAccessible) features.add("Wheelchair Accessible");
        if (hasAirConditioning) features.add("Air Conditioning");
        if (hasWifi) features.add("WiFi Available");
        if (hasGps) features.add("GPS Tracking");

        return features;
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.status = VehicleStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("Vehicle{id=%d, number='%s', plate='%s', status=%s}",
                id, vehicleNumber, licensePlate, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vehicle vehicle = (Vehicle) obj;
        return vehicleNumber != null ? vehicleNumber.equals(vehicle.vehicleNumber) :
                vehicle.vehicleNumber == null;
    }

    @Override
    public int hashCode() {
        return vehicleNumber != null ? vehicleNumber.hashCode() : 0;
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

    public Boolean getAccessible() {
        return isAccessible;
    }

    public void setAccessible(Boolean accessible) {
        isAccessible = accessible;
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

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
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

    public List<VehicleLocation> getLocations() {
        return locations;
    }

    public void setLocations(List<VehicleLocation> locations) {
        this.locations = locations;
    }

    public List<Arrival> getArrivals() {
        return arrivals;
    }

    public void setArrivals(List<Arrival> arrivals) {
        this.arrivals = arrivals;
    }
}