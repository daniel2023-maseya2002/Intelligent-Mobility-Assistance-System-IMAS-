package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.TechnicianAssignment;

import java.time.LocalDateTime;

public class TechnicianAssignmentDTO {
    private Long id;
    private Long technicianId;
    private String technicianName;
    private String taskDescription;
    private String priority;
    private LocalDateTime deadline;
    private String assignmentType;
    private Long teamId;

    // Constructors
    public TechnicianAssignmentDTO() {}

    public TechnicianAssignmentDTO(TechnicianAssignment assignment) {
        this.id = assignment.getId();
        if (assignment.getTechnician() != null) {
            // Fixed: Use getId() instead of getStaffId()
            this.technicianId = assignment.getTechnician().getId();
            this.technicianName = assignment.getTechnician().getFullName();
        }
        this.taskDescription = assignment.getTaskDescription();
        this.priority = assignment.getPriority();
        this.deadline = assignment.getDeadline();
        this.assignmentType = assignment.getAssignmentType();
        this.teamId = assignment.getTeamId();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public void setTechnicianName(String technicianName) {
        this.technicianName = technicianName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    @Override
    public String toString() {
        return "TechnicianAssignmentDTO{" +
                "id=" + id +
                ", technicianId=" + technicianId +
                ", technicianName='" + technicianName + '\'' +
                ", taskDescription='" + taskDescription + '\'' +
                ", priority='" + priority + '\'' +
                ", deadline=" + deadline +
                ", assignmentType='" + assignmentType + '\'' +
                ", teamId=" + teamId +
                '}';
    }
}