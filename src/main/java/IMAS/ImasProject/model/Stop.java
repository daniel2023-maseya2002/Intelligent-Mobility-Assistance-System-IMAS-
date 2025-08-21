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
@Table(name = "stops", indexes = {
        @Index(name = "idx_stop_name", columnList = "stopName"),
        @Index(name = "idx_stop_code", columnList = "stopCode"),
        @Index(name = "idx_stop_route", columnList = "route_id"),
        @Index(name = "idx_stop_coordinates", columnList = "latitude, longitude")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Stop name cannot be blank")
    @Size(max = 100, message = "Stop name must not exceed 100 characters")
    @Column(name = "stop_name", nullable = false, length = 100)
    private String stopName;

    @NotBlank(message = "Stop code cannot be blank")
    @Size(max = 20, message = "Stop code must not exceed 20 characters")
    @Column(name = "stop_code", nullable = false, unique = true, length = 20)
    private String stopCode;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Column(name = "latitude", nullable = false, columnDefinition = "DECIMAL(10,8)")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Column(name = "longitude", nullable = false, columnDefinition = "DECIMAL(11,8)")
    private Double longitude;

    @NotNull(message = "Sequence order is required")
    @Min(value = 1, message = "Sequence order must be at least 1")
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "has_shelter")
    @Builder.Default
    private Boolean hasShelter = false;

    @Column(name = "is_accessible")
    @Builder.Default
    private Boolean isAccessible = false;

    @Size(max = 100, message = "Zone must not exceed 100 characters")
    @Column(name = "zone", length = 100)
    private String zone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @JsonBackReference
    private Route route;

    @OneToMany(mappedBy = "stop", fetch = FetchType.LAZY)
    @JsonManagedReference
    @Builder.Default
    private List<Arrival> arrivals = new ArrayList<>();

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

    // Explicit getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getStopCode() {
        return stopCode;
    }

    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public List<Arrival> getArrivals() {
        return arrivals;
    }

    public void setArrivals(List<Arrival> arrivals) {
        this.arrivals = arrivals;
    }

    // Business methods
    public void addArrival(Arrival arrival) {
        arrivals.add(arrival);
        arrival.setStop(this);
    }

    public void removeArrival(Arrival arrival) {
        arrivals.remove(arrival);
        arrival.setStop(null);
    }

    public double distanceTo(Stop other) {
        if (other == null) return 0.0;

        double lat1Rad = Math.toRadians(this.latitude);
        double lon1Rad = Math.toRadians(this.longitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double lon2Rad = Math.toRadians(other.longitude);

        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double earthRadius = 6371.0; // Earth's radius in kilometers
        return earthRadius * c;
    }

    public boolean isValidCoordinate() {
        return latitude != null && longitude != null &&
                latitude >= -90.0 && latitude <= 90.0 &&
                longitude >= -180.0 && longitude <= 180.0;
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
        return String.format("Stop{id=%d, name='%s', code='%s', order=%d}",
                id, stopName, stopCode, sequenceOrder);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Stop stop = (Stop) obj;
        return stopCode != null ? stopCode.equals(stop.stopCode) : stop.stopCode == null;
    }

    @Override
    public int hashCode() {
        return stopCode != null ? stopCode.hashCode() : 0;
    }
}