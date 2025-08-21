package IMAS.ImasProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndividualStatsDTO {
    private Long driverId;
    private String driverName;
    private String teamName;

    // Statistiques principales que le frontend attend
    private int totalTasks;
    private int completedTasks;
    private int pendingTasks;
    private int inProgressTasks;
    private double completionRate;
    private double avgCompletionTime;

    // Données détaillées pour les graphiques
    private Map<String, Integer> priorityBreakdown;
    private Map<LocalDateTime, Double> performanceTrend;
    private Map<String, Double> skillsAssessment;
    private List<Map<String, Object>> teamTasks;

    // Statistiques spécifiques aux bus
    private int busesOperated = 0;
    private double onTimeRate = 0.0;
    private double avgSpeed = 0.0;
    private int incidentReports = 0;
    private int totalPassengersTransported = 0;
    private double avgOccupancyRate = 0.0;
    private int routesCompleted = 0;
    private double fuelEfficiency = 0.0;
    private int safetyViolations = 0;
    private double customerSatisfactionScore = 0.0;

    // Tendances pour les graphiques
    private Map<LocalDateTime, Double> onTimeTrend = new HashMap<>();
    private Map<LocalDateTime, Double> speedTrend = new HashMap<>();
    private Map<LocalDateTime, Double> occupancyTrend = new HashMap<>();
    private Map<LocalDateTime, Integer> passengerTrend = new HashMap<>();
    private Map<LocalDateTime, Double> fuelConsumptionTrend = new HashMap<>();
    private Map<LocalDateTime, Double> safetyScoreTrend = new HashMap<>();

    // Répartition par type de route
    private Map<String, Integer> routeTypeBreakdown = new HashMap<>();

    // Statistiques par ligne de bus
    private Map<String, Map<String, Object>> busLineStats = new HashMap<>();

    // Historique des accidents/incidents
    private List<Map<String, Object>> accidentHistory;

    // Évaluations de performance
    private Map<String, Double> performanceMetrics = new HashMap<>();

    // Constructeur avec paramètres essentiels
    public IndividualStatsDTO(Long driverId, String driverName, String teamName,
                              int totalTasks, int completedTasks, int pendingTasks,
                              int inProgressTasks) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.teamName = teamName;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.pendingTasks = pendingTasks;
        this.inProgressTasks = inProgressTasks;
        this.completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;

        // Initialisation des maps
        this.priorityBreakdown = new HashMap<>();
        this.performanceTrend = new HashMap<>();
        this.skillsAssessment = new HashMap<>();
        this.onTimeTrend = new HashMap<>();
        this.speedTrend = new HashMap<>();
        this.occupancyTrend = new HashMap<>();
        this.passengerTrend = new HashMap<>();
        this.fuelConsumptionTrend = new HashMap<>();
        this.safetyScoreTrend = new HashMap<>();
        this.routeTypeBreakdown = new HashMap<>();
        this.busLineStats = new HashMap<>();
        this.performanceMetrics = new HashMap<>();
    }

    // Getters/Setters personnalisés pour compatibilité avec l'ancien système
    public Long getOperatorId() {
        return driverId;
    }

    public void setOperatorId(Long operatorId) {
        this.driverId = operatorId;
    }

    public String getOperatorName() {
        return driverName;
    }

    public void setOperatorName(String operatorName) {
        this.driverName = operatorName;
    }

    // Méthodes utilitaires pour calculer les statistiques
    public double calculateEfficiencyScore() {
        double efficiency = 0.0;
        if (busesOperated > 0) {
            efficiency = (completionRate * 0.3) + (onTimeRate * 0.3) +
                    (avgOccupancyRate * 0.2) + (customerSatisfactionScore * 0.2);
        }
        return Math.round(efficiency * 100.0) / 100.0;
    }

    public double getAvgPassengersPerTrip() {
        return busesOperated > 0 ? (double) totalPassengersTransported / busesOperated : 0.0;
    }

    public double getSafetyScore() {
        if (busesOperated == 0) return 0.0;
        // Score de sécurité basé sur le nombre d'incidents par rapport aux trajets
        double incidentRate = (double) (incidentReports + safetyViolations) / busesOperated;
        return Math.max(0, 100 - (incidentRate * 10)); // Score sur 100
    }

    public String getPerformanceLevel() {
        double efficiency = calculateEfficiencyScore();
        if (efficiency >= 85) return "Excellent";
        else if (efficiency >= 70) return "Bon";
        else if (efficiency >= 55) return "Moyen";
        else return "À améliorer";
    }

    // Méthodes pour ajouter des données aux tendances
    public void addOnTimeTrend(LocalDateTime date, Double rate) {
        this.onTimeTrend.put(date, rate);
    }

    public void addSpeedTrend(LocalDateTime date, Double speed) {
        this.speedTrend.put(date, speed);
    }

    public void addOccupancyTrend(LocalDateTime date, Double occupancy) {
        this.occupancyTrend.put(date, occupancy);
    }

    public void addPassengerTrend(LocalDateTime date, Integer passengers) {
        this.passengerTrend.put(date, passengers);
    }

    public void addFuelConsumptionTrend(LocalDateTime date, Double consumption) {
        this.fuelConsumptionTrend.put(date, consumption);
    }

    public void addSafetyScoreTrend(LocalDateTime date, Double score) {
        this.safetyScoreTrend.put(date, score);
    }

    // Méthodes pour la répartition par type de route
    public void addRouteTypeData(String routeType, Integer count) {
        this.routeTypeBreakdown.put(routeType, count);
    }

    // Méthodes pour les statistiques par ligne de bus
    public void addBusLineStats(String busLine, String metric, Object value) {
        this.busLineStats.computeIfAbsent(busLine, k -> new HashMap<>()).put(metric, value);
    }

    // Méthodes pour les métriques de performance
    public void addPerformanceMetric(String metric, Double value) {
        this.performanceMetrics.put(metric, value);
    }

    @Override
    public String toString() {
        return "IndividualStatsDTO{" +
                "driverId=" + driverId +
                ", driverName='" + driverName + '\'' +
                ", teamName='" + teamName + '\'' +
                ", totalTasks=" + totalTasks +
                ", completedTasks=" + completedTasks +
                ", completionRate=" + completionRate +
                ", busesOperated=" + busesOperated +
                ", onTimeRate=" + onTimeRate +
                ", avgOccupancyRate=" + avgOccupancyRate +
                ", performanceLevel='" + getPerformanceLevel() + '\'' +
                '}';
    }
}