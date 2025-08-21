package IMAS.ImasProject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatsDTO {
    private Long teamId;
    private String teamName;
    private int totalTasks;
    private int totalCompletedTasks;
    private double teamCompletionRate;
    private List<OperatorStatsDTO> operatorStats;

    // Statistiques spécifiques aux bus pour l'équipe
    private int totalBusesOperated = 0;
    private double teamOnTimeRate = 0.0;
    private int totalIncidents = 0;
    private int totalPassengersTransported = 0;
    private double teamOccupancyRate = 0.0;
    private int totalRoutesCompleted = 0;
    private double teamAverageSpeed = 0.0;
    private int totalSafetyViolations = 0;
    private double teamFuelEfficiency = 0.0;
    private double teamCustomerSatisfaction = 0.0;
    private int activeBusDrivers = 0;
    private int totalBusDrivers = 0;

    // Constructeur avec paramètres essentiels
    public TeamStatsDTO(Long teamId, String teamName, int totalTasks, int totalCompletedTasks,
                        double teamCompletionRate, List<OperatorStatsDTO> operatorStats) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.totalTasks = totalTasks;
        this.totalCompletedTasks = totalCompletedTasks;
        this.teamCompletionRate = teamCompletionRate;
        this.operatorStats = operatorStats;
    }

    // Constructeur pour compatibilité avec l'ancien système
    public TeamStatsDTO(Long teamId, String teamName, int totalTasks, int totalCompletedTasks,
                        double teamCompletionRate, List<OperatorStatsDTO> operatorStats,
                        int totalBusesOperated, double teamOnTimeRate, int totalIncidents) {
        this(teamId, teamName, totalTasks, totalCompletedTasks, teamCompletionRate, operatorStats);
        this.totalBusesOperated = totalBusesOperated;
        this.teamOnTimeRate = teamOnTimeRate;
        this.totalIncidents = totalIncidents;
    }

    // Méthodes utilitaires pour calculer les statistiques d'équipe
    public double getTeamEfficiencyScore() {
        if (totalBusesOperated == 0) return 0.0;
        return (teamCompletionRate * 0.25) + (teamOnTimeRate * 0.25) +
                (teamOccupancyRate * 0.2) + (teamCustomerSatisfaction * 0.15) +
                (teamFuelEfficiency * 0.15);
    }

    public double getTeamSafetyScore() {
        if (totalBusesOperated == 0) return 100.0;
        double incidentRate = (double) (totalIncidents + totalSafetyViolations) / totalBusesOperated;
        return Math.max(0, 100 - (incidentRate * 10));
    }

    public double getAvgPassengersPerBus() {
        return totalBusesOperated > 0 ? (double) totalPassengersTransported / totalBusesOperated : 0.0;
    }

    public double getAvgRoutesPerBus() {
        return totalBusesOperated > 0 ? (double) totalRoutesCompleted / totalBusesOperated : 0.0;
    }

    public double getTeamProductivity() {
        return totalBusDrivers > 0 ? (double) totalBusesOperated / totalBusDrivers : 0.0;
    }

    public double getDriverUtilizationRate() {
        return totalBusDrivers > 0 ? (double) activeBusDrivers / totalBusDrivers * 100 : 0.0;
    }

    public double getIncidentRate() {
        return totalBusesOperated > 0 ? (double) totalIncidents / totalBusesOperated * 100 : 0.0;
    }

    public String getTeamPerformanceLevel() {
        double efficiency = getTeamEfficiencyScore();
        if (efficiency >= 85) return "Excellent";
        else if (efficiency >= 70) return "Bon";
        else if (efficiency >= 55) return "Moyen";
        else return "À améliorer";
    }

    public int getTeamSize() {
        return operatorStats != null ? operatorStats.size() : 0;
    }

    public OperatorStatsDTO getTopPerformer() {
        if (operatorStats == null || operatorStats.isEmpty()) return null;

        return operatorStats.stream()
                .max((op1, op2) -> Double.compare(op1.getEfficiencyScore(), op2.getEfficiencyScore()))
                .orElse(null);
    }

    public double getTeamAverageTasksPerOperator() {
        int teamSize = getTeamSize();
        return teamSize > 0 ? (double) totalTasks / teamSize : 0.0;
    }

    // FIX: Access field directly since Lombok getter might not be available
    public double getTeamAverageCompletionRate() {
        if (operatorStats == null || operatorStats.isEmpty()) return 0.0;

        return operatorStats.stream()
                .mapToDouble(operator -> operator.completionRate)
                .average()
                .orElse(0.0);
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
        return "TeamStatsDTO{" +
                "teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", totalTasks=" + totalTasks +
                ", totalCompletedTasks=" + totalCompletedTasks +
                ", teamCompletionRate=" + teamCompletionRate +
                ", totalBusesOperated=" + totalBusesOperated +
                ", teamOnTimeRate=" + teamOnTimeRate +
                ", totalIncidents=" + totalIncidents +
                ", teamSize=" + getTeamSize() +
                ", teamEfficiencyScore=" + getTeamEfficiencyScore() +
                ", teamPerformanceLevel='" + getTeamPerformanceLevel() + '\'' +
                ", driverUtilizationRate=" + getDriverUtilizationRate() +
                '}';
    }
}