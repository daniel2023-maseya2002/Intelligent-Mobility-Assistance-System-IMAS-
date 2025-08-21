package IMAS.ImasProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OverallStatsDTO {
    private int totalTasks;
    private int totalCompletedTasks;
    private double systemCompletionRate;
    private double systemAverageProgress;
    private Map<String, Integer> tasksByPriority;
    private Map<String, Integer> tasksByStatus;
    private Map<String, Double> teamPerformance;

    // Statistiques globales spécifiques aux bus
    private int totalBusesOperated = 0;
    private double systemOnTimeRate = 0.0;
    private double systemAverageSpeed = 0.0;
    private int totalIncidentsReported = 0;
    private int totalPassengersTransported = 0;
    private double systemOccupancyRate = 0.0;
    private int totalRoutesCompleted = 0;
    private double systemFuelEfficiency = 0.0;
    private int totalSafetyViolations = 0;
    private double systemCustomerSatisfaction = 0.0;
    private int activeBuses = 0;
    private int stoppedBuses = 0;
    private int busesWithAccidents = 0;

    // Répartitions et analyses détaillées
    private Map<String, Integer> incidentsBySeverity = new HashMap<>();
    private Map<String, Integer> incidentsByType = new HashMap<>();
    private Map<String, Integer> busesByLine = new HashMap<>();
    private Map<String, Integer> busesByRoute = new HashMap<>();
    private Map<String, Double> performanceByLine = new HashMap<>();
    private Map<String, Double> occupancyByRoute = new HashMap<>();
    private Map<String, Integer> passengersByTimeSlot = new HashMap<>();
    private Map<String, Double> fuelConsumptionByLine = new HashMap<>();
    private Map<String, Integer> safetyViolationsByType = new HashMap<>();
    private Map<String, Double> customerSatisfactionByLine = new HashMap<>();

    // Constructeur avec paramètres essentiels
    public OverallStatsDTO(int totalTasks, int totalCompletedTasks, double systemCompletionRate,
                           double systemAverageProgress) {
        this.totalTasks = totalTasks;
        this.totalCompletedTasks = totalCompletedTasks;
        this.systemCompletionRate = systemCompletionRate;
        this.systemAverageProgress = systemAverageProgress;

        // Initialisation des maps
        this.tasksByPriority = new HashMap<>();
        this.tasksByStatus = new HashMap<>();
        this.teamPerformance = new HashMap<>();
        this.incidentsBySeverity = new HashMap<>();
        this.incidentsByType = new HashMap<>();
        this.busesByLine = new HashMap<>();
        this.busesByRoute = new HashMap<>();
        this.performanceByLine = new HashMap<>();
        this.occupancyByRoute = new HashMap<>();
        this.passengersByTimeSlot = new HashMap<>();
        this.fuelConsumptionByLine = new HashMap<>();
        this.safetyViolationsByType = new HashMap<>();
        this.customerSatisfactionByLine = new HashMap<>();
    }

    // Constructeur pour compatibilité avec l'ancien système
    public OverallStatsDTO(int totalTasks, int totalCompletedTasks, double systemCompletionRate,
                           double systemAverageProgress, int totalBusesOperated, double systemOnTimeRate,
                           double systemAverageSpeed, int totalIncidentsReported) {
        this(totalTasks, totalCompletedTasks, systemCompletionRate, systemAverageProgress);
        this.totalBusesOperated = totalBusesOperated;
        this.systemOnTimeRate = systemOnTimeRate;
        this.systemAverageSpeed = systemAverageSpeed;
        this.totalIncidentsReported = totalIncidentsReported;
    }

    // Méthodes utilitaires pour calculer les statistiques
    public double getSystemEfficiencyScore() {
        if (totalBusesOperated == 0) return 0.0;
        return (systemCompletionRate * 0.25) + (systemOnTimeRate * 0.25) +
                (systemOccupancyRate * 0.2) + (systemCustomerSatisfaction * 0.15) +
                (systemFuelEfficiency * 0.15);
    }

    public double getSystemSafetyScore() {
        if (totalBusesOperated == 0) return 100.0;
        double incidentRate = (double) (totalIncidentsReported + totalSafetyViolations) / totalBusesOperated;
        return Math.max(0, 100 - (incidentRate * 10));
    }

    public double getAvgPassengersPerBus() {
        return totalBusesOperated > 0 ? (double) totalPassengersTransported / totalBusesOperated : 0.0;
    }

    public double getAvgRoutesPerBus() {
        return totalBusesOperated > 0 ? (double) totalRoutesCompleted / totalBusesOperated : 0.0;
    }

    public double getOperationalEfficiency() {
        if (activeBuses + stoppedBuses == 0) return 0.0;
        return (double) activeBuses / (activeBuses + stoppedBuses) * 100;
    }

    public double getAccidentRate() {
        return totalBusesOperated > 0 ? (double) busesWithAccidents / totalBusesOperated * 100 : 0.0;
    }

    public String getSystemPerformanceLevel() {
        double efficiency = getSystemEfficiencyScore();
        if (efficiency >= 85) return "Excellent";
        else if (efficiency >= 70) return "Bon";
        else if (efficiency >= 55) return "Moyen";
        else return "À améliorer";
    }

    // Méthodes pour ajouter des données aux maps
    public void addIncidentBySeverity(String severity, Integer count) {
        this.incidentsBySeverity.put(severity, count);
    }

    public void addIncidentByType(String type, Integer count) {
        this.incidentsByType.put(type, count);
    }

    public void addBusByLine(String line, Integer count) {
        this.busesByLine.put(line, count);
    }

    public void addBusByRoute(String route, Integer count) {
        this.busesByRoute.put(route, count);
    }

    public void addPerformanceByLine(String line, Double performance) {
        this.performanceByLine.put(line, performance);
    }

    public void addOccupancyByRoute(String route, Double occupancy) {
        this.occupancyByRoute.put(route, occupancy);
    }

    public void addPassengersByTimeSlot(String timeSlot, Integer passengers) {
        this.passengersByTimeSlot.put(timeSlot, passengers);
    }

    public void addFuelConsumptionByLine(String line, Double consumption) {
        this.fuelConsumptionByLine.put(line, consumption);
    }

    public void addSafetyViolationByType(String type, Integer count) {
        this.safetyViolationsByType.put(type, count);
    }

    public void addCustomerSatisfactionByLine(String line, Double satisfaction) {
        this.customerSatisfactionByLine.put(line, satisfaction);
    }

    // Getters/Setters pour compatibilité avec l'ancien système
    public int getTotalTrainsOperated() {
        return totalBusesOperated;
    }

    public void setTotalTrainsOperated(int totalTrainsOperated) {
        this.totalBusesOperated = totalTrainsOperated;
    }

    @Override
    public String toString() {
        return "OverallStatsDTO{" +
                "totalTasks=" + totalTasks +
                ", totalCompletedTasks=" + totalCompletedTasks +
                ", systemCompletionRate=" + systemCompletionRate +
                ", totalBusesOperated=" + totalBusesOperated +
                ", systemOnTimeRate=" + systemOnTimeRate +
                ", totalPassengersTransported=" + totalPassengersTransported +
                ", systemOccupancyRate=" + systemOccupancyRate +
                ", systemEfficiencyScore=" + getSystemEfficiencyScore() +
                ", systemPerformanceLevel='" + getSystemPerformanceLevel() + '\'' +
                ", operationalEfficiency=" + getOperationalEfficiency() +
                '}';
    }
}