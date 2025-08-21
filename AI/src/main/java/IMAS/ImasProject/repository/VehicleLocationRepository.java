package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.VehicleLocation;
import IMAS.ImasProject.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleLocationRepository extends JpaRepository<VehicleLocation, Long> {

    /**
     * Find all locations for a specific vehicle
     */
    List<VehicleLocation> findByVehicle(Vehicle vehicle);

    /**
     * Find all locations for a vehicle by vehicle ID
     */
    List<VehicleLocation> findByVehicleId(Long vehicleId);

    /**
     * Find latest location for a vehicle
     */
    @Query("SELECT vl FROM VehicleLocation vl WHERE vl.vehicle.id = :vehicleId ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findLatestLocationByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Find current location for a vehicle
     */
    Optional<VehicleLocation> findFirstByVehicleIdOrderByTimestampDesc(Long vehicleId);

    /**
     * Find locations within a time range for a vehicle
     */
    List<VehicleLocation> findByVehicleAndTimestampBetween(Vehicle vehicle, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find locations within a time range for a vehicle by ID
     */
    @Query("SELECT vl FROM VehicleLocation vl WHERE vl.vehicle.id = :vehicleId AND vl.timestamp BETWEEN :startTime AND :endTime ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findByVehicleIdAndTimestampBetween(@Param("vehicleId") Long vehicleId,
                                                             @Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * Find recent locations for a vehicle
     */
    @Query("SELECT vl FROM VehicleLocation vl WHERE vl.vehicle.id = :vehicleId AND vl.timestamp >= :sinceTime ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findRecentLocationsByVehicleId(@Param("vehicleId") Long vehicleId, @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Find all recent locations across all vehicles
     */
    @Query("SELECT vl FROM VehicleLocation vl WHERE vl.timestamp >= :sinceTime ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findAllRecentLocations(@Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Find locations by coordinate bounds
     */
    @Query("SELECT vl FROM VehicleLocation vl WHERE " +
            "vl.latitude BETWEEN :minLat AND :maxLat AND " +
            "vl.longitude BETWEEN :minLon AND :maxLon AND " +
            "vl.timestamp >= :sinceTime ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findLocationsInBounds(@Param("minLat") Double minLatitude,
                                                @Param("maxLat") Double maxLatitude,
                                                @Param("minLon") Double minLongitude,
                                                @Param("maxLon") Double maxLongitude,
                                                @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Find locations within radius of a point using Haversine formula
     */
    @Query(value = "SELECT * FROM vehicle_locations vl WHERE " +
            "vl.timestamp >= :sinceTime AND " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(vl.latitude)) * " +
            "cos(radians(vl.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
            "sin(radians(vl.latitude)))) <= :radiusKm " +
            "ORDER BY vl.timestamp DESC", nativeQuery = true)
    List<VehicleLocation> findLocationsInRadius(@Param("latitude") Double latitude,
                                                @Param("longitude") Double longitude,
                                                @Param("radiusKm") Double radiusKm,
                                                @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Find vehicles currently moving
     */
    @Query("SELECT vl FROM VehicleLocation vl WHERE vl.speed > :minSpeed AND vl.timestamp >= :sinceTime ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findMovingVehicles(@Param("minSpeed") Double minSpeed, @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Delete old location data
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VehicleLocation vl WHERE vl.timestamp < :cutoffTime")
    void deleteByTimestampBefore(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count locations for a vehicle
     */
    long countByVehicleId(Long vehicleId);

    /**
     * Count locations for a vehicle within time range
     */
    @Query("SELECT COUNT(vl) FROM VehicleLocation vl WHERE vl.vehicle.id = :vehicleId AND vl.timestamp BETWEEN :startTime AND :endTime")
    long countByVehicleIdAndTimestampBetween(@Param("vehicleId") Long vehicleId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * Get average speed for a vehicle in a time period
     */
    @Query("SELECT AVG(vl.speed) FROM VehicleLocation vl WHERE vl.vehicle.id = :vehicleId AND vl.timestamp BETWEEN :startTime AND :endTime AND vl.speed IS NOT NULL")
    Double getAverageSpeedByVehicleIdAndTimeRange(@Param("vehicleId") Long vehicleId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * Get maximum speed for a vehicle in a time period
     */
    @Query("SELECT MAX(vl.speed) FROM VehicleLocation vl WHERE vl.vehicle.id = :vehicleId AND vl.timestamp BETWEEN :startTime AND :endTime AND vl.speed IS NOT NULL")
    Double getMaxSpeedByVehicleIdAndTimeRange(@Param("vehicleId") Long vehicleId,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);

    /**
     * Get distance traveled by a vehicle in a time period (ordered route)
     */
    @Query("SELECT vl FROM VehicleLocation vl WHERE vl.vehicle.id = :vehicleId AND vl.timestamp BETWEEN :startTime AND :endTime ORDER BY vl.timestamp ASC")
    List<VehicleLocation> getVehicleRouteInTimeRange(@Param("vehicleId") Long vehicleId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * Find vehicles with locations in the last N minutes
     */
    @Query("SELECT DISTINCT vl.vehicle FROM VehicleLocation vl WHERE vl.timestamp >= :sinceTime")
    List<Vehicle> findVehiclesWithRecentLocations(@Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Get the most recent location for each vehicle
     */
    @Query("SELECT vl1 FROM VehicleLocation vl1 WHERE vl1.timestamp = " +
            "(SELECT MAX(vl2.timestamp) FROM VehicleLocation vl2 WHERE vl2.vehicle.id = vl1.vehicle.id)")
    List<VehicleLocation> findLatestLocationForAllVehicles();

    /**
     * Find locations with high passenger count
     */
    @Query("SELECT vl FROM VehicleLocation vl WHERE vl.passengerCount >= :minPassengers AND vl.timestamp >= :sinceTime ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findLocationsWithHighPassengerCount(@Param("minPassengers") Integer minPassengers,
                                                              @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Get total distance traveled by all vehicles in time range
     */
    @Query("SELECT SUM(" +
            "  6371 * acos(cos(radians(vl1.latitude)) * cos(radians(vl2.latitude)) * " +
            "  cos(radians(vl2.longitude) - radians(vl1.longitude)) + sin(radians(vl1.latitude)) * " +
            "  sin(radians(vl2.latitude)))" +
            ") FROM VehicleLocation vl1, VehicleLocation vl2 " +
            "WHERE vl1.vehicle.id = vl2.vehicle.id " +
            "AND vl1.timestamp BETWEEN :startTime AND :endTime " +
            "AND vl2.timestamp BETWEEN :startTime AND :endTime " +
            "AND vl2.timestamp > vl1.timestamp")
    Double getTotalDistanceTraveledInTimeRange(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * Find locations with accuracy better than threshold
     */
    @Query("SELECT vl FROM VehicleLocation vl WHERE vl.accuracy <= :maxAccuracy AND vl.timestamp >= :sinceTime ORDER BY vl.timestamp DESC")
    List<VehicleLocation> findLocationsWithGoodAccuracy(@Param("maxAccuracy") Double maxAccuracy,
                                                        @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Get locations for multiple vehicles
     */
    @Query("SELECT vl FROM VehicleLocation vl WHERE vl.vehicle.id IN :vehicleIds AND vl.timestamp >= :sinceTime ORDER BY vl.vehicle.id, vl.timestamp DESC")
    List<VehicleLocation> findLocationsByVehicleIds(@Param("vehicleIds") List<Long> vehicleIds,
                                                    @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Clean up locations older than specified date with batch processing
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM vehicle_locations WHERE timestamp < :cutoffTime LIMIT :batchSize", nativeQuery = true)
    int deleteOldLocationsBatch(@Param("cutoffTime") LocalDateTime cutoffTime, @Param("batchSize") int batchSize);

    /**
     * Find stationary vehicles (speed = 0 or null for extended period)
     */
    @Query("SELECT DISTINCT vl.vehicle FROM VehicleLocation vl WHERE " +
            "vl.timestamp >= :sinceTime AND " +
            "(vl.speed IS NULL OR vl.speed = 0) AND " +
            "NOT EXISTS (SELECT vl2 FROM VehicleLocation vl2 WHERE vl2.vehicle.id = vl.vehicle.id " +
            "AND vl2.timestamp >= :sinceTime AND vl2.speed > 0)")
    List<Vehicle> findStationaryVehicles(@Param("sinceTime") LocalDateTime sinceTime);

    /**
     * Get location statistics for a vehicle
     */
    @Query("SELECT " +
            "COUNT(vl) as totalLocations, " +
            "AVG(vl.speed) as avgSpeed, " +
            "MAX(vl.speed) as maxSpeed, " +
            "AVG(vl.passengerCount) as avgPassengers, " +
            "MAX(vl.passengerCount) as maxPassengers " +
            "FROM VehicleLocation vl WHERE vl.vehicle.id = :vehicleId AND vl.timestamp BETWEEN :startTime AND :endTime")
    Object[] getLocationStatistics(@Param("vehicleId") Long vehicleId,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);
}