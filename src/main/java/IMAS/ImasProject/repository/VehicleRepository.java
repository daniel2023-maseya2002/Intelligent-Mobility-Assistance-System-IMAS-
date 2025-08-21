    package IMAS.ImasProject.repository;

    import IMAS.ImasProject.model.Vehicle;
    import IMAS.ImasProject.model.VehicleStatus;
    import IMAS.ImasProject.model.Route;
    import IMAS.ImasProject.model.FuelType;
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
    public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

        /**
         * Find vehicle by vehicle number
         */
        Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

        /**
         * Find vehicle by license plate
         */
        Optional<Vehicle> findByLicensePlate(String licensePlate);

        /**
         * Find all vehicles by status
         */
        List<Vehicle> findByStatus(VehicleStatus status);

        /**
         * Find all vehicles by status with pagination
         */
        Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

        /**
         * Find all active vehicles
         */
        List<Vehicle> findByIsActiveTrue();

        /**
         * Find all inactive vehicles
         */
        List<Vehicle> findByIsActiveFalse();

        /**
         * Find vehicles by route
         */
        List<Vehicle> findByRoute(Route route);

        /**
         * Find vehicles by route ID
         */
        List<Vehicle> findByRouteId(Long routeId);

        /**
         * Find vehicles by capacity range
         */
        List<Vehicle> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);

        /**
         * Find vehicles with capacity greater than or equal to specified value
         */
        List<Vehicle> findByCapacityGreaterThanEqual(Integer minCapacity);

        /**
         * Find accessible vehicles
         */
        List<Vehicle> findByIsAccessibleTrue();

        /**
         * Find vehicles by type
         */
        List<Vehicle> findByVehicleType(String vehicleType);

        /**
         * Find vehicles by fuel type
         */
        List<Vehicle> findByFuelType(FuelType fuelType);

        /**
         * Find vehicles requiring maintenance (before a specific date)
         */
        List<Vehicle> findByNextMaintenanceBefore(LocalDateTime dateTime);

        /**
         * Find vehicles by status and isActive=true
         */
        List<Vehicle> findByStatusAndIsActiveTrue(VehicleStatus status);

        /**
         * Find vehicles requiring maintenance
         */
        @Query("SELECT v FROM Vehicle v WHERE v.nextMaintenance IS NOT NULL AND v.nextMaintenance <= :currentTime")
        List<Vehicle> findVehiclesRequiringMaintenance(@Param("currentTime") LocalDateTime currentTime);

        /**
         * Find vehicles overdue for maintenance
         */
        @Query("SELECT v FROM Vehicle v WHERE v.nextMaintenance IS NOT NULL AND v.nextMaintenance < :currentTime")
        List<Vehicle> findVehiclesOverdueForMaintenance(@Param("currentTime") LocalDateTime currentTime);

        /**
         * Find operational vehicles (active and not in maintenance)
         */
        @Query("SELECT v FROM Vehicle v WHERE v.isActive = true AND v.status IN ('ACTIVE', 'IN_TRANSIT')")
        List<Vehicle> findOperationalVehicles();

        /**
         * Find available vehicles (active and not assigned to route)
         */
        @Query("SELECT v FROM Vehicle v WHERE v.isActive = true AND v.status = 'INACTIVE' AND v.route IS NULL")
        List<Vehicle> findAvailableVehicles();

        /**
         * Find vehicles with GPS
         */
        List<Vehicle> findByHasGpsTrue();

        /**
         * Find vehicles without GPS
         */
        List<Vehicle> findByHasGpsFalse();

        /**
         * Find vehicles without assigned route
         */
        List<Vehicle> findByRouteIsNull();

        /**
         * Find vehicles with assigned route
         */
        List<Vehicle> findByRouteIsNotNull();


        /**
         * Find vehicles by manufacturer and model
         */
        List<Vehicle> findByManufacturerAndModel(String manufacturer, String model);

        /**
         * Find vehicles by manufacturer
         */
        List<Vehicle> findByManufacturer(String manufacturer);

        /**
         * Find vehicles by model
         */
        List<Vehicle> findByModel(String model);

        /**
         * Find vehicles by year range
         */
        List<Vehicle> findByYearBetween(Integer startYear, Integer endYear);

        /**
         * Find vehicles by year
         */
        List<Vehicle> findByYear(Integer year);

        /**
         * Find vehicles with air conditioning
         */
        List<Vehicle> findByHasAirConditioningTrue();

        /**
         * Find vehicles with WiFi
         */
        List<Vehicle> findByHasWifiTrue();

        /**
         * Count vehicles by status
         */
        long countByStatus(VehicleStatus status);

        /**
         * Count active vehicles
         */
        long countByIsActiveTrue();

        /**
         * Count inactive vehicles
         */
        long countByIsActiveFalse();

        /**
         * Count accessible vehicles
         */
        long countByIsAccessibleTrue();

        /**
         * Count vehicles with WiFi
         */
        long countByHasWifiTrue();

        /**
         * Count vehicles by route
         */
        long countByRouteId(Long routeId);

        /**
         * Count vehicles without route
         */
        long countByRouteIsNull();

        /**
         * Count vehicles by route (alternative method name)
         */
        @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.route.id = :routeId")
        long countVehiclesByRoute(@Param("routeId") Long routeId);

        /**
         * Check if vehicle number exists
         */
        boolean existsByVehicleNumber(String vehicleNumber);

        /**
         * Check if license plate exists
         */
        boolean existsByLicensePlate(String licensePlate);

        /**
         * Find vehicle with minimum capacity
         */
        @Query("SELECT v FROM Vehicle v WHERE v.capacity = (SELECT MIN(v2.capacity) FROM Vehicle v2)")
        List<Vehicle> findMinCapacity();

        /**
         * Find vehicle with maximum capacity
         */
        @Query("SELECT v FROM Vehicle v WHERE v.capacity = (SELECT MAX(v2.capacity) FROM Vehicle v2)")
        List<Vehicle> findMaxCapacity();





        /**
         * Get vehicle statistics
         */
        @Query("SELECT " +
                "COUNT(v) as totalVehicles, " +
                "SUM(CASE WHEN v.isActive = true THEN 1 ELSE 0 END) as activeVehicles, " +
                "SUM(CASE WHEN v.status = 'IN_TRANSIT' THEN 1 ELSE 0 END) as vehiclesInTransit, " +
                "SUM(CASE WHEN v.status = 'MAINTENANCE' THEN 1 ELSE 0 END) as vehiclesInMaintenance, " +
                "SUM(CASE WHEN v.status = 'BREAKDOWN' THEN 1 ELSE 0 END) as vehiclesInBreakdown, " +
                "SUM(CASE WHEN v.status = 'ACTIVE' THEN 1 ELSE 0 END) as activeReadyVehicles " +
                "FROM Vehicle v")
        Object[] getVehicleStatistics();



        /**
         * Get capacity statistics
         */
        @Query("SELECT " +
                "SUM(v.capacity) as totalCapacity, " +
                "AVG(v.capacity) as averageCapacity, " +
                "MIN(v.capacity) as minCapacity, " +
                "MAX(v.capacity) as maxCapacity " +
                "FROM Vehicle v WHERE v.isActive = true")
        Object[] getCapacityStatistics();

        /**
         * Find vehicles by multiple criteria
         */
        @Query("SELECT v FROM Vehicle v WHERE " +
                "(:status IS NULL OR v.status = :status) AND " +
                "(:isActive IS NULL OR v.isActive = :isActive) AND " +
                "(:vehicleType IS NULL OR v.vehicleType = :vehicleType) AND " +
                "(:routeId IS NULL OR v.route.id = :routeId) AND " +
                "(:minCapacity IS NULL OR v.capacity >= :minCapacity) AND " +
                "(:maxCapacity IS NULL OR v.capacity <= :maxCapacity) AND " +
                "(:isAccessible IS NULL OR v.isAccessible = :isAccessible) AND " +
                "(:hasGps IS NULL OR v.hasGps = :hasGps)")
        List<Vehicle> findVehiclesByCriteria(@Param("status") VehicleStatus status,
                                             @Param("isActive") Boolean isActive,
                                             @Param("vehicleType") String vehicleType,
                                             @Param("routeId") Long routeId,
                                             @Param("minCapacity") Integer minCapacity,
                                             @Param("maxCapacity") Integer maxCapacity,
                                             @Param("isAccessible") Boolean isAccessible,
                                             @Param("hasGps") Boolean hasGps);

        /**
         * Find vehicles due for maintenance in next N days
         */
        @Query("SELECT v FROM Vehicle v WHERE v.nextMaintenance IS NOT NULL AND v.nextMaintenance BETWEEN :currentTime AND :futureTime")
        List<Vehicle> findVehiclesDueForMaintenanceInDays(@Param("currentTime") LocalDateTime currentTime,
                                                          @Param("futureTime") LocalDateTime futureTime);

        /**
         * Find oldest vehicles
         */
        @Query("SELECT v FROM Vehicle v WHERE v.year IS NOT NULL ORDER BY v.year ASC")
        List<Vehicle> findOldestVehicles(Pageable pageable);

        /**
         * Find newest vehicles
         */
        @Query("SELECT v FROM Vehicle v WHERE v.year IS NOT NULL ORDER BY v.year DESC")
        List<Vehicle> findNewestVehicles(Pageable pageable);

        /**
         * Search vehicles by keyword (vehicle number, license plate, manufacturer, model)
         */
        @Query("SELECT v FROM Vehicle v WHERE " +
                "LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                "LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                "LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                "LOWER(v.model) LIKE LOWER(CONCAT('%', :keyword, '%'))")
        List<Vehicle> searchVehicles(@Param("keyword") String keyword);

        /**
         * Find vehicles with expired or missing maintenance dates
         */
        @Query("SELECT v FROM Vehicle v WHERE v.nextMaintenance IS NULL OR v.nextMaintenance < :currentTime")
        List<Vehicle> findVehiclesWithMaintenanceIssues(@Param("currentTime") LocalDateTime currentTime);

        /**
         * Find vehicles by odometer reading range
         */
        @Query("SELECT v FROM Vehicle v WHERE v.odometerReading BETWEEN :minReading AND :maxReading")
        List<Vehicle> findVehiclesByOdometerRange(@Param("minReading") Double minReading,
                                                  @Param("maxReading") Double maxReading);

        /**
         * Find vehicles with high odometer readings
         */
        @Query("SELECT v FROM Vehicle v WHERE v.odometerReading > :threshold ORDER BY v.odometerReading DESC")
        List<Vehicle> findVehiclesWithHighOdometer(@Param("threshold") Double threshold);

        /**
         * Get vehicles grouped by manufacturer
         */
        @Query("SELECT v.manufacturer, COUNT(v) FROM Vehicle v GROUP BY v.manufacturer ORDER BY COUNT(v) DESC")
        List<Object[]> getVehicleCountByManufacturer();

        /**
         * Get vehicles grouped by vehicle type
         */
        @Query("SELECT v.vehicleType, COUNT(v) FROM Vehicle v GROUP BY v.vehicleType ORDER BY COUNT(v) DESC")
        List<Object[]> getVehicleCountByType();

        /**
         * Get vehicles grouped by fuel type
         */
        @Query("SELECT v.fuelType, COUNT(v) FROM Vehicle v WHERE v.fuelType IS NOT NULL GROUP BY v.fuelType ORDER BY COUNT(v) DESC")
        List<Object[]> getVehicleCountByFuelType();

        /**
         * Additional helper methods for better functionality
         */

        /**
         * Find vehicles by capacity exactly
         */
        List<Vehicle> findByCapacity(Integer capacity);

        /**
         * Find vehicles with capacity less than specified value
         */
        List<Vehicle> findByCapacityLessThan(Integer maxCapacity);

        /**
         * Find vehicles with capacity greater than specified value
         */
        List<Vehicle> findByCapacityGreaterThan(Integer minCapacity);

        /**
         * Find non-accessible vehicles
         */
        List<Vehicle> findByIsAccessibleFalse();

        /**
         * Find vehicles without air conditioning
         */
        List<Vehicle> findByHasAirConditioningFalse();

        /**
         * Find vehicles without WiFi
         */
        List<Vehicle> findByHasWifiFalse();

        /**
         * Count vehicles with air conditioning
         */
        long countByHasAirConditioningTrue();

        /**
         * Count vehicles with GPS
         */
        long countByHasGpsTrue();

        /**
         * Count vehicles without GPS
         */
        long countByHasGpsFalse();

        /**
         * Count vehicles by fuel type
         */
        long countByFuelType(FuelType fuelType);

        /**
         * Count vehicles by vehicle type
         */
        long countByVehicleType(String vehicleType);

        /**
         * Count vehicles by manufacturer
         */
        long countByManufacturer(String manufacturer);

        /**
         * Count vehicles by year
         */
        long countByYear(Integer year);

        /**
         * Find vehicles requiring maintenance soon (within next N days)
         */
        @Query("SELECT v FROM Vehicle v WHERE v.nextMaintenance IS NOT NULL AND " +
                "v.nextMaintenance BETWEEN :now AND :futureDate")
        List<Vehicle> findVehiclesRequiringMaintenanceSoon(@Param("now") LocalDateTime now,
                                                           @Param("futureDate") LocalDateTime futureDate);

        /**
         * Find vehicles never maintained
         */
        List<Vehicle> findByLastMaintenanceIsNull();

        /**
         * Find vehicles with next maintenance scheduled
         */
        List<Vehicle> findByNextMaintenanceIsNotNull();

        /**
         * Find vehicles without next maintenance scheduled
         */
        List<Vehicle> findByNextMaintenanceIsNull();

        /**
         * Get average vehicle age
         */
        @Query("SELECT AVG(YEAR(CURRENT_DATE) - v.year) FROM Vehicle v WHERE v.year IS NOT NULL")
        Double getAverageVehicleAge();

        /**
         * Find vehicles older than specified years
         */
        @Query("SELECT v FROM Vehicle v WHERE v.year IS NOT NULL AND (YEAR(CURRENT_DATE) - v.year) > :years")
        List<Vehicle> findVehiclesOlderThan(@Param("years") Integer years);

        /**
         * Find vehicles newer than specified years
         */
        @Query("SELECT v FROM Vehicle v WHERE v.year IS NOT NULL AND (YEAR(CURRENT_DATE) - v.year) <= :years")
        List<Vehicle> findVehiclesNewerThan(@Param("years") Integer years);

        /**
         * Get total fleet capacity
         */
        @Query("SELECT SUM(v.capacity) FROM Vehicle v WHERE v.isActive = true")
        Long getTotalFleetCapacity();

        /**
         * Get total number of active vehicles
         */
        @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.isActive = true")
        Long getTotalActiveVehicles();

        /**
         * Find vehicles by route name
         */
        @Query("SELECT v FROM Vehicle v WHERE v.route.routeName = :routeName")
        List<Vehicle> findByRouteName(@Param("routeName") String routeName);

        /**
         * Find vehicles by route code
         */
        @Query("SELECT v FROM Vehicle v WHERE v.route.routeCode = :routeCode")
        List<Vehicle> findByRouteCode(@Param("routeCode") String routeCode);


        // Ajoutez ces méthodes à votre VehicleRepository

        /**
         * Count operational vehicles (active and not in maintenance)
         */
        @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.isActive = true AND v.status IN ('ACTIVE', 'IN_TRANSIT')")
        long countOperationalVehicles();

        /**
         * Count vehicles requiring maintenance
         */
        @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.nextMaintenance IS NOT NULL AND v.nextMaintenance <= :currentTime")
        long countVehiclesRequiringMaintenance(@Param("currentTime") LocalDateTime currentTime);

        /**
         * Count vehicles overdue for maintenance
         */
        @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.nextMaintenance IS NOT NULL AND v.nextMaintenance < :currentTime")
        long countVehiclesOverdueForMaintenance(@Param("currentTime") LocalDateTime currentTime);

        /**
         * Count vehicles with assigned route
         */
        long countByRouteIsNotNull();

        /**
         * Get average capacity as Double
         */
        @Query("SELECT AVG(v.capacity) FROM Vehicle v WHERE v.isActive = true")
        Double findAverageCapacity();



        // Ajoutez ces méthodes à votre VehicleRepository

        /**
         * Count vehicles by type
         */
        @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.vehicleType = :vehicleType")
        long countVehiclesByType(@Param("vehicleType") String vehicleType);


        /**
         * Find vehicles with multiple filters
         */
        @Query("SELECT v FROM Vehicle v WHERE " +
                "(:status IS NULL OR v.status = :status) AND " +
                "(:isActive IS NULL OR v.isActive = :isActive) AND " +
                "(:vehicleType IS NULL OR LOWER(v.vehicleType) LIKE LOWER(CONCAT('%', :vehicleType, '%'))) AND " +
                "(:routeId IS NULL OR v.route.id = :routeId) AND " +
                "(:minCapacity IS NULL OR v.capacity >= :minCapacity) AND " +
                "(:maxCapacity IS NULL OR v.capacity <= :maxCapacity) AND " +
                "(:isAccessible IS NULL OR v.isAccessible = :isAccessible) AND " +
                "(:hasGps IS NULL OR v.hasGps = :hasGps) AND " +
                "(:hasWifi IS NULL OR v.hasWifi = :hasWifi) AND " +
                "(:hasAirConditioning IS NULL OR v.hasAirConditioning = :hasAirConditioning) AND " +
                "(:manufacturer IS NULL OR LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) AND " +
                "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
                "(:fuelType IS NULL OR v.fuelType = :fuelType) AND " +
                "(:keyword IS NULL OR " +
                " LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                " LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                " LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                " LOWER(v.model) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        List<Vehicle> findVehiclesWithFilters(@Param("status") VehicleStatus status,
                                              @Param("isActive") Boolean isActive,
                                              @Param("vehicleType") String vehicleType,
                                              @Param("routeId") Long routeId,
                                              @Param("minCapacity") Integer minCapacity,
                                              @Param("maxCapacity") Integer maxCapacity,
                                              @Param("isAccessible") Boolean isAccessible,
                                              @Param("hasGps") Boolean hasGps,
                                              @Param("hasWifi") Boolean hasWifi,
                                              @Param("hasAirConditioning") Boolean hasAirConditioning,
                                              @Param("manufacturer") String manufacturer,
                                              @Param("model") String model,
                                              @Param("fuelType") FuelType fuelType,
                                              @Param("keyword") String keyword);

        /**
         * Find vehicles with filters and pagination
         */
        @Query("SELECT v FROM Vehicle v WHERE " +
                "(:status IS NULL OR v.status = :status) AND " +
                "(:isActive IS NULL OR v.isActive = :isActive) AND " +
                "(:vehicleType IS NULL OR LOWER(v.vehicleType) LIKE LOWER(CONCAT('%', :vehicleType, '%'))) AND " +
                "(:routeId IS NULL OR v.route.id = :routeId) AND " +
                "(:minCapacity IS NULL OR v.capacity >= :minCapacity) AND " +
                "(:maxCapacity IS NULL OR v.capacity <= :maxCapacity) AND " +
                "(:isAccessible IS NULL OR v.isAccessible = :isAccessible) AND " +
                "(:hasGps IS NULL OR v.hasGps = :hasGps) AND " +
                "(:hasWifi IS NULL OR v.hasWifi = :hasWifi) AND " +
                "(:hasAirConditioning IS NULL OR v.hasAirConditioning = :hasAirConditioning) AND " +
                "(:manufacturer IS NULL OR LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) AND " +
                "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
                "(:fuelType IS NULL OR v.fuelType = :fuelType) AND " +
                "(:keyword IS NULL OR " +
                " LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                " LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                " LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                " LOWER(v.model) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Vehicle> findVehiclesWithFilters(@Param("status") VehicleStatus status,
                                              @Param("isActive") Boolean isActive,
                                              @Param("vehicleType") String vehicleType,
                                              @Param("routeId") Long routeId,
                                              @Param("minCapacity") Integer minCapacity,
                                              @Param("maxCapacity") Integer maxCapacity,
                                              @Param("isAccessible") Boolean isAccessible,
                                              @Param("hasGps") Boolean hasGps,
                                              @Param("hasWifi") Boolean hasWifi,
                                              @Param("hasAirConditioning") Boolean hasAirConditioning,
                                              @Param("manufacturer") String manufacturer,
                                              @Param("model") String model,
                                              @Param("fuelType") FuelType fuelType,
                                              @Param("keyword") String keyword,
                                              Pageable pageable);

        /**
         * Find distinct vehicle types
         */
        @Query("SELECT DISTINCT v.vehicleType FROM Vehicle v WHERE v.vehicleType IS NOT NULL ORDER BY v.vehicleType")
        List<String> findDistinctVehicleTypes();

        /**
         * Find distinct manufacturers
         */
        @Query("SELECT DISTINCT v.manufacturer FROM Vehicle v WHERE v.manufacturer IS NOT NULL ORDER BY v.manufacturer")
        List<String> findDistinctManufacturers();

        /**
         * Find distinct models
         */
        @Query("SELECT DISTINCT v.model FROM Vehicle v WHERE v.model IS NOT NULL ORDER BY v.model")
        List<String> findDistinctModels();

        /**
         * Find distinct fuel types
         */
        @Query("SELECT DISTINCT v.fuelType FROM Vehicle v WHERE v.fuelType IS NOT NULL ORDER BY v.fuelType")
        List<FuelType> findDistinctFuelTypes();

        /**
         * Find distinct years
         */
        @Query("SELECT DISTINCT v.year FROM Vehicle v WHERE v.year IS NOT NULL ORDER BY v.year DESC")
        List<Integer> findDistinctYears();

        /**
         * Advanced search with multiple criteria
         */
        @Query("SELECT v FROM Vehicle v WHERE " +
                "(:searchTerm IS NULL OR " +
                " LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                " LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                " LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                " LOWER(v.model) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                " LOWER(v.vehicleType) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
                "(:statuses IS NULL OR v.status IN :statuses) AND " +
                "(:isActive IS NULL OR v.isActive = :isActive)")
        List<Vehicle> findVehiclesByAdvancedSearch(@Param("searchTerm") String searchTerm,
                                                   @Param("statuses") List<VehicleStatus> statuses,
                                                   @Param("isActive") Boolean isActive);

        /**
         * Count vehicles by multiple criteria
         */
        @Query("SELECT COUNT(v) FROM Vehicle v WHERE " +
                "(:status IS NULL OR v.status = :status) AND " +
                "(:isActive IS NULL OR v.isActive = :isActive) AND " +
                "(:vehicleType IS NULL OR LOWER(v.vehicleType) LIKE LOWER(CONCAT('%', :vehicleType, '%'))) AND " +
                "(:routeId IS NULL OR v.route.id = :routeId)")
        long countVehiclesByFilters(@Param("status") VehicleStatus status,
                                    @Param("isActive") Boolean isActive,
                                    @Param("vehicleType") String vehicleType,
                                    @Param("routeId") Long routeId);

        /**
         * Find vehicles with maintenance alerts (due in next N days)
         */
        @Query("SELECT v FROM Vehicle v WHERE " +
                "v.nextMaintenance IS NOT NULL AND " +
                "v.nextMaintenance BETWEEN :startDate AND :endDate AND " +
                "v.isActive = true")
        List<Vehicle> findVehiclesWithMaintenanceAlerts(@Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find vehicles by route and status
         */
        @Query("SELECT v FROM Vehicle v WHERE " +
                "v.route.id = :routeId AND " +
                "v.status = :status AND " +
                "v.isActive = true")
        List<Vehicle> findVehiclesByRouteAndStatus(@Param("routeId") Long routeId,
                                                   @Param("status") VehicleStatus status);




        // Ajoutez ces méthodes à votre VehicleRepository

        /**
         * Find distinct models by manufacturer
         */
        @Query("SELECT DISTINCT v.model FROM Vehicle v WHERE v.manufacturer = :manufacturer AND v.model IS NOT NULL ORDER BY v.model")
        List<String> findDistinctModelsByManufacturer(@Param("manufacturer") String manufacturer);



        /**
         * Advanced vehicle search with all possible filters (version corrigée)
         */
        @Query("SELECT v FROM Vehicle v WHERE " +
                "(:vehicleNumber IS NULL OR LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :vehicleNumber, '%'))) AND " +
                "(:licensePlate IS NULL OR LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))) AND " +
                "(:status IS NULL OR v.status = :status) AND " +
                "(:vehicleType IS NULL OR LOWER(v.vehicleType) LIKE LOWER(CONCAT('%', :vehicleType, '%'))) AND " +
                "(:fuelType IS NULL OR v.fuelType = :fuelType) AND " +
                "(:minCapacity IS NULL OR v.capacity >= :minCapacity) AND " +
                "(:maxCapacity IS NULL OR v.capacity <= :maxCapacity) AND " +
                "(:isAccessible IS NULL OR v.isAccessible = :isAccessible) AND " +
                "(:hasAirConditioning IS NULL OR v.hasAirConditioning = :hasAirConditioning) AND " +
                "(:hasWifi IS NULL OR v.hasWifi = :hasWifi) AND " +
                "(:hasGps IS NULL OR v.hasGps = :hasGps) AND " +
                "(:isActive IS NULL OR v.isActive = :isActive) AND " +
                "(:routeId IS NULL OR v.route.id = :routeId) AND " +
                "(:manufacturer IS NULL OR LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) AND " +
                "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
                "(:year IS NULL OR v.year = :year)")
        Page<Vehicle> findVehiclesWithAdvancedFilters(@Param("vehicleNumber") String vehicleNumber,
                                                      @Param("licensePlate") String licensePlate,
                                                      @Param("status") VehicleStatus status,
                                                      @Param("vehicleType") String vehicleType,
                                                      @Param("fuelType") FuelType fuelType,
                                                      @Param("minCapacity") Integer minCapacity,
                                                      @Param("maxCapacity") Integer maxCapacity,
                                                      @Param("isAccessible") Boolean isAccessible,
                                                      @Param("hasAirConditioning") Boolean hasAirConditioning,
                                                      @Param("hasWifi") Boolean hasWifi,
                                                      @Param("hasGps") Boolean hasGps,
                                                      @Param("isActive") Boolean isActive,
                                                      @Param("routeId") Long routeId,
                                                      @Param("manufacturer") String manufacturer,
                                                      @Param("model") String model,
                                                      @Param("year") Integer year,
                                                      Pageable pageable);

        /**
         * Version sans pagination pour la recherche avancée
         */
        @Query("SELECT v FROM Vehicle v WHERE " +
                "(:vehicleNumber IS NULL OR LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :vehicleNumber, '%'))) AND " +
                "(:licensePlate IS NULL OR LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))) AND " +
                "(:status IS NULL OR v.status = :status) AND " +
                "(:vehicleType IS NULL OR LOWER(v.vehicleType) LIKE LOWER(CONCAT('%', :vehicleType, '%'))) AND " +
                "(:fuelType IS NULL OR v.fuelType = :fuelType) AND " +
                "(:minCapacity IS NULL OR v.capacity >= :minCapacity) AND " +
                "(:maxCapacity IS NULL OR v.capacity <= :maxCapacity) AND " +
                "(:isAccessible IS NULL OR v.isAccessible = :isAccessible) AND " +
                "(:hasAirConditioning IS NULL OR v.hasAirConditioning = :hasAirConditioning) AND " +
                "(:hasWifi IS NULL OR v.hasWifi = :hasWifi) AND " +
                "(:hasGps IS NULL OR v.hasGps = :hasGps) AND " +
                "(:isActive IS NULL OR v.isActive = :isActive) AND " +
                "(:routeId IS NULL OR v.route.id = :routeId) AND " +
                "(:manufacturer IS NULL OR LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) AND " +
                "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
                "(:year IS NULL OR v.year = :year)")
        List<Vehicle> findVehiclesWithAdvancedFiltersList(@Param("vehicleNumber") String vehicleNumber,
                                                          @Param("licensePlate") String licensePlate,
                                                          @Param("status") VehicleStatus status,
                                                          @Param("vehicleType") String vehicleType,
                                                          @Param("fuelType") FuelType fuelType,
                                                          @Param("minCapacity") Integer minCapacity,
                                                          @Param("maxCapacity") Integer maxCapacity,
                                                          @Param("isAccessible") Boolean isAccessible,
                                                          @Param("hasAirConditioning") Boolean hasAirConditioning,
                                                          @Param("hasWifi") Boolean hasWifi,
                                                          @Param("hasGps") Boolean hasGps,
                                                          @Param("isActive") Boolean isActive,
                                                          @Param("routeId") Long routeId,
                                                          @Param("manufacturer") String manufacturer,
                                                          @Param("model") String model,
                                                          @Param("year") Integer year);

        /**
         * Count vehicles with advanced filters
         */
        @Query("SELECT COUNT(v) FROM Vehicle v WHERE " +
                "(:vehicleNumber IS NULL OR LOWER(v.vehicleNumber) LIKE LOWER(CONCAT('%', :vehicleNumber, '%'))) AND " +
                "(:licensePlate IS NULL OR LOWER(v.licensePlate) LIKE LOWER(CONCAT('%', :licensePlate, '%'))) AND " +
                "(:status IS NULL OR v.status = :status) AND " +
                "(:vehicleType IS NULL OR LOWER(v.vehicleType) LIKE LOWER(CONCAT('%', :vehicleType, '%'))) AND " +
                "(:fuelType IS NULL OR v.fuelType = :fuelType) AND " +
                "(:minCapacity IS NULL OR v.capacity >= :minCapacity) AND " +
                "(:maxCapacity IS NULL OR v.capacity <= :maxCapacity) AND " +
                "(:isAccessible IS NULL OR v.isAccessible = :isAccessible) AND " +
                "(:hasAirConditioning IS NULL OR v.hasAirConditioning = :hasAirConditioning) AND " +
                "(:hasWifi IS NULL OR v.hasWifi = :hasWifi) AND " +
                "(:hasGps IS NULL OR v.hasGps = :hasGps) AND " +
                "(:isActive IS NULL OR v.isActive = :isActive) AND " +
                "(:routeId IS NULL OR v.route.id = :routeId) AND " +
                "(:manufacturer IS NULL OR LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) AND " +
                "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))) AND " +
                "(:year IS NULL OR v.year = :year)")
        long countVehiclesWithAdvancedFilters(@Param("vehicleNumber") String vehicleNumber,
                                              @Param("licensePlate") String licensePlate,
                                              @Param("status") VehicleStatus status,
                                              @Param("vehicleType") String vehicleType,
                                              @Param("fuelType") FuelType fuelType,
                                              @Param("minCapacity") Integer minCapacity,
                                              @Param("maxCapacity") Integer maxCapacity,
                                              @Param("isAccessible") Boolean isAccessible,
                                              @Param("hasAirConditioning") Boolean hasAirConditioning,
                                              @Param("hasWifi") Boolean hasWifi,
                                              @Param("hasGps") Boolean hasGps,
                                              @Param("isActive") Boolean isActive,
                                              @Param("routeId") Long routeId,
                                              @Param("manufacturer") String manufacturer,
                                              @Param("model") String model,
                                              @Param("year") Integer year);

        /**
         * Find vehicles by manufacturer with specific model filtering
         */
        @Query("SELECT v FROM Vehicle v WHERE " +
                "(:manufacturer IS NULL OR LOWER(v.manufacturer) LIKE LOWER(CONCAT('%', :manufacturer, '%'))) AND " +
                "(:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%')))")
        List<Vehicle> findVehiclesByManufacturerAndModel(@Param("manufacturer") String manufacturer,
                                                         @Param("model") String model);






        /**
         * Get comprehensive vehicle statistics
         */
        @Query("SELECT " +
                "COUNT(v) as totalVehicles, " +
                "SUM(CASE WHEN v.isActive = true THEN 1 ELSE 0 END) as activeVehicles, " +
                "SUM(CASE WHEN v.status = 'IN_TRANSIT' THEN 1 ELSE 0 END) as vehiclesInTransit, " +
                "SUM(CASE WHEN v.status = 'MAINTENANCE' THEN 1 ELSE 0 END) as vehiclesInMaintenance, " +
                "SUM(CASE WHEN v.status = 'BREAKDOWN' THEN 1 ELSE 0 END) as vehiclesInBreakdown, " +
                "SUM(CASE WHEN v.status = 'ACTIVE' THEN 1 ELSE 0 END) as activeReadyVehicles, " +
                "SUM(CASE WHEN v.isAccessible = true THEN 1 ELSE 0 END) as accessibleVehicles, " +
                "SUM(CASE WHEN v.hasWifi = true THEN 1 ELSE 0 END) as wifiEnabledVehicles, " +
                "SUM(CASE WHEN v.hasGps = true THEN 1 ELSE 0 END) as gpsEnabledVehicles, " +
                "SUM(v.capacity) as totalCapacity, " +
                "AVG(v.capacity) as averageCapacity " +
                "FROM Vehicle v")
        Object[] getComprehensiveVehicleStatistics();

        /**
         * Get vehicle utilization statistics
         */
        @Query("SELECT " +
                "v.route.id as routeId, " +
                "v.route.routeName as routeName, " +
                "COUNT(v) as vehicleCount, " +
                "SUM(v.capacity) as totalCapacity, " +
                "AVG(v.capacity) as averageCapacity " +
                "FROM Vehicle v " +
                "WHERE v.route IS NOT NULL AND v.isActive = true " +
                "GROUP BY v.route.id, v.route.routeName " +
                "ORDER BY vehicleCount DESC")
        List<Object[]> getVehicleUtilizationByRoute();

        /**
         * Get maximum capacity as Integer
         */
        @Query("SELECT MAX(v.capacity) FROM Vehicle v WHERE v.isActive = true")
        Integer findMaxCapacityValue();

        /**
         * Get minimum capacity as Integer
         */
        @Query("SELECT MIN(v.capacity) FROM Vehicle v WHERE v.isActive = true")
        Integer findMinCapacityValue();





















        // Ajoutez ces méthodes corrigées à votre VehicleRepository

        /**
         * Count vehicles by type - CORRIGÉ
         */
        @Query("SELECT v.vehicleType, COUNT(v) FROM Vehicle v WHERE v.vehicleType IS NOT NULL GROUP BY v.vehicleType ORDER BY COUNT(v) DESC")
        List<Object[]> countVehiclesByType();

        /**
         * Find vehicles over capacity (based on current passenger count) - CORRIGÉ
         */
        @Query("SELECT DISTINCT v FROM Vehicle v JOIN v.locations l WHERE " +
                "l.passengerCount IS NOT NULL AND l.passengerCount > v.capacity AND " +
                "l.timestamp >= :recentTime")
        List<Vehicle> findVehiclesOverCapacity(@Param("recentTime") LocalDateTime recentTime);

        /**
         * Find vehicles near capacity - CORRIGÉ
         */
        @Query("SELECT DISTINCT v FROM Vehicle v JOIN v.locations l WHERE " +
                "l.passengerCount IS NOT NULL AND " +
                "(CAST(l.passengerCount AS double) / v.capacity) >= :threshold AND " +
                "l.timestamp >= :recentTime")
        List<Vehicle> findVehiclesNearCapacity(@Param("threshold") Double threshold,
                                               @Param("recentTime") LocalDateTime recentTime);

        /**
         * Count vehicles by route - CORRIGÉ
         */
        @Query("SELECT v.route.id, v.route.routeName, COUNT(v) FROM Vehicle v " +
                "WHERE v.route IS NOT NULL " +
                "GROUP BY v.route.id, v.route.routeName " +
                "ORDER BY COUNT(v) DESC")
        List<Object[]> countVehiclesByRoute();

        /**
         * Find vehicles with high occupancy - CORRIGÉ (ajout du paramètre manquant)
         */
        @Query("SELECT DISTINCT v FROM Vehicle v JOIN v.locations l WHERE " +
                "l.passengerCount IS NOT NULL AND l.passengerCount >= :minPassengers AND " +
                "l.timestamp >= :recentTime")
        List<Vehicle> findVehiclesWithHighOccupancy(@Param("minPassengers") Integer minPassengers,
                                                    @Param("recentTime") LocalDateTime recentTime);

        /**
         * Find vehicles with high occupancy rate - CORRIGÉ (ajout du paramètre manquant)
         */
        @Query("SELECT DISTINCT v FROM Vehicle v JOIN v.locations l WHERE " +
                "l.passengerCount IS NOT NULL AND " +
                "(CAST(l.passengerCount AS double) / v.capacity) >= :threshold AND " +
                "l.timestamp >= :recentTime")
        List<Vehicle> findVehiclesWithHighOccupancyRate(@Param("threshold") Double threshold,
                                                        @Param("recentTime") LocalDateTime recentTime);

        /**
         * Find moving vehicles - CORRIGÉ (ajout du paramètre manquant)
         */
        @Query("SELECT DISTINCT v FROM Vehicle v JOIN v.locations l WHERE " +
                "l.speed IS NOT NULL AND l.speed > 0 AND l.timestamp >= :recentTime")
        List<Vehicle> findMovingVehicles(@Param("recentTime") LocalDateTime recentTime);

        /**
         * Find vehicles moving above speed - CORRIGÉ (ajout du paramètre manquant)
         */
        @Query("SELECT DISTINCT v FROM Vehicle v JOIN v.locations l WHERE " +
                "l.speed IS NOT NULL AND l.speed > :minSpeed AND l.timestamp >= :recentTime")
        List<Vehicle> findVehiclesMovingAboveSpeed(@Param("minSpeed") Double minSpeed,
                                                   @Param("recentTime") LocalDateTime recentTime);

        /**
         * Find stationary vehicles - CORRIGÉ (ajout du paramètre manquant)
         */
        @Query("SELECT DISTINCT v FROM Vehicle v JOIN v.locations l WHERE " +
                "(l.speed IS NULL OR l.speed = 0) AND l.timestamp >= :recentTime")
        List<Vehicle> findStationaryVehicles(@Param("recentTime") LocalDateTime recentTime);

        /**
         * Get vehicle statistics by route - CORRIGÉ
         */
        @Query("SELECT " +
                "v.route.id as routeId, " +
                "v.route.routeName as routeName, " +
                "COUNT(v) as totalVehicles, " +
                "SUM(CASE WHEN v.isActive = true THEN 1 ELSE 0 END) as activeVehicles, " +
                "SUM(CASE WHEN v.status = 'IN_TRANSIT' THEN 1 ELSE 0 END) as vehiclesInTransit " +
                "FROM Vehicle v WHERE v.route IS NOT NULL " +
                "GROUP BY v.route.id, v.route.routeName " +
                "ORDER BY totalVehicles DESC")
        List<Object[]> getVehicleStatisticsByRoute();

        /**
         * Find vehicles in radius - CORRIGÉ (paramètres dans le bon ordre)
         */
        @Query(value = "SELECT DISTINCT v.* FROM vehicles v " +
                "JOIN vehicle_locations vl ON v.id = vl.vehicle_id " +
                "WHERE vl.timestamp >= :recentTime " +
                "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(vl.latitude)) * " +
                "cos(radians(vl.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                "sin(radians(vl.latitude)))) <= :radiusKm", nativeQuery = true)
        List<Vehicle> findVehiclesInRadius(@Param("latitude") Double latitude,
                                           @Param("longitude") Double longitude,
                                           @Param("radiusKm") Double radiusKm,
                                           @Param("recentTime") LocalDateTime recentTime);

        /**
         * Find vehicles in bounds - CORRIGÉ (paramètres dans le bon ordre)
         */
        @Query("SELECT DISTINCT v FROM Vehicle v JOIN v.locations l WHERE " +
                "l.latitude BETWEEN :minLat AND :maxLat AND " +
                "l.longitude BETWEEN :minLon AND :maxLon AND " +
                "l.timestamp >= :recentTime")
        List<Vehicle> findVehiclesInBounds(@Param("minLat") Double minLatitude,
                                           @Param("maxLat") Double maxLatitude,
                                           @Param("minLon") Double minLongitude,
                                           @Param("maxLon") Double maxLongitude,
                                           @Param("recentTime") LocalDateTime recentTime);

        /**
         * Find vehicles with recent locations - CORRIGÉ (paramètre ajouté)
         */
        @Query("SELECT DISTINCT v FROM Vehicle v JOIN v.locations l WHERE l.timestamp >= :sinceTime")
        List<Vehicle> findVehiclesWithRecentLocations(@Param("sinceTime") LocalDateTime sinceTime);

        /**
         * Count vehicles over capacity - CORRIGÉ (paramètre ajouté)
         */
        @Query("SELECT COUNT(DISTINCT v) FROM Vehicle v JOIN v.locations l WHERE " +
                "l.passengerCount IS NOT NULL AND l.passengerCount > v.capacity AND " +
                "l.timestamp >= :recentTime")
        long countVehiclesOverCapacity(@Param("recentTime") LocalDateTime recentTime);

        /**
         * Count vehicles near capacity - CORRIGÉ (paramètres ajoutés)
         */
        @Query("SELECT COUNT(DISTINCT v) FROM Vehicle v JOIN v.locations l WHERE " +
                "l.passengerCount IS NOT NULL AND " +
                "(CAST(l.passengerCount AS double) / v.capacity) >= :threshold AND " +
                "l.timestamp >= :recentTime")
        long countVehiclesNearCapacity(@Param("threshold") Double threshold,
                                       @Param("recentTime") LocalDateTime recentTime);

        /**
         * Find average occupancy rate - CORRIGÉ (paramètre ajouté)
         */
        @Query("SELECT AVG(CAST(l.passengerCount AS double) / v.capacity) FROM Vehicle v " +
                "JOIN v.locations l WHERE l.passengerCount IS NOT NULL AND " +
                "v.capacity > 0 AND l.timestamp >= :recentTime")
        Double findAverageOccupancyRate(@Param("recentTime") LocalDateTime recentTime);
    }