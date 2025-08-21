package IMAS.ImasProject.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_assignments")
public class TaskAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", referencedColumnName = "taskId", nullable = false)
    private MaintenanceTask task;

    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Staff technician;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    private LocalDateTime assignedAt;
    private LocalDateTime respondedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    private AssignmentMethod assignmentMethod;

    public enum AssignmentStatus {
        PENDING_ACCEPTANCE,
        ACCEPTED,
        REJECTED,
        IN_PROGRESS,
        COMPLETED
    }

    public enum AssignmentMethod {
        AUTOMATIC,
        MANUAL
    }

    // Constructors
    public TaskAssignment() {}

    public TaskAssignment(MaintenanceTask task, Staff technician, AssignmentMethod method) {
        this.task = task;
        this.technician = technician;
        this.assignmentMethod = method;
        this.status = AssignmentStatus.PENDING_ACCEPTANCE;
        this.assignedAt = LocalDateTime.now();
    }

    // Propriétés de convenance pour les requêtes Spring Data
    public String getTaskId() {
        return task != null ? task.getTaskId() : null;
    }

    public Long getTechnicianId() {
        return technician != null ? technician.getId() : null;
    }

    // Getters and setters standards
    public Long getId() {
        return id;
    }

    public MaintenanceTask getTask() {
        return task;
    }

    public void setTask(MaintenanceTask task) {
        this.task = task;
    }

    public Staff getTechnician() {
        return technician;
    }

    public void setTechnician(Staff technician) {
        this.technician = technician;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public AssignmentMethod getAssignmentMethod() {
        return assignmentMethod;
    }

    public void setAssignmentMethod(AssignmentMethod assignmentMethod) {
        this.assignmentMethod = assignmentMethod;
    }
}