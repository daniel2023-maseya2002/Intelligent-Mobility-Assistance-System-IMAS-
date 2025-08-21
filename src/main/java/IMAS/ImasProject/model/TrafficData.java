package IMAS.ImasProject.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "traffic_data", indexes = {
        @Index(name = "idx_traffic_location", columnList = "latitude, longitude"),
        @Index(name = "idx_traffic_timestamp", columnList = "timestamp"),
        @Index(name = "idx_traffic_level", columnList = "trafficLevel")
})
public class TrafficData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Column(nullable = false)
    private Double latitude;

    @NotNull
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Column(nullable = false)
    private Double longitude;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotNull
    @Min(value = 1, message = "Traffic level must be between 1 and 5")
    @Max(value = 5, message = "Traffic level must be between 1 and 5")
    @Column(nullable = false)
    private Integer trafficLevel; // 1=Fluide, 2=Modéré, 3=Dense, 4=Embouteillé, 5=Bloqué

    @NotNull
    @DecimalMin(value = "0.0", message = "Average speed cannot be negative")
    @Column(nullable = false)
    private Double averageSpeed; // km/h

    @Size(max = 50)
    private String weatherCondition; // sunny, rainy, cloudy, stormy

    @NotNull
    @Column(nullable = false)
    private Boolean isHoliday;

    @NotNull
    @Min(value = 1, message = "Day of week must be between 1 and 7")
    @Max(value = 7, message = "Day of week must be between 1 and 7")
    @Column(nullable = false)
    private Integer dayOfWeek; // 1=Lundi, 7=Dimanche

    @NotNull
    @Min(value = 0, message = "Hour must be between 0 and 23")
    @Max(value = 23, message = "Hour must be between 0 and 23")
    @Column(nullable = false)
    private Integer hourOfDay;

    // Champs additionnels pour l'analyse ML
    private Integer vehicleCount; // Nombre de véhicules observés
    private Double visibility; // Visibilité en km
    private Double temperature; // Température en Celsius
    private Double humidity; // Humidité en %
    private String roadType; // highway, main_road, secondary_road, residential
    private String eventType; // accident, construction, event, normal

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructeurs
    public TrafficData() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public TrafficData(Double latitude, Double longitude, Integer trafficLevel,
                       Double averageSpeed, String weatherCondition, Boolean isHoliday) {
        this();
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = LocalDateTime.now();
        this.trafficLevel = trafficLevel;
        this.averageSpeed = averageSpeed;
        this.weatherCondition = weatherCondition;
        this.isHoliday = isHoliday;
        this.dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        this.hourOfDay = LocalDateTime.now().getHour();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Méthodes utilitaires
    public String getTrafficLevelDescription() {
        switch (trafficLevel) {
            case 1: return "Fluide";
            case 2: return "Modéré";
            case 3: return "Dense";
            case 4: return "Embouteillé";
            case 5: return "Bloqué";
            default: return "Inconnu";
        }
    }

    public boolean isRushHour() {
        return (hourOfDay >= 7 && hourOfDay <= 9) || (hourOfDay >= 17 && hourOfDay <= 19);
    }

    public boolean isWeekend() {
        return dayOfWeek == 6 || dayOfWeek == 7; // Samedi ou Dimanche
    }

    @Override
    public String toString() {
        return "TrafficData{" +
                "id=" + id +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", timestamp=" + timestamp +
                ", trafficLevel=" + trafficLevel +
                ", averageSpeed=" + averageSpeed +
                ", weatherCondition='" + weatherCondition + '\'' +
                '}';
    }
}