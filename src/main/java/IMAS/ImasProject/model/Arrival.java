package IMAS.ImasProject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "arrivals", indexes = {
        @Index(name = "idx_arrival_stop", columnList = "stop_id"),
        @Index(name = "idx_arrival_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_arrival_scheduled", columnList = "scheduled_arrival"),
        @Index(name = "idx_arrival_status", columnList = "status"),
        @Index(name = "idx_arrival_date", columnList = "scheduled_arrival, stop_id"),
        @Index(name = "idx_arrival_delay", columnList = "delay")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Arrival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Scheduled arrival time is required")
    @Column(name = "scheduled_arrival", nullable = false)
    private LocalDateTime scheduledArrival;

    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    @Column(name = "delay")
    private Integer delay;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ArrivalStatus status = ArrivalStatus.SCHEDULED;

    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

    @Min(value = 0, message = "Passenger count must be non-negative")
    @Column(name = "boarding_passengers")
    private Integer boardingPassengers;

    @Min(value = 0, message = "Passenger count must be non-negative")
    @Column(name = "alighting_passengers")
    private Integer alightingPassengers;

    @Column(name = "is_cancelled")
    @Builder.Default
    private Boolean isCancelled = false;

    @Size(max = 255, message = "Cancellation reason must not exceed 255 characters")
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "weather_condition")
    private String weatherCondition;

    @Column(name = "traffic_condition")
    private String trafficCondition;

    @DecimalMin(value = "0.0", message = "Distance must be positive")
    @Column(name = "distance_from_stop")
    private Double distanceFromStop;

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
    @JoinColumn(name = "stop_id", nullable = false)
    @JsonBackReference
    @NotNull(message = "Stop is required")
    private Stop stop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonBackReference
    @NotNull(message = "Vehicle is required")
    private Vehicle vehicle;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (actualArrival != null && scheduledArrival != null) {
            calculateDelay();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (actualArrival != null && scheduledArrival != null) {
            calculateDelay();
        }
    }

    public LocalDateTime getScheduledArrival() {
        return scheduledArrival;
    }

    public ArrivalStatus getStatus() {
        return status;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void recordActualArrival(LocalDateTime arrivalTime) {
        this.actualArrival = arrivalTime;
        calculateDelay();
        updateStatus();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEstimatedArrival(LocalDateTime estimatedTime) {
        this.estimatedArrival = estimatedTime;
        if (estimatedTime != null && scheduledArrival != null) {
            long estimatedDelay = ChronoUnit.MINUTES.between(scheduledArrival, estimatedTime);
            if (estimatedDelay > 0) {
                this.status = ArrivalStatus.DELAYED;
            } else if (estimatedDelay < -2) {
                this.status = ArrivalStatus.EARLY;
            } else {
                this.status = ArrivalStatus.ON_TIME;
            }
        }
        this.updatedAt = LocalDateTime.now();
    }

    private void calculateDelay() {
        if (actualArrival != null && scheduledArrival != null) {
            this.delay = (int) ChronoUnit.MINUTES.between(scheduledArrival, actualArrival);
        }
    }

    private void updateStatus() {
        if (actualArrival == null) return;

        if (isCancelled) {
            this.status = ArrivalStatus.CANCELLED;
            return;
        }

        if (delay == null) {
            calculateDelay();
        }

        if (delay != null) {
            if (delay > 5) {
                this.status = ArrivalStatus.DELAYED;
            } else if (delay < -2) {
                this.status = ArrivalStatus.EARLY;
            } else {
                this.status = ArrivalStatus.ON_TIME;
            }
        }

        this.status = ArrivalStatus.ARRIVED;
    }

    public void cancel(String reason) {
        this.isCancelled = true;
        this.cancellationReason = reason;
        this.status = ArrivalStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void uncancel() {
        this.isCancelled = false;
        this.cancellationReason = null;
        this.status = ArrivalStatus.SCHEDULED;
        this.updatedAt = LocalDateTime.now();
    }

    public void recordPassengerActivity(Integer boarding, Integer alighting) {
        this.boardingPassengers = boarding;
        this.alightingPassengers = alighting;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDistanceFromStop(Double distance) {
        this.distanceFromStop = distance;

        if (distance != null) {
            if (distance <= 50) {
                this.status = ArrivalStatus.APPROACHING;
            } else if (distance <= 200) {
                this.status = ArrivalStatus.NEARBY;
            }
        }
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isDelayed() {
        return delay != null && delay > 5;
    }

    public boolean isEarly() {
        return delay != null && delay < -2;
    }

    public boolean isOnTime() {
        return delay != null && delay >= -2 && delay <= 5;
    }

    public boolean hasArrived() {
        return actualArrival != null;
    }

    public boolean isUpcoming() {
        return scheduledArrival != null && scheduledArrival.isAfter(LocalDateTime.now()) && !isCancelled;
    }

    public boolean isPast() {
        return scheduledArrival != null && scheduledArrival.isBefore(LocalDateTime.now());
    }

    public long getMinutesUntilScheduled() {
        if (scheduledArrival == null) return 0;
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), scheduledArrival);
    }

    public long getMinutesUntilEstimated() {
        if (estimatedArrival == null) return getMinutesUntilScheduled();
        return ChronoUnit.MINUTES.between(LocalDateTime.now(), estimatedArrival);
    }

    public LocalDateTime getExpectedArrival() {
        return estimatedArrival != null ? estimatedArrival : scheduledArrival;
    }

    public String getDelayDescription() {
        if (delay == null) return "No delay information";

        if (delay == 0) return "On time";
        if (delay > 0) return delay + " minutes late";
        return Math.abs(delay) + " minutes early";
    }

    public String getStatusDescription() {
        if (status == null) return "Unknown";

        switch (status) {
            case SCHEDULED:
                return "Scheduled";
            case ON_TIME:
                return "On Time";
            case DELAYED:
                return "Delayed" + (delay != null ? " (" + delay + " min)" : "");
            case EARLY:
                return "Early" + (delay != null ? " (" + Math.abs(delay) + " min)" : "");
            case APPROACHING:
                return "Approaching";
            case NEARBY:
                return "Nearby";
            case ARRIVED:
                return "Arrived";
            case CANCELLED:
                return "Cancelled";
            default:
                return status.toString();
        }
    }

    public Integer getNetPassengerChange() {
        if (boardingPassengers == null && alightingPassengers == null) return null;

        int boarding = boardingPassengers != null ? boardingPassengers : 0;
        int alighting = alightingPassengers != null ? alightingPassengers : 0;

        return boarding - alighting;
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
        return String.format("Arrival{id=%d, stop=%s, vehicle=%s, scheduled=%s, status=%s}",
                id,
                stop != null ? (stop.getStopName() != null ? stop.getStopName() : "Stop-" + stop.getId()) : "null",
                vehicle != null ? (vehicle.getVehicleNumber() != null ? vehicle.getVehicleNumber() : "Vehicle-" + vehicle.getId()) : "null",
                scheduledArrival,
                status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Arrival arrival = (Arrival) obj;
        return id != null && id.equals(arrival.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}