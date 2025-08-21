package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.*;
import IMAS.ImasProject.services.ScheduleBusService;
import IMAS.ImasProject.services.BusService;
import IMAS.ImasProject.services.StaffService;
import IMAS.ImasProject.services.RouteService;
import IMAS.ImasProject.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedule-buses")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ScheduleBusController {

    @Autowired
    private ScheduleBusService scheduleBusService;

    @Autowired
    private BusService busService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody ScheduleCreateRequest request) {
        try {
            // Validate required fields
            if (request.getBusId() == null || request.getDriverId() == null ||
                    request.getRouteId() == null || request.getDepartureTime() == null ||
                    request.getEstimatedDurationMinutes() == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            // Fetch entities - Using the correct methods from your BusService
            Bus bus = busService.findById(request.getBusId());
            Optional<Staff> driverOpt = staffService.findById(request.getDriverId());
            Route route = routeService.findById(request.getRouteId());

            if (bus == null) {
                return ResponseEntity.badRequest().body("Bus not found");
            }
            if (!driverOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Driver not found");
            }
            if (route == null) {
                return ResponseEntity.badRequest().body("Route not found");
            }

            Staff driver = driverOpt.get();

            // Check if driver is already assigned to another bus
            if (busService.isDriverAssignedToBus(request.getDriverId())) {
                Bus existingBus = busService.findByDriverId(request.getDriverId());
                if (!existingBus.getId().equals(bus.getId())) {
                    return ResponseEntity.badRequest().body("Driver is already assigned to another bus");
                }
            }

            // Create schedule
            ScheduleBus schedule = new ScheduleBus();
            schedule.setBus(bus);
            schedule.setDriver(driver);
            schedule.setRoute(route);
            schedule.setDayOfWeek(request.getDayOfWeek());
            schedule.setDepartureTime(request.getDepartureTime());
            schedule.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
            schedule.setArrivalTime(request.getDepartureTime().plusMinutes(request.getEstimatedDurationMinutes()));
            schedule.setIsActive(true);
            schedule.setCreatedAt(LocalDateTime.now());
            schedule.setUpdatedAt(LocalDateTime.now());

            ScheduleBus savedSchedule = scheduleBusService.save(schedule);

            // Send email notification to driver
            sendScheduleNotification(driver, savedSchedule, "New Bus Schedule Assignment");

            return ResponseEntity.ok(savedSchedule);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating schedule: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ScheduleBus>> getAllSchedules() {
        try {
            List<ScheduleBus> schedules = scheduleBusService.findAll();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleBus> getScheduleById(@PathVariable Long id) {
        try {
            ScheduleBus schedule = scheduleBusService.findById(id);
            if (schedule != null) {
                return ResponseEntity.ok(schedule);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<ScheduleBus>> getSchedulesByBusId(@PathVariable Long busId) {
        try {
            List<ScheduleBus> schedules = scheduleBusService.findByBusId(busId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<ScheduleBus>> getSchedulesByDay(@PathVariable String dayOfWeek) {
        try {
            DayOfWeek dow = DayOfWeek.valueOf(dayOfWeek.toUpperCase());
            List<ScheduleBus> schedules = scheduleBusService.findByDayOfWeek(dow);
            return ResponseEntity.ok(schedules);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody ScheduleCreateRequest request) {
        try {
            ScheduleBus existingSchedule = scheduleBusService.findById(id);
            if (existingSchedule == null) {
                return ResponseEntity.notFound().build();
            }

            // Track if driver has changed
            boolean driverChanged = false; // Fixed: removed 'astonished:' which was invalid syntax
            Staff oldDriver = existingSchedule.getDriver();

            // Update fields
            if (request.getBusId() != null) {
                Bus bus = busService.findById(request.getBusId()); // Fixed: using correct method
                if (bus != null) {
                    existingSchedule.setBus(bus);
                }
            }

            if (request.getDriverId() != null) {
                Optional<Staff> driverOpt = staffService.findById(request.getDriverId());
                if (driverOpt.isPresent()) {
                    driverChanged = !driverOpt.get().getId().equals(oldDriver.getId());
                    existingSchedule.setDriver(driverOpt.get());

                    // Check if driver is already assigned to another bus
                    if (busService.isDriverAssignedToBus(request.getDriverId())) {
                        Bus existingBus = busService.findByDriverId(request.getDriverId());
                        if (!existingBus.getId().equals(existingSchedule.getBus().getId())) {
                            return ResponseEntity.badRequest().body("Driver is already assigned to another bus");
                        }
                    }
                }
            }

            if (request.getRouteId() != null) {
                Route route = routeService.findById(request.getRouteId());
                if (route != null) {
                    existingSchedule.setRoute(route);
                }
            }

            if (request.getDayOfWeek() != null) {
                existingSchedule.setDayOfWeek(request.getDayOfWeek());
            }

            if (request.getDepartureTime() != null) {
                existingSchedule.setDepartureTime(request.getDepartureTime());
            }

            if (request.getEstimatedDurationMinutes() != null) {
                existingSchedule.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
            }

            // Recalculate arrival time
            if (request.getDepartureTime() != null || request.getEstimatedDurationMinutes() != null) {
                int duration = request.getEstimatedDurationMinutes() != null ?
                        request.getEstimatedDurationMinutes() :
                        existingSchedule.getEstimatedDurationMinutes();
                LocalDateTime departureTime = request.getDepartureTime() != null ?
                        request.getDepartureTime() :
                        existingSchedule.getDepartureTime();
                existingSchedule.setArrivalTime(departureTime.plusMinutes(duration));
            }

            existingSchedule.setUpdatedAt(LocalDateTime.now());
            ScheduleBus updatedSchedule = scheduleBusService.save(existingSchedule);

            // Send email notification to driver if driver has changed or schedule details updated
            if (driverChanged || request.getDepartureTime() != null ||
                    request.getEstimatedDurationMinutes() != null ||
                    request.getRouteId() != null ||
                    request.getDayOfWeek() != null) {
                sendScheduleNotification(
                        existingSchedule.getDriver(),
                        updatedSchedule,
                        "Bus Schedule Update Notification"
                );
            }

            return ResponseEntity.ok(updatedSchedule);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating schedule: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        try {
            ScheduleBus schedule = scheduleBusService.findById(id);
            if (schedule == null) {
                return ResponseEntity.notFound().build();
            }

            scheduleBusService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting schedule: " + e.getMessage());
        }
    }

    private void sendScheduleNotification(Staff driver, ScheduleBus schedule, String subject) {
        try {
            String body = String.format(
                    "Dear %s %s,\n\n" +
                            "Your bus schedule has been %s:\n\n" +
                            "Bus: %s\n" +
                            "Route: %s\n" +
                            "Day: %s\n" +
                            "Departure Time: %s\n" +
                            "Estimated Duration: %d minutes\n" +
                            "Arrival Time: %s\n\n" +
                            "Best regards,\nIMAS Management Team",
                    driver.getFirstName(),
                    driver.getLastName(),
                    subject.toLowerCase().contains("update") ? "updated" : "created",
                    schedule.getBus().getName(),
                    schedule.getRoute().getName(),
                    schedule.getDayOfWeek().toString(),
                    schedule.getDepartureTime().toString(),
                    schedule.getEstimatedDurationMinutes(),
                    schedule.getArrivalTime().toString()
            );

            emailService.sendEmail(driver.getEmail(), subject, body);
        } catch (Exception e) {
            // Log error but don't fail the request
            // Assuming a logger is available similar to StaffController
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
    }

    public static class ScheduleCreateRequest {
        private Long busId;
        private Long driverId;
        private Long routeId;
        private DayOfWeek dayOfWeek;
        private LocalDateTime departureTime;
        private Integer estimatedDurationMinutes;

        // Getters and setters
        public Long getBusId() { return busId; }
        public void setBusId(Long busId) { this.busId = busId; }

        public Long getDriverId() { return driverId; }
        public void setDriverId(Long driverId) { this.driverId = driverId; }

        public Long getRouteId() { return routeId; }
        public void setRouteId(Long routeId) { this.routeId = routeId; }

        public DayOfWeek getDayOfWeek() { return dayOfWeek; }
        public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

        public LocalDateTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

        public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
        public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
            this.estimatedDurationMinutes = estimatedDurationMinutes;
        }
    }
}