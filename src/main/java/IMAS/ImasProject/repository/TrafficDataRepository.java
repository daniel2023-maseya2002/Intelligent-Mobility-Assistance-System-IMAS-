package IMAS.ImasProject.repository;


import IMAS.ImasProject.model.TrafficData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrafficDataRepository extends JpaRepository<TrafficData, Long> {

    // Recherche par zone géographique
    @Query("SELECT td FROM TrafficData td WHERE " +
            "td.latitude BETWEEN :latMin AND :latMax AND " +
            "td.longitude BETWEEN :lonMin AND :lonMax AND " +
            "td.timestamp >= :since")
    List<TrafficData> findByGeographicAreaAndSince(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("lonMin") Double lonMin,
            @Param("lonMax") Double lonMax,
            @Param("since") LocalDateTime since
    );

    // Données les plus récentes pour une zone
    @Query("SELECT td FROM TrafficData td WHERE " +
            "td.latitude BETWEEN :lat - :radius AND :lat + :radius AND " +
            "td.longitude BETWEEN :lon - :radius AND :lon + :radius " +
            "ORDER BY td.timestamp DESC")
    List<TrafficData> findRecentByLocation(
            @Param("lat") Double latitude,
            @Param("lon") Double longitude,
            @Param("radius") Double radius
    );

    // Niveau de trafic moyen par heure pour ML
    @Query("SELECT td.hourOfDay, AVG(td.trafficLevel), AVG(td.averageSpeed) " +
            "FROM TrafficData td WHERE " +
            "td.latitude BETWEEN :latMin AND :latMax AND " +
            "td.longitude BETWEEN :lonMin AND :lonMax AND " +
            "td.timestamp >= :since " +
            "GROUP BY td.hourOfDay ORDER BY td.hourOfDay")
    List<Object[]> getAverageTrafficByHour(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("lonMin") Double lonMin,
            @Param("lonMax") Double lonMax,
            @Param("since") LocalDateTime since
    );

    // Données pour entraînement ML
    @Query("SELECT td FROM TrafficData td WHERE " +
            "td.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY td.timestamp ASC")
    List<TrafficData> findTrainingData(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // Points chauds de trafic
    @Query("SELECT td.latitude, td.longitude, AVG(td.trafficLevel) as avgLevel " +
            "FROM TrafficData td WHERE " +
            "td.timestamp >= :since " +
            "GROUP BY td.latitude, td.longitude " +
            "HAVING AVG(td.trafficLevel) >= :minLevel " +
            "ORDER BY avgLevel DESC")
    List<Object[]> findTrafficHotspots(
            @Param("since") LocalDateTime since,
            @Param("minLevel") Double minLevel
    );

    // Recherche par niveau de trafic
    List<TrafficData> findByTrafficLevelAndTimestampAfter(Integer trafficLevel, LocalDateTime timestamp);

    // Recherche par conditions météo
    List<TrafficData> findByWeatherConditionAndTimestampBetween(
            String weatherCondition, LocalDateTime start, LocalDateTime end);

    // Données des heures de pointe
    @Query("SELECT td FROM TrafficData td WHERE " +
            "(td.hourOfDay BETWEEN 7 AND 9 OR td.hourOfDay BETWEEN 17 AND 19) AND " +
            "td.dayOfWeek BETWEEN 1 AND 5 AND " +
            "td.timestamp >= :since")
    List<TrafficData> findRushHourData(@Param("since") LocalDateTime since);

    // Supprimer les anciennes données
    void deleteByTimestampBefore(LocalDateTime cutoffDate);

    // Compter les enregistrements par jour
    @Query("SELECT DATE(td.timestamp), COUNT(td) FROM TrafficData td " +
            "WHERE td.timestamp >= :since GROUP BY DATE(td.timestamp)")
    List<Object[]> countByDay(@Param("since") LocalDateTime since);
}
