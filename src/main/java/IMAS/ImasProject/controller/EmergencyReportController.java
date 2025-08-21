package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.EmergencyReport;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.Bus;
import IMAS.ImasProject.services.EmergencyReportService;
import IMAS.ImasProject.services.StaffService;
import IMAS.ImasProject.services.BusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/emergency-reports")
@CrossOrigin(origins = "*")
public class EmergencyReportController {

    @Autowired
    private EmergencyReportService emergencyReportService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private BusService busService;

    @PostMapping
    public ResponseEntity<?> createEmergencyReport(@RequestBody Map<String, Object> requestData) {
        try {
            EmergencyReport report = new EmergencyReport();

            // Set driver
            Long driverId = Long.valueOf(requestData.get("driverId").toString());
            Optional<Staff> driver = staffService.findById(driverId);
            if (!driver.isPresent()) {
                return ResponseEntity.badRequest().body("Driver not found");
            }
            report.setDriver(driver.get());

            // Set bus (optional) - CORRECTION ICI
            if (requestData.get("busId") != null) {
                Long busId = Long.valueOf(requestData.get("busId").toString());
                Bus bus = busService.findById(busId); // Maintenant on reçoit directement un Bus
                if (bus != null) { // On vérifie si le bus n'est pas null
                    report.setBus(bus);
                }
            }

            // Set report details
            report.setType(EmergencyReport.EmergencyType.valueOf(requestData.get("type").toString()));
            report.setLocation(requestData.get("location").toString());
            report.setDescription(requestData.get("description").toString());
            report.setSeverity(EmergencyReport.EmergencySeverity.valueOf(requestData.get("severity").toString()));

            // Set coordinates if provided
            if (requestData.get("coordinates") != null) {
                Map<String, Object> coords = (Map<String, Object>) requestData.get("coordinates");
                if (coords.get("lat") != null && coords.get("lng") != null) {
                    report.setLatitude(Double.valueOf(coords.get("lat").toString()));
                    report.setLongitude(Double.valueOf(coords.get("lng").toString()));
                }
            }

            EmergencyReport savedReport = emergencyReportService.createEmergencyReport(report);
            return ResponseEntity.ok(savedReport);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating emergency report: " + e.getMessage());
        }
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<EmergencyReport>> getDriverReports(@PathVariable Long driverId) {
        try {
            List<EmergencyReport> reports = emergencyReportService.getDriverReports(driverId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/driver/{driverId}/recent")
    public ResponseEntity<List<EmergencyReport>> getRecentDriverReports(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "30") int days) {
        try {
            List<EmergencyReport> reports = emergencyReportService.getRecentDriverReports(driverId, days);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<EmergencyReport>> getAllReports() {
        try {
            List<EmergencyReport> reports = emergencyReportService.getAllReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyReport> getReportById(@PathVariable Long id) {
        try {
            Optional<EmergencyReport> report = emergencyReportService.getReportById(id);
            return report.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<EmergencyReport> updateReportStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String status = statusUpdate.get("status");
            EmergencyReport updatedReport = emergencyReportService.updateReportStatus(id, status);
            return ResponseEntity.ok(updatedReport);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        try {
            emergencyReportService.deleteReport(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/high-priority")
    public ResponseEntity<List<EmergencyReport>> getHighPriorityReports() {
        try {
            List<EmergencyReport> reports = emergencyReportService.getHighPriorityReports();
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}