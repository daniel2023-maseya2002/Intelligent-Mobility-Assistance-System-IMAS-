package IMAS.ImasProject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "routes", indexes = {
        @Index(name = "idx_route_code", columnList = "routeCode"),
        @Index(name = "idx_route_name", columnList = "routeName")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Route name cannot be blank")
    @Size(max = 100, message = "Route name must not exceed 100 characters")
    @Column(name = "route_name", nullable = false, length = 100)
    private String routeName;

    @NotBlank(message = "Route code cannot be blank")
    @Size(max = 20, message = "Route code must not exceed 20 characters")
    @Column(name = "route_code", nullable = false, unique = true, length = 20)
    private String routeCode;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @DecimalMin(value = "0.0", message = "Total distance must be positive")
    @Column(name = "total_distance")
    private Double totalDistance;

    @Min(value = 0, message = "Estimated duration must be positive")
    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // in minutes

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hex color")
    @Column(name = "color", length = 7)
    private String color; // For map display

    @Enumerated(EnumType.STRING)
    @Column(name = "route_type")
    private RouteType routeType;

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

    // Relationships
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Stop> stops = new ArrayList<>();

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Vehicle> vehicles = new ArrayList<>();

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Schedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "route", fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<PredictionResult> predictionResults = new ArrayList<>();

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
    public void addStop(Stop stop) {
        stops.add(stop);
        stop.setRoute(this);
    }

    public void removeStop(Stop stop) {
        stops.remove(stop);
        stop.setRoute(null);
    }

    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        vehicle.setRoute(this);
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
        vehicle.setRoute(null);
    }

    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
        schedule.setRoute(this);
    }

    public void removeSchedule(Schedule schedule) {
        schedules.remove(schedule);
        schedule.setRoute(null);
    }

    // Add getName() method that was missing
    public String getName() {
        return this.routeName;
    }

    public int getStopCount() {
        return stops != null ? stops.size() : 0;
    }

    public int getActiveVehicleCount() {
        return vehicles != null ?
                (int) vehicles.stream()
                        .filter(v -> v.getStatus() == VehicleStatus.ACTIVE)
                        .count() : 0;
    }

    public double calculateAverageSpeed() {
        if (totalDistance == null || estimatedDuration == null || estimatedDuration == 0) {
            return 0.0;
        }
        return (totalDistance / estimatedDuration) * 60; // km/h
    }

    public boolean hasActiveSchedules() {
        return schedules != null &&
                schedules.stream().anyMatch(s ->
                        s.getStatus() == Schedule.Status.SCHEDULED ||
                                s.getStatus() == Schedule.Status.CONFIRMED ||
                                s.getStatus() == Schedule.Status.IN_PROGRESS);
    }

    public Stop getFirstStop() {
        return stops != null && !stops.isEmpty() ?
                stops.stream()
                        .min((s1, s2) -> Integer.compare(s1.getSequenceOrder(), s2.getSequenceOrder()))
                        .orElse(null) : null;
    }

    public Stop getLastStop() {
        return stops != null && !stops.isEmpty() ?
                stops.stream()
                        .max((s1, s2) -> Integer.compare(s1.getSequenceOrder(), s2.getSequenceOrder()))
                        .orElse(null) : null;
    }

    public List<Stop> getStopsInOrder() {
        if (stops == null) return new ArrayList<>();
        return stops.stream()
                .sorted((s1, s2) -> Integer.compare(s1.getSequenceOrder(), s2.getSequenceOrder()))
                .toList();
    }

    public boolean isOperational() {
        return isActive && hasActiveSchedules() && getActiveVehicleCount() > 0;
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("Route{id=%d, name='%s', code='%s', active=%s}",
                id, routeName, routeCode, isActive);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Route route = (Route) obj;
        return routeCode != null ? routeCode.equals(route.routeCode) : route.routeCode == null;
    }

    @Override
    public int hashCode() {
        return routeCode != null ? routeCode.hashCode() : 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public void setRouteType(RouteType routeType) {
        this.routeType = routeType;
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

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public List<PredictionResult> getPredictionResults() {
        return predictionResults;
    }

    public void setPredictionResults(List<PredictionResult> predictionResults) {
        this.predictionResults = predictionResults;
    }
}