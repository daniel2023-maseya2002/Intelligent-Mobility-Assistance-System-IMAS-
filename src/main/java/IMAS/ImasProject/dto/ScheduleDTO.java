package IMAS.ImasProject.dto;


import IMAS.ImasProject.model.Schedule;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class ScheduleDTO {
    private Long id;
    private String taskId;
    private Long technicianId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    private String priority;
    private String status;
    private String notes;
    private Boolean isRecurring;
    private String recurrenceType;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recurrenceEndDate;

    // Constructeurs
    public ScheduleDTO() {}

    public ScheduleDTO(String taskId, Long technicianId, LocalDateTime startTime, LocalDateTime endTime) {
        this.taskId = taskId;
        this.technicianId = technicianId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean isRecurring) {
        this.isRecurring = isRecurring;
    }

    public String getRecurrenceType() {
        return recurrenceType;
    }

    public void setRecurrenceType(String recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public LocalDateTime getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public void setRecurrenceEndDate(LocalDateTime recurrenceEndDate) {
        this.recurrenceEndDate = recurrenceEndDate;
    }

    // Méthodes utilitaires pour la conversion
    public Schedule.Priority getPriorityEnum() {
        if (priority == null) return Schedule.Priority.MEDIUM;
        try {
            return Schedule.Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Schedule.Priority.MEDIUM;
        }
    }

    public Schedule.Status getStatusEnum() {
        if (status == null) return Schedule.Status.SCHEDULED;
        try {
            return Schedule.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Schedule.Status.SCHEDULED;
        }
    }

    public Schedule.RecurrenceType getRecurrenceTypeEnum() {
        if (recurrenceType == null) return null;
        try {
            return Schedule.RecurrenceType.valueOf(recurrenceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}