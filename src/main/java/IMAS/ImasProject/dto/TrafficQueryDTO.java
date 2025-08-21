package IMAS.ImasProject.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.time.LocalDateTime;

public class TrafficQueryDTO {

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @DecimalMin(value = "0.0")
    private Double radius = 0.01; // Rayon par défaut ~1km

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer minTrafficLevel;
    private Integer maxTrafficLevel;
    private String weatherCondition;
    private Boolean isHoliday;
    private Integer dayOfWeek;
    private Integer hourOfDay;
    private String roadType;
    private String eventType;
    private Integer limit = 100;

    // Constructeurs
    public TrafficQueryDTO() {}

    public TrafficQueryDTO(Double latitude, Double longitude, Double radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    // Getters et Setters
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getRadius() { return radius; }
    public void setRadius(Double radius) { this.radius = radius; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Integer getMinTrafficLevel() { return minTrafficLevel; }
    public void setMinTrafficLevel(Integer minTrafficLevel) { this.minTrafficLevel = minTrafficLevel; }

    public Integer getMaxTrafficLevel() { return maxTrafficLevel; }
    public void setMaxTrafficLevel(Integer maxTrafficLevel) { this.maxTrafficLevel = maxTrafficLevel; }

    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }

    public Boolean getIsHoliday() { return isHoliday; }
    public void setIsHoliday(Boolean isHoliday) { this.isHoliday = isHoliday; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Integer getHourOfDay() { return hourOfDay; }
    public void setHourOfDay(Integer hourOfDay) { this.hourOfDay = hourOfDay; }

    public String getRoadType() { return roadType; }
    public void setRoadType(String roadType) { this.roadType = roadType; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public Integer getLimit() { return limit; }
    public void setLimit(Integer limit) { this.limit = limit; }
}
