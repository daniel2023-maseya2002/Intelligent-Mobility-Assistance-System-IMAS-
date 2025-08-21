package IMAS.ImasProject.dto;


import IMAS.ImasProject.model.MaintenanceTask;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.TaskAssignment;

public class TaskAssignmentDTO {
    private String taskId;
    private Long technicianId;
    private MaintenanceTask task;
    private Staff technician;
    private TaskAssignment.AssignmentMethod assignmentMethod;

    // Constructors, getters and setters
    public TaskAssignmentDTO() {}

    public TaskAssignmentDTO(String taskId, Long technicianId) {
        this.taskId = taskId;
        this.technicianId = technicianId;
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

    public TaskAssignment.AssignmentMethod getAssignmentMethod() {
        return assignmentMethod;
    }

    public void setAssignmentMethod(TaskAssignment.AssignmentMethod assignmentMethod) {
        this.assignmentMethod = assignmentMethod;
    }
}