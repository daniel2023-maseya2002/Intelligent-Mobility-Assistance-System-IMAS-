package IMAS.ImasProject.services;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssignmentRequestDTO {
    private Long teamId;
    private List<Long> technicianIds = new ArrayList<>();
    private String taskDescription;
    private String taskPriority;
    private LocalDateTime taskDeadline;
    private boolean isTeamAssignment = false;

    // Constructors
    public AssignmentRequestDTO() {}

    // Getters and setters
    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
        // If team ID is set, this is a team assignment
        if (teamId != null) {
            this.isTeamAssignment = true;
        }
    }

    public List<Long> getTechnicianIds() {
        return technicianIds;
    }

    public void setTechnicianIds(List<Long> technicianIds) {
        this.technicianIds = technicianIds;
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

    public LocalDateTime getTaskDeadline() {
        return taskDeadline;
    }

    public void setTaskDeadline(LocalDateTime taskDeadline) {
        this.taskDeadline = taskDeadline;
    }

    public boolean isTeamAssignment() {
        return isTeamAssignment;
    }

    public void setTeamAssignment(boolean teamAssignment) {
        isTeamAssignment = teamAssignment;
    }
}
