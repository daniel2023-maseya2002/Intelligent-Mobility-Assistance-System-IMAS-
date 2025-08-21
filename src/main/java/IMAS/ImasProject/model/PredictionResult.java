
package IMAS.ImasProject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;


@Entity
@Table(name = "prediction_results", indexes = {
        @Index(name = "idx_prediction_route", columnList = "route_id"),
        @Index(name = "idx_prediction_timestamp", columnList = "predictionTimestamp"),
        @Index(name = "idx_prediction_type", columnList = "predictionType")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PredictionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    @JsonBackReference
    private Route route;

    @NotNull(message = "Prediction type cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "prediction_type", nullable = false)
    private PredictionType predictionType;

    @NotNull(message = "Prediction timestamp cannot be null")
    @Column(name = "prediction_timestamp", nullable = false)
    private LocalDateTime predictionTimestamp;

    @Column(name = "predicted_value")
    private Double predictedValue;

    @DecimalMin(value = "0.0", message = "Confidence score must be between 0 and 1")
    @DecimalMax(value = "1.0", message = "Confidence score must be between 0 and 1")
    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Size(max = 1000, message = "Prediction data must not exceed 1000 characters")
    @Column(name = "prediction_data", length = 1000)
    private String predictionData; // JSON string for complex predictions

    @Column(name = "actual_value")
    private Double actualValue; // For accuracy tracking

    @Column(name = "prediction_horizon")
    private Integer predictionHorizon; // Minutes into the future

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "is_accurate")
    private Boolean isAccurate; // Set after comparing with actual

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public boolean isPredictionExpired() {
        if (predictionTimestamp == null || predictionHorizon == null) {
            return true;
        }
        LocalDateTime expiryTime = predictionTimestamp.plusMinutes(predictionHorizon);
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public double calculateAccuracy() {
        if (actualValue == null || predictedValue == null) {
            return 0.0;
        }

        double error = Math.abs(actualValue - predictedValue);
        double accuracy = Math.max(0.0, 1.0 - (error / Math.max(actualValue, predictedValue)));
        return Math.round(accuracy * 10000.0) / 10000.0; // 4 decimal places
    }

    public void updateAccuracy() {
        if (actualValue != null && predictedValue != null) {
            double accuracy = calculateAccuracy();
            this.isAccurate = accuracy >= 0.8; // 80% threshold
        }
    }

    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore >= 0.8;
    }

    @Override
    public String toString() {
        return String.format("PredictionResult{id=%d, type=%s, value=%.2f, confidence=%.2f}",
                id, predictionType, predictedValue, confidenceScore);
    }
}


enum PredictionType {
    PASSENGER_FLOW("Passenger Flow"),
    DELAY_PREDICTION("Delay Prediction"),
    ARRIVAL_TIME("Arrival Time"),
    OCCUPANCY_RATE("Occupancy Rate"),
    MAINTENANCE_NEED("Maintenance Need"),
    TRAFFIC_IMPACT("Traffic Impact"),
    WEATHER_IMPACT("Weather Impact"),
    DEMAND_FORECAST("Demand Forecast");

    private final String displayName;

    PredictionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
