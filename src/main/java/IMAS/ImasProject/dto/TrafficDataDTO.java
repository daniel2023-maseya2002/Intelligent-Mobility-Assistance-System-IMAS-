package IMAS.ImasProject.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class TrafficDataDTO {

    private Long id;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;

    private LocalDateTime timestamp;

    @NotNull(message = "Traffic level is required")
    @Min(value = 1, message = "Traffic level must be between 1 and 5")
    @Max(value = 5, message = "Traffic level must be between 1 and 5")
    private Integer trafficLevel;

    @NotNull(message = "Average speed is required")
    @DecimalMin(value = "0.0", message = "Average speed cannot be negative")
    private Double averageSpeed;

    private String weatherCondition;
    private Boolean isHoliday;
    private Integer dayOfWeek;
    private Integer hourOfDay;
    private Integer vehicleCount;
    private Double visibility;
    private Double temperature;
    private Double humidity;
    private String roadType;
    private String eventType;
    private String trafficLevelDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeurs
    public TrafficDataDTO() {}

    public TrafficDataDTO(Double latitude, Double longitude, Integer trafficLevel, Double averageSpeed) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.trafficLevel = trafficLevel;
        this.averageSpeed = averageSpeed;
        this.timestamp = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Integer getTrafficLevel() { return trafficLevel; }
    public void setTrafficLevel(Integer trafficLevel) { this.trafficLevel = trafficLevel; }

    public Double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(Double averageSpeed) { this.averageSpeed = averageSpeed; }

    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }

    public Boolean getIsHoliday() { return isHoliday; }
    public void setIsHoliday(Boolean isHoliday) { this.isHoliday = isHoliday; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Integer getHourOfDay() { return hourOfDay; }
    public void setHourOfDay(Integer hourOfDay) { this.hourOfDay = hourOfDay; }

    public Integer getVehicleCount() { return vehicleCount; }
    public void setVehicleCount(Integer vehicleCount) { this.vehicleCount = vehicleCount; }

    public Double getVisibility() { return visibility; }
    public void setVisibility(Double visibility) { this.visibility = visibility; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public String getRoadType() { return roadType; }
    public void setRoadType(String roadType) { this.roadType = roadType; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTrafficLevelDescription() { return trafficLevelDescription; }
    public void setTrafficLevelDescription(String trafficLevelDescription) { this.trafficLevelDescription = trafficLevelDescription; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}