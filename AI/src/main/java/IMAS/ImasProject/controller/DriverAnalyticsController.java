package IMAS.ImasProject.controller;

import IMAS.ImasProject.services.TripService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics/driver")
public class DriverAnalyticsController {
    private static final Logger log = LoggerFactory.getLogger(DriverAnalyticsController.class);

    @Autowired
    private TripService tripService;

    /**
     * Get monthly trip statistics for a driver
     */
    @GetMapping("/{driverId}/monthly")
    public ResponseEntity<List<Map<String, Object>>> getDriverMonthlyStats(@PathVariable Long driverId) {
        try {
            List<Map<String, Object>> stats = tripService.getDriverMonthlyStats(driverId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching monthly stats for driver ID {}: {}", driverId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Unexpected error fetching monthly stats for driver ID {}: {}", driverId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get weekly trip statistics for a driver
     */
    @GetMapping("/{driverId}/weekly")
    public ResponseEntity<List<Map<String, Object>>> getDriverWeeklyStats(@PathVariable Long driverId) {
        try {
            List<Map<String, Object>> stats = tripService.getDriverWeeklyStats(driverId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching weekly stats for driver ID {}: {}", driverId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Unexpected error fetching weekly stats for driver ID {}: {}", driverId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Get on-time performance statistics for a driver
     */
    @GetMapping("/{driverId}/ontime")
    public ResponseEntity<Map<String, Object>> getDriverOnTimeStats(@PathVariable Long driverId) {
        try {
            Map<String, Object> stats = tripService.getDriverOnTimeStats(driverId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching on-time stats for driver ID {}: {}", driverId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Unexpected error fetching on-time stats for driver ID {}: {}", driverId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}