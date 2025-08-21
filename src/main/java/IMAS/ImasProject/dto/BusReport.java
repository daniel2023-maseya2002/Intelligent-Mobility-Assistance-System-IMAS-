package IMAS.ImasProject.dto;

import java.util.List;

public class BusReport {
    private String busName;
    private String busLine;
    private String status;
    private Double speed;
    private Double occupancyRate;
    private Integer passengers;
    private Integer capacity;
    private Double progress;
    private Integer totalIncidents;
    private Double onTimePercentage;
    private List<IncidentDTO> recentIncidents;
    private List<String> recommendations;

    // Getters and setters
    public String getBusName() { return busName; }
    public void setBusName(String busName) { this.busName = busName; }

    public String getBusLine() { return busLine; }
    public void setBusLine(String busLine) { this.busLine = busLine; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(Double occupancyRate) { this.occupancyRate = occupancyRate; }

    public Integer getPassengers() { return passengers; }
    public void setPassengers(Integer passengers) { this.passengers = passengers; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Double getProgress() { return progress; }
    public void setProgress(Double progress) { this.progress = progress; }

    public Integer getTotalIncidents() { return totalIncidents; }
    public void setTotalIncidents(Integer totalIncidents) { this.totalIncidents = totalIncidents; }

    public Double getOnTimePercentage() { return onTimePercentage; }
    public void setOnTimePercentage(Double onTimePercentage) { this.onTimePercentage = onTimePercentage; }

    public List<IncidentDTO> getRecentIncidents() { return recentIncidents; }
    public void setRecentIncidents(List<IncidentDTO> recentIncidents) { this.recentIncidents = recentIncidents; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public static class IncidentDTO {
        private String dateTime;
        private String description;

        public String getDateTime() { return dateTime; }
        public void setDateTime(String dateTime) { this.dateTime = dateTime; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}