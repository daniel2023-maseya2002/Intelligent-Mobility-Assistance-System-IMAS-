package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.ScheduleBus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleBusRepository extends JpaRepository<ScheduleBus, Long> {

    List<ScheduleBus> findByBusId(Long busId);

    List<ScheduleBus> findByDriverId(Long driverId);

    List<ScheduleBus> findByRouteId(Long routeId);

    List<ScheduleBus> findByDayOfWeek(DayOfWeek dayOfWeek);

    List<ScheduleBus> findByDayOfWeekAndIsActiveTrue(DayOfWeek dayOfWeek);

    List<ScheduleBus> findByIsActiveTrue();

    List<ScheduleBus> findByIsActiveFalse();

    List<ScheduleBus> findByDepartureTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<ScheduleBus> findByArrivalTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT s FROM ScheduleBus s WHERE s.departureTime >= :startDate AND s.departureTime <= :endDate AND s.isActive = true")
    List<ScheduleBus> findActiveSchedulesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM ScheduleBus s WHERE s.bus.id = :busId AND s.isActive = true")
    List<ScheduleBus> findActiveBusSchedules(@Param("busId") Long busId);

    @Query("SELECT s FROM ScheduleBus s WHERE s.driver.id = :driverId AND s.isActive = true")
    List<ScheduleBus> findActiveDriverSchedules(@Param("driverId") Long driverId);

    @Query("SELECT s FROM ScheduleBus s WHERE s.route.id = :routeId AND s.isActive = true")
    List<ScheduleBus> findActiveRouteSchedules(@Param("routeId") Long routeId);

    long countByIsActiveTrue();

    long countByIsActiveFalse();

    long countByDayOfWeekAndIsActiveTrue(DayOfWeek dayOfWeek);

    long countByBusIdAndIsActiveTrue(Long busId);

    long countByDriverIdAndIsActiveTrue(Long driverId);

    long countByRouteIdAndIsActiveTrue(Long routeId);

    @Query("SELECT COUNT(s) FROM ScheduleBus s WHERE s.departureTime >= :startDate AND s.departureTime <= :endDate AND s.isActive = true")
    long countActiveSchedulesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT s FROM ScheduleBus s WHERE s.departureTime >= :now AND s.isActive = true ORDER BY s.departureTime ASC")
    List<ScheduleBus> findUpcomingSchedules(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM ScheduleBus s WHERE s.departureTime <= :now AND s.arrivalTime >= :now AND s.isActive = true")
    List<ScheduleBus> findCurrentlyRunningSchedules(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM ScheduleBus s WHERE s.arrivalTime < :now AND s.isActive = true ORDER BY s.arrivalTime DESC")
    List<ScheduleBus> findCompletedSchedules(@Param("now") LocalDateTime now);
}