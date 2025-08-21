package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.LoginAttempt;
import IMAS.ImasProject.services.AnalyticService;
import IMAS.ImasProject.services.LoginTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticService analyticService;

    @Autowired
    private LoginTrackingService loginTrackingService;

    @GetMapping("/system-overview")
    public ResponseEntity<Map<String, Object>> getSystemOverview() {
        return ResponseEntity.ok(analyticService.getSystemOverview());
    }

    @GetMapping("/login-statistics/{period}")
    public ResponseEntity<Map<String, Object>> getLoginStatistics(@PathVariable String period) {
        return ResponseEntity.ok(analyticService.getLoginStatistics(period));
    }

    @GetMapping("/financial-statistics/{period}")
    public ResponseEntity<Map<String, Object>> getFinancialStatistics(@PathVariable String period) {
        return ResponseEntity.ok(analyticService.getFinancialStatistics(period));
    }

    @GetMapping("/passenger-statistics")
    public ResponseEntity<Map<String, Object>> getPassengerStatistics() {
        return ResponseEntity.ok(analyticService.getPassengerStatistics());
    }

    @GetMapping("/bus-performance/{period}")
    public ResponseEntity<Map<String, Object>> getBusPerformance(@PathVariable String period) {
        return ResponseEntity.ok(analyticService.getBusPerformanceStatistics(period));
    }

    @GetMapping("/driver-revenue/{period}")
    public ResponseEntity<Map<String, Object>> getDriverRevenue(@PathVariable String period) {
        return ResponseEntity.ok(analyticService.getDriverRevenueStatistics(period));
    }

    @GetMapping("/recent-logins/{limit}")
    public ResponseEntity<List<LoginAttempt>> getRecentLogins(@PathVariable int limit) {
        return ResponseEntity.ok(loginTrackingService.getRecentLoginAttempts(limit));
    }

    @GetMapping("/login-attempts")
    public ResponseEntity<List<LoginAttempt>> getLoginAttempts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(analyticService.getLoginAttempts(page, size));
    }

    @GetMapping("/failed-logins")
    public ResponseEntity<List<LoginAttempt>> getFailedLogins(
            @RequestParam(defaultValue = "24") int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return ResponseEntity.ok(loginTrackingService.getFailedLoginAttempts(since));
    }

    @GetMapping("/login-attempts/{email}")
    public ResponseEntity<List<LoginAttempt>> getLoginAttemptsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(loginTrackingService.getLoginAttemptsByEmail(email));
    }

    @GetMapping("/login-stats-by-role/{period}")
    public ResponseEntity<Map<?, Long>> getLoginStatsByRole(@PathVariable String period) {
        return ResponseEntity.ok(analyticService.getLoginStatsByRole(period));
    }
}