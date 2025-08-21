package IMAS.ImasProject.controller;


import jakarta.validation.constraints.NotNull;

public class AssignTechnicianRequest {
    @NotNull
    private Long technicianId;
    private String taskDescription;
    private String taskPriority;
    private String taskDeadline;

    // Constructors
    public AssignTechnicianRequest() {}

    // Getters and setters
    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskPriority() {
        return taskPriority;
    }

    public void setTaskPriority(String taskPriority) {
        this.taskPriority = taskPriority;
    }

    public String getTaskDeadline() {
        return taskDeadline;
    }

    public void setTaskDeadline(String taskDeadline) {
        this.taskDeadline = taskDeadline;
    }

    // Validation method
    public boolean isValid() {
        return technicianId != null && taskDescription != null && !taskDescription.trim().isEmpty();
    }
}