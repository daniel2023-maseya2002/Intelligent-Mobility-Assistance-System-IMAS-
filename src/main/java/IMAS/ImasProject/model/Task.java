package IMAS.ImasProject.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "task_id", updatable = false, nullable = false)
    private String taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id", nullable = false, referencedColumnName = "id")
    private Staff technician;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(nullable = false)
    private int progress = 0; // 0-100

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "progress_notes", length = 1000)
    private String progressNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructeurs
    public Task() {}

    public Task(String taskId, Incident incident, Staff technician, String description,
                Priority priority, LocalDateTime deadline, int progress, Status status,
                String progressNotes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.taskId = taskId;
        this.incident = incident;
        this.technician = technician;
        this.description = description;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.deadline = deadline;
        this.progress = progress;
        this.status = status != null ? status : Status.PENDING;
        this.progressNotes = progressNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Builder Pattern - Implémentation manuelle
    public static TaskBuilder builder() {
        return new TaskBuilder();
    }

    public static class TaskBuilder {
        private String taskId;
        private Incident incident;
        private Staff technician;
        private String description;
        private Priority priority = Priority.MEDIUM;
        private LocalDateTime deadline;
        private int progress = 0;
        private Status status = Status.PENDING;
        private String progressNotes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public TaskBuilder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public TaskBuilder incident(Incident incident) {
            this.incident = incident;
            return this;
        }

        public TaskBuilder technician(Staff technician) {
            this.technician = technician;
            return this;
        }

        public TaskBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TaskBuilder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public TaskBuilder deadline(LocalDateTime deadline) {
            this.deadline = deadline;
            return this;
        }

        public TaskBuilder progress(int progress) {
            this.progress = progress;
            return this;
        }

        public TaskBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public TaskBuilder progressNotes(String progressNotes) {
            this.progressNotes = progressNotes;
            return this;
        }

        public TaskBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TaskBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Task build() {
            return new Task(taskId, incident, technician, description, priority,
                    deadline, progress, status, progressNotes, createdAt, updatedAt);
        }
    }

    public enum Priority {
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low");

        private final String displayName;

        Priority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Status {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (progress == 0) {
            progress = 0;
        }
        if (status == null) {
            status = Status.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters
    public String getTaskId() {
        return taskId;
    }

    public Incident getIncident() {
        return incident;
    }

    public Staff getTechnician() {
        return technician;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public int getProgress() {
        return progress;
    }

    public Status getStatus() {
        return status;
    }

    public String getProgressNotes() {
        return progressNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public void setTechnician(Staff technician) {
        this.technician = technician;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public void setProgress(int progress) {
        updateProgress(progress);
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void setProgressNotes(String progressNotes) {
        this.progressNotes = progressNotes;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Méthodes utilitaires
    public boolean isCompleted() {
        return this.status == Status.COMPLETED;
    }

    public boolean isOverdue() {
        return this.deadline != null &&
                LocalDateTime.now().isAfter(this.deadline) &&
                !isCompleted();
    }

    public boolean isInProgress() {
        return this.status == Status.IN_PROGRESS;
    }

    public boolean isPending() {
        return this.status == Status.PENDING;
    }

    public boolean isCancelled() {
        return this.status == Status.CANCELLED;
    }

    public void markAsCompleted() {
        this.status = Status.COMPLETED;
        this.progress = 100;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsInProgress() {
        this.status = Status.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = Status.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProgress(int newProgress) {
        if (newProgress < 0) {
            this.progress = 0;
        } else if (newProgress > 100) {
            this.progress = 100;
            this.status = Status.COMPLETED;
        } else {
            this.progress = newProgress;
            if (newProgress == 100) {
                this.status = Status.COMPLETED;
            } else if (newProgress > 0 && this.status == Status.PENDING) {
                this.status = Status.IN_PROGRESS;
            }
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void addProgressNote(String note) {
        if (note != null && !note.trim().isEmpty()) {
            if (this.progressNotes == null || this.progressNotes.trim().isEmpty()) {
                this.progressNotes = note;
            } else {
                this.progressNotes += "\n" + note;
            }
            this.updatedAt = LocalDateTime.now();
        }
    }

    public Long getDurationInHours() {
        if (this.createdAt != null && this.updatedAt != null) {
            return java.time.Duration.between(this.createdAt, this.updatedAt).toHours();
        }
        return null;
    }

    public Long getTimeToDeadlineInHours() {
        if (this.deadline != null) {
            return java.time.Duration.between(LocalDateTime.now(), this.deadline).toHours();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId='" + taskId + '\'' +
                ", description='" + (description != null ? description.substring(0, Math.min(description.length(), 30)) + "..." : "null") + '\'' +
                ", priority=" + priority +
                ", deadline=" + deadline +
                ", progress=" + progress +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", incidentId=" + (incident != null ? incident.getIncidentId() : "null") +
                ", technicianId=" + (technician != null ? technician.getId() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskId != null && taskId.equals(task.taskId);
    }

    @Override
    public int hashCode() {
        return taskId != null ? taskId.hashCode() : 0;
    }
}