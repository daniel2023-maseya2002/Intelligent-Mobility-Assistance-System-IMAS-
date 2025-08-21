package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {

    /**
     * Find bus by driver ID
     */
    @Query("SELECT b FROM Bus b WHERE b.driver.id = :driverId")
    Bus findByDriverId(@Param("driverId") Long driverId);

    /**
     * Find all buses with driver and route information loaded
     */
    @Query("SELECT DISTINCT b FROM Bus b " +
            "LEFT JOIN FETCH b.driver " +
            "LEFT JOIN FETCH b.route")
    List<Bus> findAllWithDriverAndRoute();

    /**
     * Find bus by ID with driver and route information loaded
     */
    @Query("SELECT b FROM Bus b " +
            "LEFT JOIN FETCH b.driver " +
            "LEFT JOIN FETCH b.route " +
            "WHERE b.id = :id")
    Bus findByIdWithDriverAndRoute(@Param("id") Long id);

    /**
     * Find buses by route ID
     */
    @Query("SELECT b FROM Bus b WHERE b.route.id = :routeId")
    List<Bus> findByRouteId(@Param("routeId") Long routeId);

    /**
     * Find active buses (not stopped)
     */
    @Query("SELECT b FROM Bus b WHERE b.isStopped = false")
    List<Bus> findByIsStoppedFalse();

    /**
     * Find stopped buses
     */
    @Query("SELECT b FROM Bus b WHERE b.isStopped = true")
    List<Bus> findByIsStoppedTrue();

    /**
     * Find buses with accidents
     */
    @Query("SELECT b FROM Bus b WHERE b.hasAccident = true")
    List<Bus> findByHasAccidentTrue();

    /**
     * Find buses by departure time range
     */
    @Query("SELECT b FROM Bus b WHERE b.departureTime BETWEEN :startTime AND :endTime")
    List<Bus> findByDepartureTimeBetween(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * Find buses arriving soon (within specified minutes)
     */
    @Query("SELECT b FROM Bus b WHERE b.arrivalTime BETWEEN :now AND :futureTime " +
            "AND b.isStopped = false AND b.progress < 100.0")
    List<Bus> findBusesArrivingSoon(@Param("now") LocalDateTime now,
                                    @Param("futureTime") LocalDateTime futureTime);

    /**
     * Find buses that completed their route (handled in service layer for better control)
     * Removing the complex date arithmetic to avoid JPQL compatibility issues
     */

    /**
     * Find buses by capacity range
     */
    @Query("SELECT b FROM Bus b WHERE b.capacity BETWEEN :minCapacity AND :maxCapacity")
    List<Bus> findByCapacityBetween(@Param("minCapacity") Integer minCapacity,
                                    @Param("maxCapacity") Integer maxCapacity);

    /**
     * Find buses with high occupancy rate
     */
    @Query("SELECT b FROM Bus b WHERE (CAST(b.passengers AS double) / CAST(b.capacity AS double)) * 100 > :occupancyThreshold")
    List<Bus> findHighOccupancyBuses(@Param("occupancyThreshold") Double occupancyThreshold);

    /**
     * Find buses by progress range
     */
    @Query("SELECT b FROM Bus b WHERE b.progress BETWEEN :minProgress AND :maxProgress")
    List<Bus> findByProgressBetween(@Param("minProgress") Double minProgress,
                                    @Param("maxProgress") Double maxProgress);

    /**
     * Find buses by bus line
     */
    @Query("SELECT b FROM Bus b WHERE b.busLine = :busLine")
    List<Bus> findByBusLine(@Param("busLine") String busLine);

    /**
     * Find available buses (no accidents, stopped, no passengers)
     */
    @Query("SELECT b FROM Bus b WHERE b.hasAccident = false " +
            "AND b.isStopped = true " +
            "AND (b.passengers = 0 OR b.passengers IS NULL)")
    List<Bus> findAvailableBuses();

    /**
     * Count buses by status
     */
    @Query("SELECT COUNT(b) FROM Bus b WHERE b.isStopped = :isStopped")
    Long countByStoppedStatus(@Param("isStopped") Boolean isStopped);

    /**
     * Count buses with accidents
     */
    @Query("SELECT COUNT(b) FROM Bus b WHERE b.hasAccident = true")
    Long countBusesWithAccidents();

    /**
     * Find buses created within date range
     */
    @Query("SELECT b FROM Bus b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    List<Bus> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find buses by driver name (first name or last name)
     */
    @Query("SELECT b FROM Bus b WHERE " +
            "LOWER(b.driver.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(b.driver.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Bus> findByDriverName(@Param("name") String name);

    /**
     * Find buses currently running (not stopped and progress > 0)
     */
    @Query("SELECT b FROM Bus b WHERE b.isStopped = false AND b.progress > 0.0")
    List<Bus> findCurrentlyRunning();

    /**
     * Find buses that completed their route today
     */
    @Query("SELECT b FROM Bus b WHERE b.progress >= 100.0 " +
            "AND DATE(b.arrivalTime) = CURRENT_DATE")
    List<Bus> findCompletedToday();

    /**
     * Custom method to find buses with specific criteria
     */
    @Query("SELECT b FROM Bus b WHERE " +
            "(:busLine IS NULL OR b.busLine = :busLine) AND " +
            "(:hasAccident IS NULL OR b.hasAccident = :hasAccident) AND " +
            "(:isStopped IS NULL OR b.isStopped = :isStopped) AND " +
            "(:driverId IS NULL OR b.driver.id = :driverId)")
    List<Bus> findBusesWithCriteria(@Param("busLine") String busLine,
                                    @Param("hasAccident") Boolean hasAccident,
                                    @Param("isStopped") Boolean isStopped,
                                    @Param("driverId") Long driverId);

    /**
     * Find the most recent bus for a specific route
     */
    @Query("SELECT b FROM Bus b WHERE b.route.id = :routeId " +
            "ORDER BY b.departureTime DESC")
    List<Bus> findMostRecentByRoute(@Param("routeId") Long routeId);

    // Add this method to your BusRepository interface
    List<Bus> findAllByDriverId(Long driverId);




    /**
     * Find buses that need maintenance (completed many trips or have accidents)
     */
    @Query("SELECT b FROM Bus b WHERE b.hasAccident = true " +
            "OR (SELECT COUNT(t) FROM Bus t WHERE t.name = b.name AND t.progress >= 100.0) > 10")
    List<Bus> findBusesNeedingMaintenance();
}