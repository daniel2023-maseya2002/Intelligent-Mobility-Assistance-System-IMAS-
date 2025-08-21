package IMAS.ImasProject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Data
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", referencedColumnName = "taskId", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MaintenanceTask task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "technician_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "resetToken", "resetTokenExpiration"})
    private Staff technician;

    // Add route relationship to match the Route entity expectations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    @JsonBackReference
    private Route route;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type")
    private ScheduleType scheduleType = ScheduleType.REGULAR;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type")
    private RecurrenceType recurrenceType;

    @Column(name = "recurrence_end_date")
    private LocalDateTime recurrenceEndDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        SCHEDULED, CONFIRMED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED
    }

    public enum ScheduleType {
        REGULAR(1.0),
        EXPRESS(1.5),
        NIGHT(1.2),
        WEEKEND(1.3),
        HOLIDAY(2.0);

        private final double fareMultiplier;

        ScheduleType(double fareMultiplier) {
            this.fareMultiplier = fareMultiplier;
        }

        public double getFareMultiplier() {
            return fareMultiplier;
        }
    }

    public enum RecurrenceType {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    // Default no-argument constructor (replaces both @NoArgsConstructor and manual constructor)
    public Schedule() {
        this.priority = Priority.MEDIUM;
        this.status = Status.SCHEDULED;
        this.scheduleType = ScheduleType.REGULAR;
        this.isActive = true;
        this.isRecurring = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor with essential fields
    public Schedule(String taskId, Staff technician, LocalDateTime startTime, LocalDateTime endTime) {
        this();
        this.taskId = taskId;
        this.technician = technician;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Constructor with all main fields
    public Schedule(String taskId, Staff technician, Route route, LocalDateTime startTime,
                    LocalDateTime endTime, Priority priority, Status status) {
        this();
        this.taskId = taskId;
        this.technician = technician;
        this.route = route;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = status != null ? status : Status.SCHEDULED;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (priority == null) {
            priority = Priority.MEDIUM;
        }
        if (status == null) {
            status = Status.SCHEDULED;
        }
        if (scheduleType == null) {
            scheduleType = ScheduleType.REGULAR;
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isRecurring == null) {
            isRecurring = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean isActive() {
        return status == Status.SCHEDULED || status == Status.CONFIRMED || status == Status.IN_PROGRESS;
    }

    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }

    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    public boolean isOverdue() {
        return endTime != null && LocalDateTime.now().isAfter(endTime) && !isCompleted();
    }

    public boolean isUpcoming() {
        return startTime != null && LocalDateTime.now().isBefore(startTime);
    }

    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return startTime != null && endTime != null &&
                !now.isBefore(startTime) && !now.isAfter(endTime) &&
                (status == Status.IN_PROGRESS || status == Status.CONFIRMED);
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = Status.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = Status.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void start() {
        this.status = Status.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void pause() {
        this.status = Status.ON_HOLD;
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        this.status = Status.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    public double getFareMultiplier() {
        return scheduleType != null ? scheduleType.getFareMultiplier() : 1.0;
    }

    @Override
    public String toString() {
        return String.format("Schedule{id=%d, taskId='%s', status=%s, startTime=%s, endTime=%s}",
                id, taskId, status, startTime, endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return id != null && id.equals(schedule.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}