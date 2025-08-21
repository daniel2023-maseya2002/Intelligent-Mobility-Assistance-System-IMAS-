package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.*;
import IMAS.ImasProject.dto.*;
import IMAS.ImasProject.repository.BusRepository;
import IMAS.ImasProject.services.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/buses")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Validated
public class BusController {

    @Autowired
    private BusService busService;

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private StaffService staffService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RouteService routeService;

    /**
     * Create a new bus
     */
    @PostMapping
    public ResponseEntity<?> createBus(@RequestBody @Validated BusCreateRequest request) {
        try {
            // Validate required fields
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Bus name is required"));
            }

            if (request.getRouteId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Route ID is required"));
            }

            // Create the bus entity
            Bus bus = new Bus();
            bus.setName(request.getName().trim());
            bus.setBusLine(request.getBusLine());
            bus.setCapacity(request.getCapacity());
            bus.setStartLat(request.getStartLat());
            bus.setStartLng(request.getStartLng());
            bus.setEndLat(request.getEndLat());
            bus.setEndLng(request.getEndLng());
            bus.setCurrentLat(request.getStartLat());
            bus.setCurrentLng(request.getStartLng());
            bus.setPassengers(0);
            bus.setProgress(0.0);
            bus.setStopped(true); // New buses start as stopped
            bus.setHasAccident(false);
            bus.setDepartureTime(request.getDepartureTime());

            // Set route
            Route route = routeService.findById(request.getRouteId());
            if (route == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Route not found"));
            }
            bus.setRoute(route);

            // Calculate estimated arrival time if departure time is provided
            if (request.getDepartureTime() != null && request.getEstimatedDuration() != null) {
                bus.setArrivalTime(request.getDepartureTime().plusMinutes(request.getEstimatedDuration()));
            }

            // Assign driver if provided
            if (request.getDriverId() != null) {
                Optional<Staff> driverOptional = staffService.findById(request.getDriverId());
                if (driverOptional.isPresent()) {
                    Staff driver = driverOptional.get();
                    if (driver.getRole() == StaffRole.DRIVER) {
                        // Check if driver is already assigned to another active bus
                        Bus existingBus = busService.findByDriverId(request.getDriverId());
                        if (existingBus != null && !existingBus.getStopped()) {
                            return ResponseEntity.badRequest()
                                    .body(createErrorResponse("Driver is already assigned to an active bus"));
                        }

                        bus.setDriver(driver);
                        // Send email notification to driver
                        sendDriverNotification(driver, bus, request);
                    } else {
                        return ResponseEntity.badRequest()
                                .body(createErrorResponse("Selected staff member is not a driver"));
                    }
                } else {
                    return ResponseEntity.badRequest().body(createErrorResponse("Driver not found"));
                }
            }

            Bus savedBus = busService.save(bus);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBus);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error creating bus: " + e.getMessage()));
        }
    }

    /**
     * Get all buses with driver and route information
     */
    @GetMapping
    public ResponseEntity<List<Bus>> getAllBuses() {
        try {
            List<Bus> buses = busService.findAllWithDriverAndRoute();
            return ResponseEntity.ok(buses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get bus by ID with full details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getBusById(@PathVariable Long id) {
        try {
            Optional<Bus> busOptional = busService.findByIdOptional(id);
            if (busOptional.isPresent()) {
                Bus foundBus = busOptional.get();
                // Ensure route and stops are loaded
                if (foundBus.getRoute() != null) {
                    Hibernate.initialize(foundBus.getRoute().getStops());
                }
                return ResponseEntity.ok(foundBus);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving bus: " + e.getMessage()));
        }
    }

    /**
     * Update bus information
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBus(@PathVariable Long id, @RequestBody @Validated BusCreateRequest request) {
        try {
            Bus bus = busService.findById(id);
            if (bus == null) {
                return ResponseEntity.notFound().build();
            }

            // Update basic information
            bus.setName(request.getName() != null ? request.getName().trim() : bus.getName());
            bus.setBusLine(request.getBusLine() != null ? request.getBusLine() : bus.getBusLine());
            bus.setCapacity(request.getCapacity() != null ? request.getCapacity() : bus.getCapacity());

            // Update departure time and recalculate arrival time
            if (request.getDepartureTime() != null) {
                bus.setDepartureTime(request.getDepartureTime());
                if (request.getEstimatedDuration() != null) {
                    bus.setArrivalTime(request.getDepartureTime().plusMinutes(request.getEstimatedDuration()));
                }
            }

            // Update route if provided
            if (request.getRouteId() != null) {
                Route route = routeService.findById(request.getRouteId());
                if (route == null) {
                    return ResponseEntity.badRequest().body(createErrorResponse("Route not found"));
                }
                bus.setRoute(route);
            }

            // Update driver assignment
            if (request.getDriverId() != null) {
                // Check if driver has changed
                boolean driverChanged = bus.getDriver() == null ||
                        !request.getDriverId().equals(bus.getDriver().getId());

                if (driverChanged) {
                    Optional<Staff> driverOptional = staffService.findById(request.getDriverId());
                    if (driverOptional.isPresent()) {
                        Staff driver = driverOptional.get();
                        if (driver.getRole() == StaffRole.DRIVER) {
                            // Check if driver is already assigned to another active bus
                            Bus existingBus = busService.findByDriverId(request.getDriverId());
                            if (existingBus != null && !existingBus.getId().equals(id) && !existingBus.getStopped()) {
                                return ResponseEntity.badRequest()
                                        .body(createErrorResponse("Driver is already assigned to another active bus"));
                            }

                            bus.setDriver(driver);
                            sendDriverNotification(driver, bus, request);
                        } else {
                            return ResponseEntity.badRequest()
                                    .body(createErrorResponse("Selected staff member is not a driver"));
                        }
                    } else {
                        return ResponseEntity.badRequest().body(createErrorResponse("Driver not found"));
                    }
                }
            } else if (request.getDriverId() == null && bus.getDriver() != null) {
                // Remove driver assignment
                bus.setDriver(null);
            }

            Bus updatedBus = busService.save(bus);
            return ResponseEntity.ok(updatedBus);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating bus: " + e.getMessage()));
        }
    }

    /**
     * Delete a bus
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBus(@PathVariable Long id) {
        try {
            if (!busService.canDeleteBus(id)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createErrorResponse("Cannot delete bus with active passengers or running status"));
            }

            busService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Bus deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error deleting bus: " + e.getMessage()));
        }
    }

    /**
     * Start a bus journey
     */
    @PutMapping("/{id}/start")
    public ResponseEntity<?> startBus(@PathVariable Long id) {
        try {
            // LOG: Ajout de logging pour déboguer
            System.out.println("Starting bus with ID: " + id);

            Bus bus = busService.findById(id);
            if (bus == null) {
                System.err.println("Bus not found with ID: " + id);
                return ResponseEntity.badRequest().body(createErrorResponse("Bus not found with ID: " + id));
            }

            System.out.println("Bus found: " + bus.getName() + ", stopped: " + bus.getStopped() + ", hasAccident: " + bus.getHasAccident());

            Bus updatedBus = busService.startBus(id);
            return ResponseEntity.ok(updatedBus);
        } catch (RuntimeException e) {
            System.err.println("Runtime exception in startBus: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            System.err.println("General exception in startBus: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error starting bus: " + e.getMessage()));
        }
    }

    /**
     * Complete a bus route
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeRoute(@PathVariable Long id) {
        try {
            Bus updatedBus = busService.completeRoute(id);
            return ResponseEntity.ok(Map.of(
                    "bus", updatedBus,
                    "message", "Route completed successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error completing route: " + e.getMessage()));
        }
    }

    /**
     * Stop a bus
     */
    @PutMapping("/{id}/stop")
    public ResponseEntity<?> stopBus(@PathVariable Long id) {
        try {
            Bus updatedBus = busService.stopBus(id);
            return ResponseEntity.ok(updatedBus);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error stopping bus: " + e.getMessage()));
        }
    }

    /**
     * Restart a stopped bus
     */
    @PostMapping("/{id}/restart")
    public ResponseEntity<?> restartBus(@PathVariable Long id) {
        try {
            Bus bus = busService.findById(id);
            if (bus == null) {
                return ResponseEntity.notFound().build();
            }

            if (bus.getHasAccident()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Cannot restart a bus with an accident. Please resolve the accident first."));
            }

            bus.setStopped(false);
            bus.setDepartureTime(LocalDateTime.now());

            // Recalculate arrival time
            if (bus.getProgress() != null && bus.getProgress() < 100) {
                // Estimate remaining time based on progress
                Duration estimatedTotal = Duration.ofMinutes(60); // Default estimation
                double remainingProgress = 100.0 - bus.getProgress();
                long remainingMinutes = Math.round((remainingProgress / 100.0) * estimatedTotal.toMinutes());
                bus.setArrivalTime(LocalDateTime.now().plusMinutes(remainingMinutes));
            }

            Bus updatedBus = busService.save(bus);
            return ResponseEntity.ok(updatedBus);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error restarting bus: " + e.getMessage()));
        }
    }

    /**
     * Report an accident
     */
    @PutMapping("/{id}/accident")
    public ResponseEntity<?> reportAccident(@PathVariable Long id) {
        try {
            Bus updatedBus = busService.reportAccident(id);
            return ResponseEntity.ok(Map.of(
                    "bus", updatedBus,
                    "message", "Accident reported successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error reporting accident: " + e.getMessage()));
        }
    }

    /**
     * Clear accident status
     */
    @PutMapping("/{id}/clear-accident")
    public ResponseEntity<?> clearAccident(@PathVariable Long id) {
        try {
            Bus bus = busService.findById(id);
            if (bus == null) {
                return ResponseEntity.notFound().build();
            }

            bus.setHasAccident(false);
            Bus updatedBus = busService.save(bus);

            return ResponseEntity.ok(Map.of(
                    "bus", updatedBus,
                    "message", "Accident status cleared successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error clearing accident: " + e.getMessage()));
        }
    }

    /**
     * Update bus position and progress
     */
    @PutMapping("/{id}/position")
    public ResponseEntity<?> updatePosition(@PathVariable Long id, @RequestBody PositionUpdate update) {
        try {
            if (update.getCurrentLat() == null || update.getCurrentLng() == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Latitude and longitude are required"));
            }

            Bus updatedBus = busService.updatePosition(id,
                    update.getCurrentLat(),
                    update.getCurrentLng(),
                    update.getProgress());

            return ResponseEntity.ok(updatedBus);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating position: " + e.getMessage()));
        }
    }

    /**
     * Update only progress
     */
    @PutMapping("/{id}/progress")
    public ResponseEntity<?> updateProgress(@PathVariable Long id, @RequestBody Map<String, Double> progressData) {
        try {
            Double progress = progressData.get("progress");
            if (progress == null || progress < 0 || progress > 100) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Progress must be between 0 and 100"));
            }

            Bus bus = busService.findById(id);
            if (bus == null) {
                return ResponseEntity.notFound().build();
            }

            bus.setProgress(progress);

            // Update arrival time based on new progress
            if (!bus.getStopped() && bus.getDepartureTime() != null) {
                if (progress >= 100.0) {
                    bus.setArrivalTime(LocalDateTime.now());
                } else {
                    Duration elapsed = Duration.between(bus.getDepartureTime(), LocalDateTime.now());
                    double timePerProgress = elapsed.toMinutes() / progress;
                    double remainingProgress = 100.0 - progress;
                    long remainingMinutes = Math.round(remainingProgress * timePerProgress);
                    bus.setArrivalTime(LocalDateTime.now().plusMinutes(remainingMinutes));
                }
            }

            Bus updatedBus = busService.save(bus);
            return ResponseEntity.ok(updatedBus);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating progress: " + e.getMessage()));
        }
    }

    /**
     * Update passenger count
     */
    @PutMapping("/{id}/passengers")
    public ResponseEntity<?> updatePassengerCount(@PathVariable Long id, @RequestBody Map<String, Integer> passengerData) {
        try {
            Integer passengerCount = passengerData.get("passengers");
            if (passengerCount == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Passenger count is required"));
            }

            Bus updatedBus = busService.updatePassengerCount(id, passengerCount);
            return ResponseEntity.ok(updatedBus);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating passenger count: " + e.getMessage()));
        }
    }

    /**
     * Get bus route details
     */
    @GetMapping("/{id}/route-details")
    public ResponseEntity<?> getBusRouteDetails(@PathVariable Long id) {
        try {
            Bus bus = busService.findById(id);
            if (bus == null || bus.getRoute() == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            Route route = bus.getRoute();

            // Get first and last stops
            Stop firstStop = route.getFirstStop();
            Stop lastStop = route.getLastStop();

            response.put("routeName", route.getRouteName());
            response.put("origin", firstStop != null ? firstStop.getStopName() : "Unknown");
            response.put("destination", lastStop != null ? lastStop.getStopName() : "Unknown");
            response.put("totalStops", route.getStops() != null ? route.getStops().size() : 0);
            response.put("estimatedDistance", busService.calculateTotalDistance(bus));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving route details: " + e.getMessage()));
        }
    }

    /**
     * Get bus report with analytics
     */
    @GetMapping("/report/{id}")
    public ResponseEntity<?> getBusReport(@PathVariable Long id) {
        try {
            Bus bus = busService.findById(id);
            if (bus == null) {
                return ResponseEntity.notFound().build();
            }

            BusReport report = busService.generateBusReport(bus);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error generating bus report: " + e.getMessage()));
        }
    }

    /**
     * Get available drivers for assignment
     */
    @GetMapping("/available-drivers")
    public ResponseEntity<?> getAvailableDrivers() {
        try {
            List<Staff> drivers = staffService.getActiveStaffByRole(StaffRole.DRIVER)
                    .stream()
                    .map(staffDTO -> staffService.getStaffEntityById(staffDTO.getId()))
                    .filter(driver -> {
                        // Only include drivers not assigned to active buses
                        Bus assignedBus = busService.findByDriverId(driver.getId());
                        return assignedBus == null || assignedBus.getStopped();
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving available drivers: " + e.getMessage()));
        }
    }

    /**
     * Get buses by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getBusesByStatus(@PathVariable String status) {
        try {
            List<Bus> buses = busService.getBusesByStatus(status);
            return ResponseEntity.ok(buses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving buses by status: " + e.getMessage()));
        }
    }

    /**
     * Get active buses
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveBuses() {
        try {
            List<Bus> activeBuses = busService.findActiveBuses();
            return ResponseEntity.ok(activeBuses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving active buses: " + e.getMessage()));
        }
    }

    /**
     * Get buses with accidents
     */
    @GetMapping("/accidents")
    public ResponseEntity<?> getBusesWithAccidents() {
        try {
            List<Bus> busesWithAccidents = busService.findBusesWithAccidents();
            return ResponseEntity.ok(busesWithAccidents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving buses with accidents: " + e.getMessage()));
        }
    }

    /**
     * Get bus statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getBusStatistics() {
        try {
            List<Bus> allBuses = busService.findAll();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBuses", allBuses.size());
            stats.put("activeBuses", allBuses.stream().filter(bus -> !bus.getStopped()).count());
            stats.put("stoppedBuses", allBuses.stream().filter(Bus::getStopped).count());
            stats.put("busesWithAccidents", allBuses.stream().filter(Bus::getHasAccident).count());
            stats.put("totalPassengers", allBuses.stream().mapToInt(bus -> bus.getPassengers() != null ? bus.getPassengers() : 0).sum());
            stats.put("totalCapacity", allBuses.stream().mapToInt(bus -> bus.getCapacity() != null ? bus.getCapacity() : 0).sum());

            double avgOccupancy = allBuses.stream()
                    .filter(bus -> bus.getCapacity() != null && bus.getCapacity() > 0)
                    .mapToDouble(bus -> ((double) (bus.getPassengers() != null ? bus.getPassengers() : 0) / bus.getCapacity()) * 100)
                    .average()
                    .orElse(0.0);
            stats.put("averageOccupancyRate", Math.round(avgOccupancy * 100.0) / 100.0);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving bus statistics: " + e.getMessage()));
        }
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getBusByDriver(@PathVariable Long driverId) {
        try {
            // Updated to return LIST of buses instead of single bus
            List<Bus> buses = busService.findAllByDriverId(driverId);
            return ResponseEntity.ok(buses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving buses for driver: " + e.getMessage()));
        }
    }

    /**
     * Get buses by route
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<?> getBusesByRoute(@PathVariable Long routeId) {
        try {
            List<Bus> buses = busService.findByRouteId(routeId);
            return ResponseEntity.ok(buses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving buses for route: " + e.getMessage()));
        }
    }

    /**
     * Update arrival time manually
     */
    @PutMapping("/{id}/arrival-time")
    public ResponseEntity<?> updateArrivalTime(@PathVariable Long id, @RequestBody Map<String, String> timeData) {
        try {
            Bus bus = busService.findById(id);
            if (bus == null) {
                return ResponseEntity.notFound().build();
            }

            String arrivalTimeStr = timeData.get("arrivalTime");
            if (arrivalTimeStr == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Arrival time is required"));
            }

            LocalDateTime arrivalTime = LocalDateTime.parse(arrivalTimeStr);
            bus.setArrivalTime(arrivalTime);

            Bus updatedBus = busService.save(bus);
            return ResponseEntity.ok(updatedBus);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error updating arrival time: " + e.getMessage()));
        }
    }

    // Helper Methods

    /**
     * Send email notification to driver about bus assignment
     */
    private void sendDriverNotification(Staff driver, Bus bus, BusCreateRequest request) {
        try {
            String subject = "New Bus Assignment - " + bus.getName();
            String routeInfo = "";

            // Safe route information retrieval
            try {
                Route route = routeService.findById(request.getRouteId());
                if (route != null) {
                    routeInfo = route.getRouteName();
                } else {
                    routeInfo = "Route information not available";
                }
            } catch (Exception e) {
                routeInfo = "Route information not available";
            }

            String body = String.format(
                    "Dear %s %s,\n\n" +
                            "You have been assigned to drive bus %s (%s).\n\n" +
                            "Trip Details:\n" +
                            "- Route: %s\n" +
                            "- Departure: %s\n" +
                            "- Estimated Arrival: %s\n" +
                            "- Capacity: %d passengers\n" +
                            "- Estimated Duration: %d minutes\n\n" +
                            "Please be ready 30 minutes before departure time.\n\n" +
                            "Safe travels!\n" +
                            "IMAS Management Team",
                    driver.getFirstName(),
                    driver.getLastName(),
                    bus.getName(),
                    bus.getBusLine(),
                    routeInfo,
                    request.getDepartureTime() != null ? request.getDepartureTime().toString() : "TBD",
                    bus.getArrivalTime() != null ? bus.getArrivalTime().toString() : "TBD",
                    bus.getCapacity(),
                    request.getEstimatedDuration() != null ? request.getEstimatedDuration() : 0
            );

            emailService.sendEmail(driver.getEmail(), subject, body);
        } catch (Exception e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
            // Don't throw exception as this is not critical for bus creation
        }
    }

    /**
     * Create standardized error response
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        return errorResponse;
    }

    // DTOs

    /**
     * Request DTO for creating/updating buses
     */
    public static class BusCreateRequest {
        private String name;
        private String busLine;
        private Integer capacity;
        private Double startLat;
        private Double startLng;
        private Double endLat;
        private Double endLng;
        private LocalDateTime departureTime;
        private Long driverId;
        private Long routeId;
        private Integer estimatedDuration;

        // Constructors
        public BusCreateRequest() {}

        public BusCreateRequest(String name, String busLine, Integer capacity, Long routeId) {
            this.name = name;
            this.busLine = busLine;
            this.capacity = capacity;
            this.routeId = routeId;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getBusLine() { return busLine; }
        public void setBusLine(String busLine) { this.busLine = busLine; }

        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }

        public Double getStartLat() { return startLat; }
        public void setStartLat(Double startLat) { this.startLat = startLat; }

        public Double getStartLng() { return startLng; }
        public void setStartLng(Double startLng) { this.startLng = startLng; }

        public Double getEndLat() { return endLat; }
        public void setEndLat(Double endLat) { this.endLat = endLat; }

        public Double getEndLng() { return endLng; }
        public void setEndLng(Double endLng) { this.endLng = endLng; }

        public LocalDateTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

        public Long getDriverId() { return driverId; }
        public void setDriverId(Long driverId) { this.driverId = driverId; }

        public Long getRouteId() { return routeId; }
        public void setRouteId(Long routeId) { this.routeId = routeId; }

        public Integer getEstimatedDuration() { return estimatedDuration; }
        public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

        // Validation method
        public boolean isValid() {
            return name != null && !name.trim().isEmpty() &&
                    capacity != null && capacity > 0 &&
                    routeId != null;
        }
    }





    /**
     * DTO for position updates
     */
    public static class PositionUpdate {
        private Double currentLat;
        private Double currentLng;
        private Double progress;

        // Constructors
        public PositionUpdate() {}

        public PositionUpdate(Double currentLat, Double currentLng, Double progress) {
            this.currentLat = currentLat;
            this.currentLng = currentLng;
            this.progress = progress;
        }

        // Getters and setters
        public Double getCurrentLat() { return currentLat; }
        public void setCurrentLat(Double currentLat) { this.currentLat = currentLat; }

        public Double getCurrentLng() { return currentLng; }
        public void setCurrentLng(Double currentLng) { this.currentLng = currentLng; }

        public Double getProgress() { return progress; }
        public void setProgress(Double progress) { this.progress = progress; }

        // Validation method
        public boolean isValid() {
            return currentLat != null && currentLng != null &&
                    currentLat >= -90 && currentLat <= 90 &&
                    currentLng >= -180 && currentLng <= 180 &&
                    (progress == null || (progress >= 0 && progress <= 100));
        }
    }
}