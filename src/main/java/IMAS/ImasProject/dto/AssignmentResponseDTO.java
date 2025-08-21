package IMAS.ImasProject.dto;


import IMAS.ImasProject.model.MaintenanceTask;
import IMAS.ImasProject.model.TaskAssignment;

public class AssignmentResponseDTO {
    private Long assignmentId;
    private String taskId;
    private Long technicianId;
    private TaskAssignment.AssignmentStatus status;
    private String rejectionReason;
    private MaintenanceTask taskDetails;

    // Constructors, getters and setters
    public AssignmentResponseDTO() {}

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public TaskAssignment.AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(TaskAssignment.AssignmentStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public MaintenanceTask getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(MaintenanceTask taskDetails) {
        this.taskDetails = taskDetails;
    }
}