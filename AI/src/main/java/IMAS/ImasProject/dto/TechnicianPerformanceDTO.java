package IMAS.ImasProject.dto;


import java.util.Map;

/**
 * Data Transfer Object for technician performance statistics
 */
public class TechnicianPerformanceDTO {

    private Long technicianId;
    private String technicianName;
    private int tasksAssigned;
    private int tasksCompleted;
    private double onTimeCompletionRate;
    private double averageResponseTime;
    private double averageResolutionTime;
    private Map<String, Integer> skillRatings;
    private Map<String, Integer> tasksByPriority;
    private Map<String, Integer> tasksByStatus;
    private Map<String, Double> completionTrend;
    private Map<String, Integer> incidentTypes;

    // Constructors
    public TechnicianPerformanceDTO() {}

    public TechnicianPerformanceDTO(Long technicianId, String technicianName) {
        this.technicianId = technicianId;
        this.technicianName = technicianName;
    }

    // Getters and Setters
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

    public int getTasksAssigned() {
        return tasksAssigned;
    }

    public void setTasksAssigned(int tasksAssigned) {
        this.tasksAssigned = tasksAssigned;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(int tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public double getOnTimeCompletionRate() {
        return onTimeCompletionRate;
    }

    public void setOnTimeCompletionRate(double onTimeCompletionRate) {
        this.onTimeCompletionRate = onTimeCompletionRate;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public double getAverageResolutionTime() {
        return averageResolutionTime;
    }

    public void setAverageResolutionTime(double averageResolutionTime) {
        this.averageResolutionTime = averageResolutionTime;
    }

    public Map<String, Integer> getSkillRatings() {
        return skillRatings;
    }

    public void setSkillRatings(Map<String, Integer> skillRatings) {
        this.skillRatings = skillRatings;
    }

    public Map<String, Integer> getTasksByPriority() {
        return tasksByPriority;
    }

    public void setTasksByPriority(Map<String, Integer> tasksByPriority) {
        this.tasksByPriority = tasksByPriority;
    }

    public Map<String, Integer> getTasksByStatus() {
        return tasksByStatus;
    }

    public void setTasksByStatus(Map<String, Integer> tasksByStatus) {
        this.tasksByStatus = tasksByStatus;
    }

    public Map<String, Double> getCompletionTrend() {
        return completionTrend;
    }

    public void setCompletionTrend(Map<String, Double> completionTrend) {
        this.completionTrend = completionTrend;
    }

    public Map<String, Integer> getIncidentTypes() {
        return incidentTypes;
    }

    public void setIncidentTypes(Map<String, Integer> incidentTypes) {
        this.incidentTypes = incidentTypes;
    }
}
