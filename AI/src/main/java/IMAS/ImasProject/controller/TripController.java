package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.Trip;
import IMAS.ImasProject.services.TripService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TripController {
    private static final Logger log = LoggerFactory.getLogger(TripController.class);

    private final TripService tripService;

    @Autowired
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping("/driver/{driverId}/count")
    public ResponseEntity<Long> getDriverTripCount(@PathVariable Long driverId) {
        try {
            long count = tripService.getDriverTripCount(driverId);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting trip count for driver {}: {}", driverId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/driver/{driverId}/stats")
    public ResponseEntity<Map<String, Object>> getDriverStats(@PathVariable Long driverId) {
        try {
            Map<String, Object> stats = tripService.getDriverTripStats(driverId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting stats for driver {}: {}", driverId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/driver/{driverId}/weekly")
    public ResponseEntity<List<Map<String, Object>>> getDriverWeeklyStats(@PathVariable Long driverId) {
        try {
            List<Map<String, Object>> stats = tripService.getDriverWeeklyStats(driverId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting weekly stats for driver {}: {}", driverId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/driver/{driverId}/monthly")
    public ResponseEntity<List<Map<String, Object>>> getDriverMonthlyStats(@PathVariable Long driverId) {
        try {
            List<Map<String, Object>> stats = tripService.getDriverMonthlyStats(driverId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting monthly stats for driver {}: {}", driverId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/driver/{driverId}/ontime")
    public ResponseEntity<Map<String, Object>> getDriverOnTimeStats(@PathVariable Long driverId) {
        try {
            Map<String, Object> stats = tripService.getDriverOnTimeStats(driverId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting on-time stats for driver {}: {}", driverId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Trip>> getTripsByDriver(@PathVariable Long driverId) {
        try {
            List<Trip> trips = tripService.findByDriverId(driverId);
            return ResponseEntity.ok(trips);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error getting trips for driver {}: {}", driverId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<Trip>> getTripsByBus(@PathVariable Long busId) {
        try {
            List<Trip> trips = tripService.findByBusId(busId);
            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            log.error("Error getting trips for bus {}: {}", busId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable Long id) {
        try {
            Optional<Trip> trip = tripService.findById(id);
            return trip.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting trip {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id) {
        try {
            tripService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting trip {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}