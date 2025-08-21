package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Arrival;
import IMAS.ImasProject.model.ArrivalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArrivalRepository extends JpaRepository<Arrival, Long> {

    // Find arrivals by stop
    List<Arrival> findByStopIdAndIsActiveTrue(Long stopId);

    Page<Arrival> findByStopIdAndIsActiveTrue(Long stopId, Pageable pageable);

    // Find arrivals by vehicle
    List<Arrival> findByVehicleIdAndIsActiveTrue(Long vehicleId);

    Page<Arrival> findByVehicleIdAndIsActiveTrue(Long vehicleId, Pageable pageable);

    // Find arrivals by status
    List<Arrival> findByStatusAndIsActiveTrue(ArrivalStatus status);

    Page<Arrival> findByStatusAndIsActiveTrue(ArrivalStatus status, Pageable pageable);

    // Find arrivals by date range
    @Query("SELECT a FROM Arrival a WHERE a.scheduledArrival BETWEEN :startDate AND :endDate AND a.isActive = true")
    List<Arrival> findByScheduledArrivalBetweenAndIsActiveTrue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Arrival a WHERE a.scheduledArrival BETWEEN :startDate AND :endDate AND a.isActive = true")
    Page<Arrival> findByScheduledArrivalBetweenAndIsActiveTrue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Find upcoming arrivals for a stop
    @Query("SELECT a FROM Arrival a WHERE a.stop.id = :stopId AND a.scheduledArrival > :now AND a.isCancelled = false AND a.isActive = true ORDER BY a.scheduledArrival ASC")
    List<Arrival> findUpcomingArrivalsByStop(@Param("stopId") Long stopId, @Param("now") LocalDateTime now);

    // Find delayed arrivals
    @Query("SELECT a FROM Arrival a WHERE a.delay > :delayThreshold AND a.isActive = true")
    List<Arrival> findDelayedArrivals(@Param("delayThreshold") Integer delayThreshold);

    // Find arrivals by stop and date
    @Query("SELECT a FROM Arrival a WHERE a.stop.id = :stopId AND DATE(a.scheduledArrival) = DATE(:date) AND a.isActive = true ORDER BY a.scheduledArrival")
    List<Arrival> findByStopAndDate(@Param("stopId") Long stopId, @Param("date") LocalDateTime date);

    // Find arrivals by vehicle and date range
    @Query("SELECT a FROM Arrival a WHERE a.vehicle.id = :vehicleId AND a.scheduledArrival BETWEEN :startDate AND :endDate AND a.isActive = true ORDER BY a.scheduledArrival")
    List<Arrival> findByVehicleAndDateRange(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Find cancelled arrivals
    List<Arrival> findByIsCancelledTrueAndIsActiveTrue();

    // Find arrivals within distance
    @Query("SELECT a FROM Arrival a WHERE a.distanceFromStop <= :distance AND a.isActive = true")
    List<Arrival> findArrivalsWithinDistance(@Param("distance") Double distance);

    // Performance analytics queries
    @Query("SELECT AVG(a.delay) FROM Arrival a WHERE a.delay IS NOT NULL AND a.stop.id = :stopId AND a.scheduledArrival BETWEEN :startDate AND :endDate")
    Double getAverageDelayByStop(@Param("stopId") Long stopId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(a.delay) FROM Arrival a WHERE a.delay IS NOT NULL AND a.vehicle.id = :vehicleId AND a.scheduledArrival BETWEEN :startDate AND :endDate")
    Double getAverageDelayByVehicle(@Param("vehicleId") Long vehicleId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(a) FROM Arrival a WHERE a.status = :status AND a.scheduledArrival BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(@Param("status") ArrivalStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find next arrival for a stop
    @Query("SELECT a FROM Arrival a WHERE a.stop.id = :stopId AND a.scheduledArrival > :now AND a.isCancelled = false AND a.isActive = true ORDER BY a.scheduledArrival ASC LIMIT 1")
    Optional<Arrival> findNextArrivalByStop(@Param("stopId") Long stopId, @Param("now") LocalDateTime now);
}
