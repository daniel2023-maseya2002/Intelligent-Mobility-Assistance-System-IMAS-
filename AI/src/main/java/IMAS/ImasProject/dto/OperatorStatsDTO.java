package IMAS.ImasProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperatorStatsDTO {
    private Long operatorId;
    private String name;
    private int totalTasks;
    private int tasksCompleted;
    public double completionRate;

    // Statistiques spécifiques aux bus
    private int busesOperated = 0;
    private double onTimeRate = 0.0;
    private int incidentCount = 0;
    private int totalPassengersTransported = 0;
    private double avgOccupancyRate = 0.0;
    private int routesCompleted = 0;
    private double avgSpeed = 0.0;
    private int safetyViolations = 0;
    private double fuelEfficiency = 0.0;
    private double customerSatisfactionScore = 0.0;

    // Constructeur avec paramètres essentiels
    public OperatorStatsDTO(Long operatorId, String name, int totalTasks, int tasksCompleted, double completionRate) {
        this.operatorId = operatorId;
        this.name = name;
        this.totalTasks = totalTasks;
        this.tasksCompleted = tasksCompleted;
        this.completionRate = completionRate;
    }

    // Constructeur pour compatibilité avec l'ancien système
    public OperatorStatsDTO(Long operatorId, String name, int totalTasks, int tasksCompleted,
                            double completionRate, int busesOperated, double onTimeRate, int incidentCount) {
        this.operatorId = operatorId;
        this.name = name;
        this.totalTasks = totalTasks;
        this.tasksCompleted = tasksCompleted;
        this.completionRate = completionRate;
        this.busesOperated = busesOperated;
        this.onTimeRate = onTimeRate;
        this.incidentCount = incidentCount;
    }

    // Méthodes utilitaires
    public double getAvgPassengersPerTrip() {
        return busesOperated > 0 ? (double) totalPassengersTransported / busesOperated : 0.0;
    }

    public double getSafetyScore() {
        if (busesOperated == 0) return 100.0;
        double incidentRate = (double) (incidentCount + safetyViolations) / busesOperated;
        return Math.max(0, 100 - (incidentRate * 10));
    }

    public double getEfficiencyScore() {
        if (busesOperated == 0) return 0.0;
        return (completionRate * 0.3) + (onTimeRate * 0.3) +
                (avgOccupancyRate * 0.2) + (customerSatisfactionScore * 0.2);
    }

    public String getPerformanceLevel() {
        double efficiency = getEfficiencyScore();
        if (efficiency >= 85) return "Excellent";
        else if (efficiency >= 70) return "Bon";
        else if (efficiency >= 55) return "Moyen";
        else return "À améliorer";
    }

    // Getters explicites (au cas où Lombok ne les génère pas correctement)
    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    // Getters/Setters pour compatibilité (si nécessaire)
    public Long getDriverId() {
        return operatorId;
    }

    public void setDriverId(Long driverId) {
        this.operatorId = driverId;
    }

    public String getDriverName() {
        return name;
    }

    public void setDriverName(String driverName) {
        this.name = driverName;
    }

    @Override
    public String toString() {
        return "OperatorStatsDTO{" +
                "operatorId=" + operatorId +
                ", name='" + name + '\'' +
                ", totalTasks=" + totalTasks +
                ", tasksCompleted=" + tasksCompleted +
                ", completionRate=" + completionRate +
                ", busesOperated=" + busesOperated +
                ", onTimeRate=" + onTimeRate +
                ", incidentCount=" + incidentCount +
                ", avgOccupancyRate=" + avgOccupancyRate +
                ", performanceLevel='" + getPerformanceLevel() + '\'' +
                '}';
    }
}