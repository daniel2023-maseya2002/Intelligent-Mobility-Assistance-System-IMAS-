package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.Vehicle;
import IMAS.ImasProject.model.VehicleStatus;
import IMAS.ImasProject.model.VehicleLocation;
import IMAS.ImasProject.model.FuelType;
import IMAS.ImasProject.repository.VehicleRepository;
import IMAS.ImasProject.repository.RouteRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    @Autowired
    private VehicleRepository vehicleRepository;

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);


    @Autowired
    private RouteRepository routeRepository;

    // =================== CRUD Operations ===================

    /**
     * Get all vehicles
     */
    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findAll();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all vehicles with pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<Vehicle>> getAllVehiclesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Vehicle> vehicles = vehicleRepository.findAll(pageable);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicle by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        try {
            Optional<Vehicle> vehicle = vehicleRepository.findById(id);
            return vehicle.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create new vehicle
     */
    @PostMapping
    public ResponseEntity<?> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        try {
            // Check if vehicle number already exists
            if (vehicleRepository.existsByVehicleNumber(vehicle.getVehicleNumber())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vehicle number already exists"));
            }

            // Check if license plate already exists
            if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "License plate already exists"));
            }

            Vehicle savedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create vehicle: " + e.getMessage()));
        }
    }

    /**
     * Update vehicle
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicleDetails) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();

            // Check if vehicle number is being changed and if it already exists
            if (!vehicle.getVehicleNumber().equals(vehicleDetails.getVehicleNumber()) &&
                    vehicleRepository.existsByVehicleNumber(vehicleDetails.getVehicleNumber())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vehicle number already exists"));
            }

            // Check if license plate is being changed and if it already exists
            if (!vehicle.getLicensePlate().equals(vehicleDetails.getLicensePlate()) &&
                    vehicleRepository.existsByLicensePlate(vehicleDetails.getLicensePlate())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "License plate already exists"));
            }

            // Update fields
            vehicle.setVehicleNumber(vehicleDetails.getVehicleNumber());
            vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
            vehicle.setCapacity(vehicleDetails.getCapacity());
            vehicle.setVehicleType(vehicleDetails.getVehicleType());
            vehicle.setAccessible(vehicleDetails.getAccessible());
            vehicle.setManufacturer(vehicleDetails.getManufacturer());
            vehicle.setModel(vehicleDetails.getModel());
            vehicle.setYear(vehicleDetails.getYear());
            vehicle.setFuelCapacity(vehicleDetails.getFuelCapacity());
            vehicle.setFuelType(vehicleDetails.getFuelType());
            vehicle.setHasAirConditioning(vehicleDetails.getHasAirConditioning());
            vehicle.setHasWifi(vehicleDetails.getHasWifi());
            vehicle.setHasGps(vehicleDetails.getHasGps());
            vehicle.setOdometerReading(vehicleDetails.getOdometerReading());

            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update vehicle: " + e.getMessage()));
        }
    }

    /**
     * Delete vehicle
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        try {
            if (!vehicleRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            vehicleRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Vehicle deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete vehicle: " + e.getMessage()));
        }
    }

    // =================== Search Operations ===================

    /**
     * Find vehicle by vehicle number
     */
    @GetMapping("/by-number/{vehicleNumber}")
    public ResponseEntity<Vehicle> getVehicleByNumber(@PathVariable String vehicleNumber) {
        try {
            Optional<Vehicle> vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber);
            return vehicle.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Find vehicle by license plate
     */
    @GetMapping("/by-plate/{licensePlate}")
    public ResponseEntity<Vehicle> getVehicleByLicensePlate(@PathVariable String licensePlate) {
        try {
            Optional<Vehicle> vehicle = vehicleRepository.findByLicensePlate(licensePlate);
            return vehicle.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search vehicles by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<Vehicle>> searchVehicles(@RequestParam String keyword) {
        try {
            List<Vehicle> vehicles = vehicleRepository.searchVehicles(keyword);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =================== Status Operations ===================

    /**
     * Get vehicles by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Vehicle>> getVehiclesByStatus(@PathVariable VehicleStatus status) {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByStatus(status);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles by status with pagination
     */
    @GetMapping("/status/{status}/paginated")
    public ResponseEntity<Page<Vehicle>> getVehiclesByStatusPaginated(
            @PathVariable VehicleStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Vehicle> vehicles = vehicleRepository.findByStatus(status, pageable);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update vehicle status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateVehicleStatus(@PathVariable Long id, @RequestParam VehicleStatus status) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.setStatus(status);
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update vehicle status: " + e.getMessage()));
        }
    }

    /**
     * Get active vehicles
     */
    @GetMapping("/active")
    public ResponseEntity<List<Vehicle>> getActiveVehicles() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByIsActiveTrue();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get inactive vehicles
     */
    @GetMapping("/inactive")
    public ResponseEntity<List<Vehicle>> getInactiveVehicles() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByIsActiveFalse();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get operational vehicles
     */
    @GetMapping("/operational")
    public ResponseEntity<List<Vehicle>> getOperationalVehicles() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findOperationalVehicles();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get available vehicles
     */
    @GetMapping("/available")
    public ResponseEntity<List<Vehicle>> getAvailableVehicles() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findAvailableVehicles();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =================== Route Operations ===================

    /**
     * Get vehicles by route ID
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByRoute(@PathVariable Long routeId) {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByRouteId(routeId);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles without assigned route
     */
    @GetMapping("/unassigned")
    public ResponseEntity<List<Vehicle>> getUnassignedVehicles() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByRouteIsNull();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Assign vehicle to route
     */
    @PatchMapping("/{vehicleId}/assign-route/{routeId}")
    public ResponseEntity<?> assignVehicleToRoute(@PathVariable Long vehicleId, @PathVariable Long routeId) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            if (!routeRepository.existsById(routeId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Route not found"));
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.assignToRoute(routeRepository.findById(routeId).get());
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to assign vehicle to route: " + e.getMessage()));
        }
    }

    /**
     * Unassign vehicle from route
     */
    @PatchMapping("/{vehicleId}/unassign-route")
    public ResponseEntity<?> unassignVehicleFromRoute(@PathVariable Long vehicleId) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.assignToRoute(null);
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to unassign vehicle from route: " + e.getMessage()));
        }
    }

    // =================== Capacity Operations ===================

    /**
     * Get vehicles by capacity range
     */
    @GetMapping("/capacity")
    public ResponseEntity<List<Vehicle>> getVehiclesByCapacity(
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity) {
        try {
            List<Vehicle> vehicles;
            if (minCapacity != null && maxCapacity != null) {
                vehicles = vehicleRepository.findByCapacityBetween(minCapacity, maxCapacity);
            } else if (minCapacity != null) {
                vehicles = vehicleRepository.findByCapacityGreaterThanEqual(minCapacity);
            } else {
                vehicles = vehicleRepository.findAll();
            }
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get accessible vehicles
     */
    @GetMapping("/accessible")
    public ResponseEntity<List<Vehicle>> getAccessibleVehicles() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByIsAccessibleTrue();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =================== Vehicle Type and Features ===================

    /**
     * Get vehicles by type
     */
    @GetMapping("/type/{vehicleType}")
    public ResponseEntity<List<Vehicle>> getVehiclesByType(@PathVariable String vehicleType) {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByVehicleType(vehicleType);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles by fuel type
     */
    @GetMapping("/fuel-type/{fuelType}")
    public ResponseEntity<List<Vehicle>> getVehiclesByFuelType(@PathVariable FuelType fuelType) {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByFuelType(fuelType);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles with GPS
     */
    @GetMapping("/with-gps")
    public ResponseEntity<List<Vehicle>> getVehiclesWithGps() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByHasGpsTrue();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles with air conditioning
     */
    @GetMapping("/with-ac")
    public ResponseEntity<List<Vehicle>> getVehiclesWithAC() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByHasAirConditioningTrue();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles with WiFi
     */
    @GetMapping("/with-wifi")
    public ResponseEntity<List<Vehicle>> getVehiclesWithWifi() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findByHasWifiTrue();
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =================== Maintenance Operations ===================

    /**
     * Get vehicles requiring maintenance
     */
    @GetMapping("/maintenance/required")
    public ResponseEntity<List<Vehicle>> getVehiclesRequiringMaintenance() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findVehiclesRequiringMaintenance(LocalDateTime.now());
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles overdue for maintenance
     */
    @GetMapping("/maintenance/overdue")
    public ResponseEntity<List<Vehicle>> getVehiclesOverdueForMaintenance() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findVehiclesOverdueForMaintenance(LocalDateTime.now());
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles due for maintenance in next N days
     */
    @GetMapping("/maintenance/due")
    public ResponseEntity<List<Vehicle>> getVehiclesDueForMaintenance(@RequestParam(defaultValue = "30") int days) {
        try {
            LocalDateTime currentTime = LocalDateTime.now();
            LocalDateTime futureTime = currentTime.plusDays(days);
            List<Vehicle> vehicles = vehicleRepository.findVehiclesDueForMaintenanceInDays(currentTime, futureTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Perform maintenance on vehicle
     */
    @PatchMapping("/{id}/maintenance/perform")
    public ResponseEntity<?> performMaintenance(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.performMaintenance();
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to perform maintenance: " + e.getMessage()));
        }
    }

    /**
     * Complete maintenance on vehicle
     */
    @PatchMapping("/{id}/maintenance/complete")
    public ResponseEntity<?> completeMaintenance(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.completeMaintenance();
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to complete maintenance: " + e.getMessage()));
        }
    }

    // =================== Service Operations ===================

    /**
     * Start vehicle service
     */
    @PatchMapping("/{id}/service/start")
    public ResponseEntity<?> startService(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.startService();
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start service: " + e.getMessage()));
        }
    }

    /**
     * End vehicle service
     */
    @PatchMapping("/{id}/service/end")
    public ResponseEntity<?> endService(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.endService();
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to end service: " + e.getMessage()));
        }
    }

    /**
     * Start trip
     */
    @PatchMapping("/{id}/trip/start")
    public ResponseEntity<?> startTrip(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.startTrip();
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start trip: " + e.getMessage()));
        }
    }

    /**
     * End trip
     */
    @PatchMapping("/{id}/trip/end")
    public ResponseEntity<?> endTrip(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.endTrip();
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to end trip: " + e.getMessage()));
        }
    }

    /**
     * Report breakdown
     */
    @PatchMapping("/{id}/breakdown/report")
    public ResponseEntity<?> reportBreakdown(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.reportBreakdown();
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to report breakdown: " + e.getMessage()));
        }
    }

    /**
     * Resolve breakdown
     */
    @PatchMapping("/{id}/breakdown/resolve")
    public ResponseEntity<?> resolveBreakdown(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.resolveBreakdown();
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to resolve breakdown: " + e.getMessage()));
        }
    }

    /**
     * Activate vehicle
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<?> activateVehicle(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.activate();
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to activate vehicle: " + e.getMessage()));
        }
    }

    /**
     * Deactivate vehicle
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateVehicle(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            vehicle.deactivate();
            Vehicle updatedVehicle = vehicleRepository.save(vehicle);
            return ResponseEntity.ok(updatedVehicle);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to deactivate vehicle: " + e.getMessage()));
        }
    }

    // =================== Location and Tracking Operations ===================

    /**
     * Get vehicles with recent location updates
     */
    @GetMapping("/with-recent-locations")
    public ResponseEntity<List<Vehicle>> getVehiclesWithRecentLocations(
            @RequestParam(defaultValue = "60") Integer minutesBack) {
        try {
            LocalDateTime sinceTime = LocalDateTime.now().minusMinutes(minutesBack);
            List<Vehicle> vehicles = vehicleRepository.findVehiclesWithRecentLocations(sinceTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get moving vehicles - CORRIGÉ
     */
    @GetMapping("/moving")
    public ResponseEntity<List<Vehicle>> getMovingVehicles(
            @RequestParam(defaultValue = "30") Integer minutesBack) {
        try {
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(minutesBack);
            List<Vehicle> vehicles = vehicleRepository.findMovingVehicles(recentTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    /**
     * Get vehicles moving above speed - CORRIGÉ
     */
    @GetMapping("/moving-above-speed")
    public ResponseEntity<List<Vehicle>> getVehiclesMovingAboveSpeed(
            @RequestParam(defaultValue = "50.0") Double minSpeed,
            @RequestParam(defaultValue = "30") Integer minutesBack) {
        try {
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(minutesBack);
            List<Vehicle> vehicles = vehicleRepository.findVehiclesMovingAboveSpeed(minSpeed, recentTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




    /**
     * Get stationary vehicles
     */
    @GetMapping("/stationary")
    public ResponseEntity<List<Vehicle>> getStationaryVehicles(
            @RequestParam(defaultValue = "30") Integer minutesBack) {
        try {
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(minutesBack);
            List<Vehicle> vehicles = vehicleRepository.findStationaryVehicles(recentTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




    @GetMapping("/in-bounds")
    public ResponseEntity<List<Vehicle>> getVehiclesInBounds(
            @RequestParam Double minLatitude,
            @RequestParam Double maxLatitude,
            @RequestParam Double minLongitude,
            @RequestParam Double maxLongitude,
            @RequestParam(defaultValue = "60") Integer minutesBack) {
        try {
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(minutesBack);
            List<Vehicle> vehicles = vehicleRepository.findVehiclesInBounds(
                    minLatitude, maxLatitude, minLongitude, maxLongitude, recentTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles in radius
     */
    @GetMapping("/in-radius")
    public ResponseEntity<List<Vehicle>> getVehiclesInRadius(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            @RequestParam(defaultValue = "60") Integer minutesBack) {
        try {
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(minutesBack);
            List<Vehicle> vehicles = vehicleRepository.findVehiclesInRadius(
                    latitude, longitude, radiusKm, recentTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicle current location
     */
    @GetMapping("/{id}/location")
    public ResponseEntity<?> getVehicleCurrentLocation(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            VehicleLocation currentLocation = vehicle.getCurrentLocation();

            if (currentLocation == null) {
                return ResponseEntity.ok(Map.of("message", "No location data available"));
            }

            return ResponseEntity.ok(currentLocation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get vehicle location: " + e.getMessage()));
        }
    }

    /**
     * Get vehicle location history
     */
    @GetMapping("/{id}/location/history")
    public ResponseEntity<?> getVehicleLocationHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            List<VehicleLocation> locations = vehicle.getRecentLocations(limit);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get location history: " + e.getMessage()));
        }
    }

    // =================== Statistics and Analytics ===================

    /**
     * Get vehicle statistics
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<?> getVehicleStatistics(@PathVariable Long id) {
        try {
            Optional<Vehicle> optionalVehicle = vehicleRepository.findById(id);
            if (!optionalVehicle.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Vehicle vehicle = optionalVehicle.get();
            Map<String, Object> stats = new HashMap<>();

            stats.put("vehicleId", vehicle.getId());
            stats.put("vehicleNumber", vehicle.getVehicleNumber());
            stats.put("currentSpeed", vehicle.getCurrentSpeed());
            stats.put("currentPassengerCount", vehicle.getCurrentPassengerCount());
            stats.put("occupancyRate", vehicle.getOccupancyRate());
            stats.put("isOverCapacity", vehicle.isOverCapacity());
            stats.put("isMoving", vehicle.isMoving());
            stats.put("isOperational", vehicle.isOperational());
            stats.put("isMaintenanceRequired", vehicle.isMaintenanceRequired());
            stats.put("daysSinceLastMaintenance", vehicle.getDaysSinceLastMaintenance());
            stats.put("features", vehicle.getFeatures());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get vehicle statistics: " + e.getMessage()));
        }
    }


    /**
     * Get vehicles with high occupancy - CORRIGÉ
     */
    @GetMapping("/high-occupancy")
    public ResponseEntity<List<Vehicle>> getVehiclesWithHighOccupancy(
            @RequestParam(defaultValue = "20") Integer minPassengers,
            @RequestParam(defaultValue = "60") Integer minutesBack) {
        try {
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(minutesBack);
            List<Vehicle> vehicles = vehicleRepository.findVehiclesWithHighOccupancy(minPassengers, recentTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles with high occupancy rate - CORRIGÉ
     */
    @GetMapping("/high-occupancy-rate")
    public ResponseEntity<List<Vehicle>> getVehiclesWithHighOccupancyRate(
            @RequestParam(defaultValue = "0.8") Double threshold,
            @RequestParam(defaultValue = "60") Integer minutesBack) {
        try {
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(minutesBack);
            List<Vehicle> vehicles = vehicleRepository.findVehiclesWithHighOccupancyRate(threshold, recentTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get fleet statistics
     */
    @GetMapping("/statistics/fleet")
    public ResponseEntity<?> getFleetStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // Total vehicles
            long totalVehicles = vehicleRepository.count();
            stats.put("totalVehicles", totalVehicles);

            // Active vs Inactive
            long activeVehicles = vehicleRepository.countByIsActiveTrue();
            long inactiveVehicles = vehicleRepository.countByIsActiveFalse();
            stats.put("activeVehicles", activeVehicles);
            stats.put("inactiveVehicles", inactiveVehicles);

            // Status breakdown
            Map<String, Long> statusBreakdown = new HashMap<>();
            for (VehicleStatus status : VehicleStatus.values()) {
                long count = vehicleRepository.countByStatus(status);
                statusBreakdown.put(status.name(), count);
            }
            stats.put("statusBreakdown", statusBreakdown);

            // Operational vehicles
            long operationalVehicles = vehicleRepository.countOperationalVehicles();
            stats.put("operationalVehicles", operationalVehicles);

            // Maintenance statistics
            long vehiclesRequiringMaintenance = vehicleRepository.countVehiclesRequiringMaintenance(LocalDateTime.now());
            long vehiclesOverdueForMaintenance = vehicleRepository.countVehiclesOverdueForMaintenance(LocalDateTime.now());
            stats.put("vehiclesRequiringMaintenance", vehiclesRequiringMaintenance);
            stats.put("vehiclesOverdueForMaintenance", vehiclesOverdueForMaintenance);

            // Route assignment
            long assignedVehicles = vehicleRepository.countByRouteIsNotNull();
            long unassignedVehicles = vehicleRepository.countByRouteIsNull();
            stats.put("assignedVehicles", assignedVehicles);
            stats.put("unassignedVehicles", unassignedVehicles);

            // Capacity statistics
            Double averageCapacity = vehicleRepository.findAverageCapacity();
            Integer maxCapacity = vehicleRepository.findMaxCapacityValue();
            Integer minCapacity = vehicleRepository.findMinCapacityValue();
            stats.put("averageCapacity", averageCapacity);
            stats.put("maxCapacity", maxCapacity);
            stats.put("minCapacity", minCapacity);

            // Feature statistics
            long accessibleVehicles = vehicleRepository.countByIsAccessibleTrue();
            long vehiclesWithAC = vehicleRepository.countByHasAirConditioningTrue();
            long vehiclesWithWifi = vehicleRepository.countByHasWifiTrue();
            long vehiclesWithGps = vehicleRepository.countByHasGpsTrue();
            stats.put("accessibleVehicles", accessibleVehicles);
            stats.put("vehiclesWithAC", vehiclesWithAC);
            stats.put("vehiclesWithWifi", vehiclesWithWifi);
            stats.put("vehiclesWithGPS", vehiclesWithGps);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get fleet statistics: " + e.getMessage()));
        }
    }


    // Méthodes corrigées pour le VehicleController

    /**
     * Get vehicle type statistics - CORRIGÉ
     */
    @GetMapping("/statistics/by-type")
    public ResponseEntity<?> getVehicleTypeStatistics() {
        try {
            List<Object[]> typeStats = vehicleRepository.countVehiclesByType();
            Map<String, Long> stats = new HashMap<>();

            for (Object[] row : typeStats) {
                String type = (String) row[0];
                Long count = (Long) row[1];
                stats.put(type, count);
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get vehicle type statistics: " + e.getMessage()));
        }
    }
    /**
     * Get fuel type statistics
     */
    @GetMapping("/statistics/by-fuel-type")
    public ResponseEntity<?> getFuelTypeStatistics() {
        try {
            Map<String, Long> stats = new HashMap<>();

            for (FuelType fuelType : FuelType.values()) {
                long count = vehicleRepository.countByFuelType(fuelType);
                stats.put(fuelType.name(), count);
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get fuel type statistics: " + e.getMessage()));
        }
    }

    // =================== Occupancy and Capacity Operations ===================

    /**
     * Get vehicles near capacity - CORRIGÉ
     */
    @GetMapping("/near-capacity")
    public ResponseEntity<List<Vehicle>> getVehiclesNearCapacity(
            @RequestParam(defaultValue = "0.8") Double threshold,
            @RequestParam(defaultValue = "60") Integer minutesBack) {
        try {
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(minutesBack);
            List<Vehicle> vehicles = vehicleRepository.findVehiclesNearCapacity(threshold, recentTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get vehicles over capacity - CORRIGÉ
     */
    @GetMapping("/over-capacity")
    public ResponseEntity<List<Vehicle>> getVehiclesOverCapacity(
            @RequestParam(defaultValue = "60") Integer minutesBack) {
        try {
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(minutesBack);
            List<Vehicle> vehicles = vehicleRepository.findVehiclesOverCapacity(recentTime);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /*
    *//**
     * Get occupancy statistics
     *//*
    @GetMapping("/occupancy/statistics")
    public ResponseEntity<?> getOccupancyStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            Double averageOccupancy = vehicleRepository.findAverageOccupancyRate();
            Long vehiclesNearCapacity = vehicleRepository.countVehiclesNearCapacity(0.8);
            Long vehiclesOverCapacity = vehicleRepository.countVehiclesOverCapacity();

            stats.put("averageOccupancyRate", averageOccupancy);
            stats.put("vehiclesNearCapacity", vehiclesNearCapacity);
            stats.put("vehiclesOverCapacity", vehiclesOverCapacity);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get occupancy statistics: " + e.getMessage()));
        }
    }*/

    // =================== Bulk Operations ===================

    /**
     * Bulk update vehicle status
     */
    @PatchMapping("/bulk/status")
    public ResponseEntity<?> bulkUpdateStatus(
            @RequestParam List<Long> vehicleIds,
            @RequestParam VehicleStatus status) {
        try {
            List<Vehicle> updatedVehicles = new ArrayList<>();

            for (Long vehicleId : vehicleIds) {
                Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
                if (optionalVehicle.isPresent()) {
                    Vehicle vehicle = optionalVehicle.get();
                    vehicle.setStatus(status);
                    updatedVehicles.add(vehicleRepository.save(vehicle));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Updated " + updatedVehicles.size() + " vehicles",
                    "updatedVehicles", updatedVehicles
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to bulk update status: " + e.getMessage()));
        }
    }

    /**
     * Bulk assign vehicles to route
     */
    @PatchMapping("/bulk/assign-route")
    public ResponseEntity<?> bulkAssignToRoute(
            @RequestParam List<Long> vehicleIds,
            @RequestParam Long routeId) {
        try {
            if (!routeRepository.existsById(routeId)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Route not found"));
            }

            List<Vehicle> updatedVehicles = new ArrayList<>();

            for (Long vehicleId : vehicleIds) {
                Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
                if (optionalVehicle.isPresent()) {
                    Vehicle vehicle = optionalVehicle.get();
                    vehicle.assignToRoute(routeRepository.findById(routeId).get());
                    updatedVehicles.add(vehicleRepository.save(vehicle));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Assigned " + updatedVehicles.size() + " vehicles to route",
                    "updatedVehicles", updatedVehicles
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to bulk assign to route: " + e.getMessage()));
        }
    }

    /**
     * Bulk activate vehicles
     */
    @PatchMapping("/bulk/activate")
    public ResponseEntity<?> bulkActivateVehicles(@RequestParam List<Long> vehicleIds) {
        try {
            List<Vehicle> updatedVehicles = new ArrayList<>();

            for (Long vehicleId : vehicleIds) {
                Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
                if (optionalVehicle.isPresent()) {
                    Vehicle vehicle = optionalVehicle.get();
                    vehicle.activate();
                    updatedVehicles.add(vehicleRepository.save(vehicle));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Activated " + updatedVehicles.size() + " vehicles",
                    "updatedVehicles", updatedVehicles
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to bulk activate vehicles: " + e.getMessage()));
        }
    }

    /**
     * Bulk deactivate vehicles
     */
    @PatchMapping("/bulk/deactivate")
    public ResponseEntity<?> bulkDeactivateVehicles(@RequestParam List<Long> vehicleIds) {
        try {
            List<Vehicle> updatedVehicles = new ArrayList<>();

            for (Long vehicleId : vehicleIds) {
                Optional<Vehicle> optionalVehicle = vehicleRepository.findById(vehicleId);
                if (optionalVehicle.isPresent()) {
                    Vehicle vehicle = optionalVehicle.get();
                    vehicle.deactivate();
                    updatedVehicles.add(vehicleRepository.save(vehicle));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Deactivated " + updatedVehicles.size() + " vehicles",
                    "updatedVehicles", updatedVehicles
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to bulk deactivate vehicles: " + e.getMessage()));
        }
    }

    /**
     * Bulk delete vehicles
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<?> bulkDeleteVehicles(@RequestParam List<Long> vehicleIds) {
        try {
            List<Long> deletedIds = new ArrayList<>();
            List<Long> notFoundIds = new ArrayList<>();

            for (Long vehicleId : vehicleIds) {
                if (vehicleRepository.existsById(vehicleId)) {
                    vehicleRepository.deleteById(vehicleId);
                    deletedIds.add(vehicleId);
                } else {
                    notFoundIds.add(vehicleId);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bulk delete completed");
            response.put("deletedCount", deletedIds.size());
            response.put("deletedIds", deletedIds);
            if (!notFoundIds.isEmpty()) {
                response.put("notFoundIds", notFoundIds);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to bulk delete vehicles: " + e.getMessage()));
        }
    }

// =================== Advanced Search and Filtering ===================
    /**
     * Advanced vehicle search with multiple filters (version corrigée)
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<?> advancedSearch(
            @RequestParam(required = false) String vehicleNumber,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) String vehicleType,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity,
            @RequestParam(required = false) Boolean isAccessible,
            @RequestParam(required = false) Boolean hasAirConditioning,
            @RequestParam(required = false) Boolean hasWifi,
            @RequestParam(required = false) Boolean hasGps,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // Utilisation de la nouvelle méthode avec la signature correcte
            Page<Vehicle> vehicles = vehicleRepository.findVehiclesWithAdvancedFilters(
                    vehicleNumber, licensePlate, status, vehicleType, fuelType,
                    minCapacity, maxCapacity, isAccessible, hasAirConditioning,
                    hasWifi, hasGps, isActive, routeId, manufacturer, model, year, pageable);

            // Création d'une réponse enrichie avec des métadonnées
            Map<String, Object> response = new HashMap<>();
            response.put("vehicles", vehicles.getContent());

            // Utilisation de Map.of pour la pagination (moins de 10 paires clé-valeur)
            response.put("pagination", Map.of(
                    "currentPage", vehicles.getNumber(),
                    "totalPages", vehicles.getTotalPages(),
                    "totalElements", vehicles.getTotalElements(),
                    "size", vehicles.getSize(),
                    "hasNext", vehicles.hasNext(),
                    "hasPrevious", vehicles.hasPrevious()
            ));

            // Création manuelle du map des filtres avec HashMap pour éviter la limitation de Map.of()
            Map<String, Object> filters = new HashMap<>();
            filters.put("vehicleNumber", vehicleNumber);
            filters.put("licensePlate", licensePlate);
            filters.put("status", status);
            filters.put("vehicleType", vehicleType);
            filters.put("fuelType", fuelType);
            filters.put("minCapacity", minCapacity);
            filters.put("maxCapacity", maxCapacity);
            filters.put("isAccessible", isAccessible);
            filters.put("hasAirConditioning", hasAirConditioning);
            filters.put("hasWifi", hasWifi);
            filters.put("hasGps", hasGps);
            filters.put("isActive", isActive);
            filters.put("routeId", routeId);
            filters.put("manufacturer", manufacturer);
            filters.put("model", model);
            filters.put("year", year);

            response.put("filters", filters);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error performing advanced search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to perform advanced search: " + e.getMessage()));
        }
    }

    /**
     * Get vehicle search options for dropdowns
     */
    @GetMapping("/search/options")
    public ResponseEntity<?> getSearchOptions() {
        try {
            Map<String, Object> options = new HashMap<>();

            options.put("vehicleTypes", vehicleRepository.findDistinctVehicleTypes());
            options.put("manufacturers", vehicleRepository.findDistinctManufacturers());
            options.put("models", vehicleRepository.findDistinctModels());
            options.put("fuelTypes", vehicleRepository.findDistinctFuelTypes());
            options.put("years", vehicleRepository.findDistinctYears());
            options.put("statuses", Arrays.asList(VehicleStatus.values()));

            // Statistiques de capacité
            options.put("capacityRange", Map.of(
                    "min", vehicleRepository.findMinCapacityValue(),
                    "max", vehicleRepository.findMaxCapacityValue(),
                    "average", vehicleRepository.findAverageCapacity()
            ));

            return ResponseEntity.ok(options);
        } catch (Exception e) {
            logger.error("Error fetching search options", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch search options: " + e.getMessage()));
        }
    }


    /**
     * Get comprehensive vehicle statistics - CORRIGÉ
     */
    @GetMapping("/statistics/comprehensive")
    public ResponseEntity<?> getComprehensiveStatistics() {
        try {
            Object[] rawStats = vehicleRepository.getComprehensiveVehicleStatistics();

            Map<String, Object> stats = new HashMap<>();
            if (rawStats != null && rawStats.length > 0) {
                stats.put("totalVehicles", rawStats[0]);
                stats.put("activeVehicles", rawStats[1]);
                stats.put("vehiclesInTransit", rawStats[2]);
                stats.put("vehiclesInMaintenance", rawStats[3]);
                stats.put("vehiclesInBreakdown", rawStats[4]);
                stats.put("activeReadyVehicles", rawStats[5]);
                stats.put("accessibleVehicles", rawStats[6]);
                stats.put("wifiEnabledVehicles", rawStats[7]);
                stats.put("gpsEnabledVehicles", rawStats[8]);
                stats.put("totalCapacity", rawStats[9]);
                stats.put("averageCapacity", rawStats[10]);
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get comprehensive statistics: " + e.getMessage()));
        }
    }



    /**
     * Get vehicle utilization by route - CORRIGÉ
     */
    @GetMapping("/statistics/utilization-by-route")
    public ResponseEntity<?> getVehicleUtilizationByRoute() {
        try {
            List<Object[]> utilizationData = vehicleRepository.getVehicleUtilizationByRoute();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Object[] row : utilizationData) {
                Map<String, Object> routeData = new HashMap<>();
                routeData.put("routeId", row[0]);
                routeData.put("routeName", row[1]);
                routeData.put("vehicleCount", row[2]);
                routeData.put("totalCapacity", row[3]);
                routeData.put("averageCapacity", row[4]);
                result.add(routeData);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get vehicle utilization by route: " + e.getMessage()));
        }
    }


    /**
     * Get occupancy statistics - CORRIGÉ
     */
    @GetMapping("/statistics/occupancy")
    public ResponseEntity<?> getOccupancyStatistics(
            @RequestParam(defaultValue = "60") Integer minutesBack,
            @RequestParam(defaultValue = "0.8") Double capacityThreshold) {
        try {
            LocalDateTime recentTime = LocalDateTime.now().minusMinutes(minutesBack);

            Map<String, Object> stats = new HashMap<>();

            // Count vehicles over capacity
            long overCapacity = vehicleRepository.countVehiclesOverCapacity(recentTime);
            stats.put("vehiclesOverCapacity", overCapacity);

            // Count vehicles near capacity
            long nearCapacity = vehicleRepository.countVehiclesNearCapacity(capacityThreshold, recentTime);
            stats.put("vehiclesNearCapacity", nearCapacity);

            // Average occupancy rate
            Double avgOccupancy = vehicleRepository.findAverageOccupancyRate(recentTime);
            stats.put("averageOccupancyRate", avgOccupancy != null ? avgOccupancy : 0.0);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get occupancy statistics: " + e.getMessage()));
        }
    }

    // =================== Export Operations ===================

    /**
     * Export vehicles to CSV
     */
    @GetMapping("/export/csv")
    public ResponseEntity<?> exportVehiclesToCsv() {
        try {
            List<Vehicle> vehicles = vehicleRepository.findAll();

            StringBuilder csvContent = new StringBuilder();
            csvContent.append("ID,Vehicle Number,License Plate,Capacity,Vehicle Type,Status,Manufacturer,Model,Year,Fuel Type,Is Active,Route ID\n");

            for (Vehicle vehicle : vehicles) {
                csvContent.append(vehicle.getId()).append(",")
                        .append(vehicle.getVehicleNumber()).append(",")
                        .append(vehicle.getLicensePlate()).append(",")
                        .append(vehicle.getCapacity()).append(",")
                        .append(vehicle.getVehicleType()).append(",")
                        .append(vehicle.getStatus()).append(",")
                        .append(vehicle.getManufacturer() != null ? vehicle.getManufacturer() : "").append(",")
                        .append(vehicle.getModel() != null ? vehicle.getModel() : "").append(",")
                        .append(vehicle.getYear() != null ? vehicle.getYear() : "").append(",")
                        .append(vehicle.getFuelType() != null ? vehicle.getFuelType() : "").append(",")
                        .append(vehicle.getActive()).append(",")
                        .append(vehicle.getRoute() != null ? vehicle.getRoute().getId() : "")
                        .append("\n");
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv")
                    .header("Content-Disposition", "attachment; filename=vehicles.csv")
                    .body(csvContent.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to export vehicles: " + e.getMessage()));
        }
    }

    // =================== Health Check and System Operations ===================

    /**
     * System health check
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", LocalDateTime.now());
            health.put("totalVehicles", vehicleRepository.count());
            health.put("activeVehicles", vehicleRepository.countByIsActiveTrue());
            health.put("operationalVehicles", vehicleRepository.countOperationalVehicles());

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("timestamp", LocalDateTime.now());
            health.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
        }
    }

    // =================== Utility Methods ===================

    /**
     * Validate vehicle data
     */
    private boolean isValidVehicle(Vehicle vehicle) {
        return vehicle.getVehicleNumber() != null && !vehicle.getVehicleNumber().trim().isEmpty() &&
                vehicle.getLicensePlate() != null && !vehicle.getLicensePlate().trim().isEmpty() &&
                vehicle.getCapacity() != null && vehicle.getCapacity() > 0 &&
                vehicle.getVehicleType() != null && !vehicle.getVehicleType().trim().isEmpty();
    }

    /**
     * Get all vehicle types
     */
    @GetMapping("/types")
    public ResponseEntity<List<String>> getAllVehicleTypes() {
        try {
            List<String> types = vehicleRepository.findDistinctVehicleTypes();
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all manufacturers
     */
    @GetMapping("/manufacturers")
    public ResponseEntity<List<String>> getAllManufacturers() {
        try {
            List<String> manufacturers = vehicleRepository.findDistinctManufacturers();
            return ResponseEntity.ok(manufacturers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get models for a manufacturer
     */
    @GetMapping("/manufacturers/{manufacturer}/models")
    public ResponseEntity<List<String>> getModelsByManufacturer(@PathVariable String manufacturer) {
        try {
            List<String> models = vehicleRepository.findDistinctModelsByManufacturer(manufacturer);
            return ResponseEntity.ok(models);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/count/by-route")
    public ResponseEntity<?> getVehicleCountByRoute() {
        try {
            List<Object[]> countByRoute = vehicleRepository.countVehiclesByRoute();
            List<Map<String, Object>> result = new ArrayList<>();

            for (Object[] row : countByRoute) {
                Map<String, Object> routeData = new HashMap<>();
                routeData.put("routeId", row[0]);
                routeData.put("routeName", row[1]);
                routeData.put("vehicleCount", row[2]);
                result.add(routeData);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get vehicle count by route: " + e.getMessage()));
        }
    }
}