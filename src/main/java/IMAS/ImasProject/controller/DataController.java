package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.Incident;
import IMAS.ImasProject.model.MaintenanceRecord;
import IMAS.ImasProject.repository.IncidentRepository;
import IMAS.ImasProject.repository.MaintenanceRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/data")
public class DataController {
    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Autowired
    private MaintenanceRecordRepository maintenanceRepo;

    @Autowired
    private IncidentRepository incidentRepo;

    @GetMapping("/all-maintenance-records")
    public ResponseEntity<List<Map<String, Object>>> getAllMaintenanceRecords(
            @RequestParam(required = false) Long equipmentId) {
        log.info("[getAllMaintenanceRecords] equipmentId={}", equipmentId);
        List<MaintenanceRecord> records = equipmentId != null
                ? maintenanceRepo.findByEquipmentEquipmentId(equipmentId)
                : maintenanceRepo.findAll();

        if (equipmentId != null && records.isEmpty()) {
            log.info("[getAllMaintenanceRecords] No records found for equipmentId={}", equipmentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(List.of(Map.of("error", "No maintenance records found for equipmentId " + equipmentId)));
        }

        List<Map<String, Object>> response = records.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("equipmentId", record.getEquipment() != null ? record.getEquipment().getEquipmentId() : null);
            map.put("startDate", record.getStartDate() != null ? record.getStartDate().toString() : null);
            map.put("endDate", record.getEndDate() != null ? record.getEndDate().toString() : null);
            map.put("estimatedHours", record.getEstimatedHours());
            map.put("priority", record.getPriority() != null ? record.getPriority().toString() : null);
            map.put("description", record.getDescription());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/incidents")
    public List<Incident> getIncidents(@RequestParam(required = false) Long busId) {
        log.info("[getIncidents] busId={}", busId);
        return busId != null
                ? incidentRepo.findByBusId(busId)
                : incidentRepo.findAll();
    }

    @PostMapping("/predictions/maintenance")
    public ResponseEntity<?> getMaintenancePrediction(
            @RequestBody Map<String, String> req) {
        log.info("[getMaintenancePrediction] Received: {}", req);

        String eqStr = req.get("equipmentId");
        if (eqStr == null || eqStr.trim().isEmpty()) {
            log.error("[getMaintenancePrediction] equipmentId missing");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "equipmentId is required"));
        }

        Long eqId;
        try {
            eqId = Long.valueOf(eqStr);
        } catch (NumberFormatException ex) {
            log.error("[getMaintenancePrediction] Invalid equipmentId format: {}", eqStr);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid equipmentId format"));
        }

        String url = "http://localhost:5000/predict";
        RestTemplate rt = new RestTemplate();
        Map<String, Object> requestBody = Map.of("equipmentId", eqId.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        log.info("[getMaintenancePrediction] Posting to Flask: {}", requestBody);

        try {
            Map<String, Object> flaskResp = rt.postForObject(url, entity, Map.class);
            log.info("[getMaintenancePrediction] Flask response: {}", flaskResp);
            return ResponseEntity.ok(flaskResp);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("[getMaintenancePrediction] Flask error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            return ResponseEntity.status(ex.getStatusCode())
                    .body(Map.of(
                            "error", ex.getResponseBodyAsString().contains("No maintenance records found")
                                    ? ex.getResponseBodyAsString()
                                    : "Invalid equipment ID",
                            "detail", ex.getResponseBodyAsString()
                    ));
        } catch (Exception ex) {
            log.error("[getMaintenancePrediction] Unexpected error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error", "detail", ex.getMessage()));
        }
    }

    @PostMapping("/notifications")
    public ResponseEntity<?> postNotification(@RequestBody Map<String, Object> payload) {
        log.info("[postNotification] Received: {}", payload);
        return ResponseEntity.ok(Map.of("status", "received"));
    }
}