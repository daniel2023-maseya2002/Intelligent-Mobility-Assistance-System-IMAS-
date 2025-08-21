package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, String> {

    // Find incidents by type
    List<Incident> findByIncidentType(Incident.IncidentType incidentType);

    // Find incidents by status
    List<Incident> findByStatus(Incident.IncidentStatus status);

    // Find incidents by severity
    List<Incident> findBySeverity(Incident.Severity severity);

    // Find incidents by technician ID - Fixed: using id instead of staffId
    @Query("SELECT i FROM Incident i JOIN i.assignedTechnicians a WHERE a.technician.id = :technicianId ORDER BY i.dateTime DESC")
    List<Incident> findByTechnicianInAssignedTechnicians(@Param("technicianId") Long technicianId);

    // Find incidents by assigned technician ID
    List<Incident> findByAssignedTechnicianIdOrderByDateTimeDesc(Long technicianId);

    // Find incidents by assigned technician ID and date range
    @Query("SELECT i FROM Incident i WHERE i.assignedTechnicianId = :technicianId AND i.dateTime BETWEEN :start AND :end ORDER BY i.dateTime DESC")
    List<Incident> findByAssignedTechnicianIdAndDateTimeBetween(
            @Param("technicianId") Long technicianId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Find incidents by multiple technician IDs and date range
    @Query("SELECT i FROM Incident i WHERE i.assignedTechnicianId IN :technicianIds AND i.dateTime BETWEEN :start AND :end ORDER BY i.dateTime DESC")
    List<Incident> findByAssignedTechnicianIdInAndDateTimeBetween(
            @Param("technicianIds") List<Long> technicianIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Find incidents within a date range
    List<Incident> findByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find incidents within a date range ordered by date
    List<Incident> findByDateTimeBetweenOrderByDateTimeDesc(LocalDateTime start, LocalDateTime end);

    // Find incidents by location
    List<Incident> findByLocation(String location);

    // Custom query to find incidents with details containing a specific key
    @Query("SELECT i FROM Incident i WHERE KEY(i.additionalDetails) = :detailKey")
    List<Incident> findIncidentsWithDetailKey(@Param("detailKey") String detailKey);

    // Count incidents by location
    long countByLocation(String location);

    // Count incidents by type
    long countByIncidentType(Incident.IncidentType incidentType);

    // Count incidents by status
    long countByStatus(Incident.IncidentStatus status);

    // Count incidents by severity
    long countBySeverity(Incident.Severity severity);

    // Count incidents by bus ID
    long countByBusId(Long busId);

    // Count incidents between two dates
    long countByDateTimeBetween(LocalDateTime start, LocalDateTime end);

    // Count incidents by date range
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.dateTime BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate);

    // Count incidents between two dates with specific severity
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.dateTime BETWEEN :start AND :end AND i.severity = :severity")
    long countByDateTimeBetweenAndSeverity(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("severity") Incident.Severity severity);

    // Count incidents by incident type and date range
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.incidentType = :incidentType AND i.dateTime BETWEEN :start AND :end")
    long countByIncidentTypeAndDateTimeBetween(
            @Param("incidentType") Incident.IncidentType incidentType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Count incidents by team and date range
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.assignedTeam.id = :teamId AND i.dateTime BETWEEN :startDate AND :endDate")
    Long countByTeamAndDateRange(@Param("teamId") Long teamId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);

    // Count incidents by bus line and date range
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.bus.busLine = :busLine AND i.dateTime BETWEEN :startDate AND :endDate")
    Long countByBusLineAndDateRange(@Param("busLine") String busLine,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);

    // Get average resolution time in hours
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, i.dateTime, i.resolutionTime)) FROM Incident i WHERE i.dateTime BETWEEN :start AND :end AND i.resolutionTime IS NOT NULL")
    Double getAverageResolutionTime(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    // Get average resolution time by incident type
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, i.dateTime, i.resolutionTime)) FROM Incident i WHERE i.incidentType = :incidentType AND i.dateTime BETWEEN :start AND :end AND i.resolutionTime IS NOT NULL")
    Double getAverageResolutionTimeByType(@Param("incidentType") Incident.IncidentType incidentType,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    // Get average resolution time by severity
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, i.dateTime, i.resolutionTime)) FROM Incident i WHERE i.severity = :severity AND i.dateTime BETWEEN :start AND :end AND i.resolutionTime IS NOT NULL")
    Double getAverageResolutionTimeBySeverity(@Param("severity") Incident.Severity severity,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    // Count incidents by type
    @Query("SELECT i.incidentType as type, COUNT(i) as count FROM Incident i WHERE i.dateTime BETWEEN :start AND :end GROUP BY i.incidentType")
    List<Map<String, Object>> getIncidentCountByType(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    // Count incidents by severity
    @Query("SELECT i.severity as severity, COUNT(i) as count FROM Incident i WHERE i.dateTime BETWEEN :start AND :end GROUP BY i.severity")
    List<Map<String, Object>> getIncidentCountBySeverity(@Param("start") LocalDateTime start,
                                                         @Param("end") LocalDateTime end);

    // Count incidents by status
    @Query("SELECT i.status as status, COUNT(i) as count FROM Incident i WHERE i.dateTime BETWEEN :start AND :end GROUP BY i.status")
    List<Map<String, Object>> getIncidentCountByStatus(@Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end);

    // Count incidents by location
    @Query("SELECT i.location as location, COUNT(i) as count FROM Incident i WHERE i.dateTime BETWEEN :start AND :end GROUP BY i.location ORDER BY count DESC")
    List<Map<String, Object>> getIncidentCountByLocation(@Param("start") LocalDateTime start,
                                                         @Param("end") LocalDateTime end);

    // Count incidents by bus line
    @Query("SELECT i.bus.busLine as busLine, COUNT(i) as count FROM Incident i WHERE i.dateTime BETWEEN :start AND :end AND i.bus IS NOT NULL GROUP BY i.bus.busLine ORDER BY count DESC")
    List<Map<String, Object>> getIncidentCountByBusLine(@Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);

    // Get incident trends by day
    @Query("SELECT FUNCTION('DATE', i.dateTime) as date, COUNT(i) as total, " +
            "SUM(CASE WHEN i.severity = 'HIGH' OR i.severity = 'CRITICAL' THEN 1 ELSE 0 END) as major " +
            "FROM Incident i WHERE i.dateTime BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('DATE', i.dateTime) ORDER BY FUNCTION('DATE', i.dateTime)")
    List<Map<String, Object>> getIncidentTrend(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    // Get incident trends by week
    @Query("SELECT FUNCTION('WEEK', i.dateTime) as week, FUNCTION('YEAR', i.dateTime) as year, COUNT(i) as total, " +
            "SUM(CASE WHEN i.severity = 'HIGH' OR i.severity = 'CRITICAL' THEN 1 ELSE 0 END) as major " +
            "FROM Incident i WHERE i.dateTime BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('WEEK', i.dateTime), FUNCTION('YEAR', i.dateTime) " +
            "ORDER BY FUNCTION('YEAR', i.dateTime), FUNCTION('WEEK', i.dateTime)")
    List<Map<String, Object>> getIncidentTrendByWeek(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    // Get incident trends by month
    @Query("SELECT FUNCTION('MONTH', i.dateTime) as month, FUNCTION('YEAR', i.dateTime) as year, COUNT(i) as total, " +
            "SUM(CASE WHEN i.severity = 'HIGH' OR i.severity = 'CRITICAL' THEN 1 ELSE 0 END) as major " +
            "FROM Incident i WHERE i.dateTime BETWEEN :start AND :end " +
            "GROUP BY FUNCTION('MONTH', i.dateTime), FUNCTION('YEAR', i.dateTime) " +
            "ORDER BY FUNCTION('YEAR', i.dateTime), FUNCTION('MONTH', i.dateTime)")
    List<Map<String, Object>> getIncidentTrendByMonth(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    // Get incidents by bus driver - Fixed: using id instead of staffId
    @Query("SELECT i FROM Incident i WHERE i.bus.driver.id = :driverId ORDER BY i.dateTime DESC")
    List<Incident> findByBusDriverId(@Param("driverId") Long driverId);

    // Count incidents by bus driver - Fixed: using id instead of staffId
    @Query("SELECT COUNT(i) FROM Incident i WHERE i.bus.driver.id = :driverId AND i.dateTime BETWEEN :start AND :end")
    Long countByBusDriverIdAndDateRange(@Param("driverId") Long driverId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);

    // Get unresolved incidents (without resolution time)
    @Query("SELECT i FROM Incident i WHERE i.resolutionTime IS NULL ORDER BY i.dateTime DESC")
    List<Incident> findUnresolvedIncidents();

    // Get overdue incidents (created more than X hours ago and still unresolved)
    @Query("SELECT i FROM Incident i WHERE i.resolutionTime IS NULL AND i.dateTime < :overdueThreshold ORDER BY i.dateTime")
    List<Incident> findOverdueIncidents(@Param("overdueThreshold") LocalDateTime overdueThreshold);

    // Get incidents by multiple statuses
    @Query("SELECT i FROM Incident i WHERE i.status IN :statuses ORDER BY i.dateTime DESC")
    List<Incident> findByStatusIn(@Param("statuses") List<Incident.IncidentStatus> statuses);

    // Get incidents by multiple severities
    @Query("SELECT i FROM Incident i WHERE i.severity IN :severities ORDER BY i.dateTime DESC")
    List<Incident> findBySeverityIn(@Param("severities") List<Incident.Severity> severities);

    // Get incidents by multiple types
    @Query("SELECT i FROM Incident i WHERE i.incidentType IN :types ORDER BY i.dateTime DESC")
    List<Incident> findByIncidentTypeIn(@Param("types") List<Incident.IncidentType> types);

    // Get incidents by bus capacity range
    @Query("SELECT i FROM Incident i WHERE i.bus.capacity BETWEEN :minCapacity AND :maxCapacity ORDER BY i.dateTime DESC")
    List<Incident> findByBusCapacityRange(@Param("minCapacity") int minCapacity,
                                          @Param("maxCapacity") int maxCapacity);

    // Get incidents by passenger count range
    @Query("SELECT i FROM Incident i WHERE i.bus.passengers BETWEEN :minPassengers AND :maxPassengers ORDER BY i.dateTime DESC")
    List<Incident> findByBusPassengerRange(@Param("minPassengers") int minPassengers,
                                           @Param("maxPassengers") int maxPassengers);

    // Get most recent incident by bus ID - Fixed: Removed LIMIT 1 (not supported in JPQL)
    @Query("SELECT i FROM Incident i WHERE i.busId = :busId ORDER BY i.dateTime DESC")
    Optional<Incident> findLatestByBusId(@Param("busId") Long busId);

    // Alternative method using Spring Data method naming convention
    Optional<Incident> findTopByBusIdOrderByDateTimeDesc(Long busId);

    // Get most critical incidents (HIGH or CRITICAL severity) - Fixed: Using enum values
    @Query("SELECT i FROM Incident i WHERE i.severity IN ('HIGH', 'CRITICAL') AND i.dateTime BETWEEN :start AND :end ORDER BY i.dateTime DESC")
    List<Incident> findCriticalIncidents(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    // Get incidents requiring immediate attention (CRITICAL severity and REPORTED status) - Fixed: Using enum values
    @Query("SELECT i FROM Incident i WHERE i.severity = 'CRITICAL' AND i.status = 'REPORTED' ORDER BY i.dateTime DESC")
    List<Incident> findIncidentsRequiringImmediateAttention();

    // Get incident statistics summary
    @Query("SELECT " +
            "COUNT(i) as totalIncidents, " +
            "SUM(CASE WHEN i.severity = 'CRITICAL' THEN 1 ELSE 0 END) as criticalIncidents, " +
            "SUM(CASE WHEN i.severity = 'HIGH' THEN 1 ELSE 0 END) as highIncidents, " +
            "SUM(CASE WHEN i.severity = 'MEDIUM' THEN 1 ELSE 0 END) as mediumIncidents, " +
            "SUM(CASE WHEN i.severity = 'LOW' THEN 1 ELSE 0 END) as lowIncidents, " +
            "SUM(CASE WHEN i.status = 'RESOLVED' THEN 1 ELSE 0 END) as resolvedIncidents, " +
            "SUM(CASE WHEN i.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgressIncidents, " +
            "SUM(CASE WHEN i.status = 'REPORTED' THEN 1 ELSE 0 END) as reportedIncidents " +
            "FROM Incident i WHERE i.dateTime BETWEEN :start AND :end")
    Map<String, Object> getIncidentStatisticsSummary(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    // Additional helper methods for better performance

    // Find incidents by driver using Spring Data method naming
    List<Incident> findByBusDriverIdOrderByDateTimeDesc(Long driverId);

    // Count incidents by driver using Spring Data method naming
    Long countByBusDriverId(Long driverId);

    // Find recent incidents (last 24 hours)
    @Query("SELECT i FROM Incident i WHERE i.dateTime >= :since ORDER BY i.dateTime DESC")
    List<Incident> findRecentIncidents(@Param("since") LocalDateTime since);

    // Find incidents by priority (combining severity and status)
    @Query("SELECT i FROM Incident i WHERE (i.severity = 'CRITICAL' AND i.status != 'RESOLVED') OR (i.severity = 'HIGH' AND i.status = 'REPORTED') ORDER BY i.dateTime DESC")
    List<Incident> findHighPriorityIncidents();

    // Get incidents with resolution time within specified hours
    @Query("SELECT i FROM Incident i WHERE i.resolutionTime IS NOT NULL AND TIMESTAMPDIFF(HOUR, i.dateTime, i.resolutionTime) <= :maxHours ORDER BY i.dateTime DESC")
    List<Incident> findIncidentsResolvedWithinHours(@Param("maxHours") int maxHours);

    // Get incidents that took longer than specified hours to resolve
    @Query("SELECT i FROM Incident i WHERE i.resolutionTime IS NOT NULL AND TIMESTAMPDIFF(HOUR, i.dateTime, i.resolutionTime) > :minHours ORDER BY i.dateTime DESC")
    List<Incident> findIncidentsResolvedAfterHours(@Param("minHours") int minHours);

    // Find incidents by bus ID
    List<Incident> findByBusId(Long busId);

    // ============== DYNAMIC REROUTING QUERIES ==============

    // Find traffic-related incidents affecting specific route segments
    @Query("SELECT i FROM Incident i WHERE i.incidentType IN ('TRAFFIC_CONGESTION', 'ROAD_CLOSURE', 'ACCIDENT') " +
            "AND i.status IN ('REPORTED', 'IN_PROGRESS') " +
            "AND i.location LIKE %:routeSegment% " +
            "ORDER BY i.severity DESC, i.dateTime DESC")
    List<Incident> findActiveTrafficIncidentsByRouteSegment(@Param("routeSegment") String routeSegment);

    // Find incidents that could affect bus route performance
    @Query("SELECT i FROM Incident i WHERE i.bus.busLine = :busLine " +
            "AND i.incidentType IN ('TRAFFIC_CONGESTION', 'ROAD_CLOSURE', 'ACCIDENT', 'WEATHER_RELATED') " +
            "AND i.status IN ('REPORTED', 'IN_PROGRESS') " +
            "AND i.dateTime >= :since " +
            "ORDER BY i.severity DESC, i.dateTime DESC")
    List<Incident> findRouteDisruptingIncidents(@Param("busLine") String busLine,
                                                @Param("since") LocalDateTime since);

    // Get historical traffic pattern data for route optimization
    @Query("SELECT i.location as location, " +
            "FUNCTION('HOUR', i.dateTime) as hour, " +
            "FUNCTION('DAYOFWEEK', i.dateTime) as dayOfWeek, " +
            "COUNT(i) as incidentCount, " +
            "AVG(TIMESTAMPDIFF(MINUTE, i.dateTime, i.resolutionTime)) as avgResolutionMinutes " +
            "FROM Incident i WHERE i.incidentType = 'TRAFFIC_CONGESTION' " +
            "AND i.dateTime BETWEEN :start AND :end " +
            "AND i.resolutionTime IS NOT NULL " +
            "GROUP BY i.location, FUNCTION('HOUR', i.dateTime), FUNCTION('DAYOFWEEK', i.dateTime) " +
            "ORDER BY incidentCount DESC")
    List<Map<String, Object>> getTrafficPatternAnalysis(@Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);

    // Find incidents by geographic coordinates (for route planning)
    @Query("SELECT i FROM Incident i WHERE " +
            "CAST(i.additionalDetails['latitude'] AS double) BETWEEN :minLat AND :maxLat " +
            "AND CAST(i.additionalDetails['longitude'] AS double) BETWEEN :minLon AND :maxLon " +
            "AND i.status IN ('REPORTED', 'IN_PROGRESS') " +
            "ORDER BY i.severity DESC, i.dateTime DESC")
    List<Incident> findIncidentsByGeoBounds(@Param("minLat") double minLat,
                                            @Param("maxLat") double maxLat,
                                            @Param("minLon") double minLon,
                                            @Param("maxLon") double maxLon);

    // Get congestion hotspots for proactive rerouting
    @Query("SELECT i.location as location, " +
            "COUNT(i) as totalIncidents, " +
            "SUM(CASE WHEN i.severity IN ('HIGH', 'CRITICAL') THEN 1 ELSE 0 END) as severeIncidents, " +
            "AVG(TIMESTAMPDIFF(MINUTE, i.dateTime, COALESCE(i.resolutionTime, CURRENT_TIMESTAMP))) as avgDurationMinutes " +
            "FROM Incident i WHERE i.incidentType IN ('TRAFFIC_CONGESTION', 'ACCIDENT') " +
            "AND i.dateTime >= :since " +
            "GROUP BY i.location " +
            "HAVING COUNT(i) >= :minIncidents " +
            "ORDER BY severeIncidents DESC, totalIncidents DESC")
    List<Map<String, Object>> getCongestionHotspots(@Param("since") LocalDateTime since,
                                                    @Param("minIncidents") int minIncidents);

    // Find recurring incident patterns for predictive rerouting
    @Query("SELECT i.location as location, " +
            "i.incidentType as type, " +
            "FUNCTION('HOUR', i.dateTime) as peakHour, " +
            "FUNCTION('DAYOFWEEK', i.dateTime) as dayOfWeek, " +
            "COUNT(i) as occurrences, " +
            "AVG(TIMESTAMPDIFF(MINUTE, i.dateTime, i.resolutionTime)) as avgResolutionMinutes " +
            "FROM Incident i WHERE i.incidentType IN ('TRAFFIC_CONGESTION', 'ROAD_CLOSURE') " +
            "AND i.dateTime BETWEEN :start AND :end " +
            "AND i.resolutionTime IS NOT NULL " +
            "GROUP BY i.location, i.incidentType, FUNCTION('HOUR', i.dateTime), FUNCTION('DAYOFWEEK', i.dateTime) " +
            "HAVING COUNT(i) >= :minOccurrences " +
            "ORDER BY occurrences DESC")
    List<Map<String, Object>> getRecurringIncidentPatterns(@Param("start") LocalDateTime start,
                                                           @Param("end") LocalDateTime end,
                                                           @Param("minOccurrences") int minOccurrences);

    // Get real-time incident impact assessment
    @Query("SELECT i.location as affectedArea, " +
            "i.severity as severity, " +
            "i.incidentType as type, " +
            "TIMESTAMPDIFF(MINUTE, i.dateTime, CURRENT_TIMESTAMP) as durationMinutes, " +
            "i.additionalDetails as details " +
            "FROM Incident i WHERE i.status IN ('REPORTED', 'IN_PROGRESS') " +
            "AND i.incidentType IN ('TRAFFIC_CONGESTION', 'ROAD_CLOSURE', 'ACCIDENT', 'WEATHER_RELATED') " +
            "AND i.dateTime >= :since " +
            "ORDER BY i.severity DESC, durationMinutes DESC")
    List<Map<String, Object>> getCurrentRouteImpacts(@Param("since") LocalDateTime since);

    // Find incidents affecting multiple bus lines
    @Query("SELECT i.location as location, " +
            "COUNT(DISTINCT i.bus.busLine) as affectedLines, " +
            "COUNT(i) as totalIncidents, " +
            "MAX(i.severity) as maxSeverity, " +
            "MIN(i.dateTime) as firstReported " +
            "FROM Incident i WHERE i.status IN ('REPORTED', 'IN_PROGRESS') " +
            "AND i.incidentType IN ('TRAFFIC_CONGESTION', 'ROAD_CLOSURE', 'ACCIDENT') " +
            "AND i.dateTime >= :since " +
            "GROUP BY i.location " +
            "HAVING COUNT(DISTINCT i.bus.busLine) >= :minAffectedLines " +
            "ORDER BY affectedLines DESC, totalIncidents DESC")
    List<Map<String, Object>> getMultiLineImpactAreas(@Param("since") LocalDateTime since,
                                                      @Param("minAffectedLines") int minAffectedLines);

    // Get weather-related incidents for route planning
    @Query("SELECT i FROM Incident i WHERE i.incidentType = 'WEATHER_RELATED' " +
            "AND i.status IN ('REPORTED', 'IN_PROGRESS') " +
            "AND i.dateTime >= :since " +
            "ORDER BY i.severity DESC, i.dateTime DESC")
    List<Incident> findActiveWeatherIncidents(@Param("since") LocalDateTime since);

    // Calculate route reliability score based on incident history
    @Query("SELECT " +
            "COUNT(i) as totalIncidents, " +
            "SUM(CASE WHEN i.severity = 'CRITICAL' THEN 3 " +
            "    WHEN i.severity = 'HIGH' THEN 2 " +
            "    WHEN i.severity = 'MEDIUM' THEN 1 ELSE 0 END) as severityScore, " +
            "AVG(TIMESTAMPDIFF(MINUTE, i.dateTime, i.resolutionTime)) as avgResolutionTime " +
            "FROM Incident i WHERE i.bus.busLine = :busLine " +
            "AND i.dateTime BETWEEN :start AND :end " +
            "AND i.incidentType IN ('TRAFFIC_CONGESTION', 'ROAD_CLOSURE', 'ACCIDENT')")
    Map<String, Object> getRouteReliabilityMetrics(@Param("busLine") String busLine,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
}