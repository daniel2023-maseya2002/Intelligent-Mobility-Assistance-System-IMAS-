package IMAS.ImasProject.controller;



import IMAS.ImasProject.dto.IncidentDTO;
import IMAS.ImasProject.model.*;
import IMAS.ImasProject.repository.IncidentRepository;
import IMAS.ImasProject.services.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping
    public ResponseEntity<Incident> createIncident(@RequestBody Map<String, Object> rawData) {
        try {
            System.out.println("Received incident data: " + rawData);
            Incident incident = new Incident();

            // Set the type
            try {
                String incidentTypeStr = (String) rawData.get("incidentType");
                System.out.println("Processing incidentType: " + incidentTypeStr);
                incident.setIncidentType(Incident.IncidentType.valueOf(incidentTypeStr));
            } catch (Exception e) {
                System.err.println("Error parsing incidentType: " + e.getMessage());
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            // Set the location
            String location = (String) rawData.get("location");
            System.out.println("Setting location: " + location);
            incident.setLocation(location);

            // Parse and set the dateTime
            try {
                String dateTimeStr = (String) rawData.get("dateTime");
                System.out.println("Processing dateTime: " + dateTimeStr);

                if (dateTimeStr.endsWith("Z")) {
                    dateTimeStr = dateTimeStr.substring(0, dateTimeStr.length() - 1);
                }

                LocalDateTime dateTime;
                try {
                    dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
                } catch (DateTimeParseException e) {
                    if (dateTimeStr.contains(".")) {
                        dateTimeStr = dateTimeStr.substring(0, dateTimeStr.indexOf("."));
                    }
                    dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }

                incident.setDateTime(dateTime);
                System.out.println("DateTime set to: " + dateTime);
            } catch (Exception e) {
                System.err.println("Error parsing dateTime: " + e.getMessage());
                e.printStackTrace();
                LocalDateTime now = LocalDateTime.now();
                incident.setDateTime(now);
                System.out.println("Using current time as fallback: " + now);
            }

            // Set the summary
            String summary = (String) rawData.get("summary");
            System.out.println("Setting summary: " + summary);
            incident.setSummary(summary);

            // Set the status
            try {
                String statusStr = (String) rawData.get("status");
                System.out.println("Processing status: " + statusStr);
                incident.setStatus(Incident.IncidentStatus.valueOf(statusStr));
            } catch (Exception e) {
                System.err.println("Error parsing status: " + e.getMessage());
                e.printStackTrace();
                incident.setStatus(Incident.IncidentStatus.REPORTED);
                System.out.println("Using default status: REPORTED");
            }

            // Set the busId
            try {
                Object busIdObj = rawData.get("busId");
                if (busIdObj != null) {
                    Long busId = Long.valueOf(busIdObj.toString());
                    incident.setBusId(busId);
                    System.out.println("Setting busId: " + busId);
                }
            } catch (Exception e) {
                System.err.println("Error parsing busId: " + e.getMessage());
                e.printStackTrace();
            }

            // Set the severity
            try {
                String severityStr = (String) rawData.get("severity");
                System.out.println("Processing severity: " + severityStr);
                incident.setSeverity(Incident.Severity.valueOf(severityStr));
            } catch (Exception e) {
                System.err.println("Error parsing severity: " + e.getMessage());
                e.printStackTrace();
                incident.setSeverity(Incident.Severity.LOW);
                System.out.println("Using default severity: LOW");
            }

            // Set additional details
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> additionalDetailsRaw = (Map<String, Object>) rawData.get("additionalDetails");
                if (additionalDetailsRaw != null) {
                    Map<String, String> additionalDetails = new HashMap<>();
                    for (Map.Entry<String, Object> entry : additionalDetailsRaw.entrySet()) {
                        additionalDetails.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                    incident.setAdditionalDetails(additionalDetails);
                    System.out.println("Additional details set: " + additionalDetails);
                } else {
                    System.out.println("No additional details provided");
                }
            } catch (Exception e) {
                System.err.println("Error processing additional details: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("Saving incident to database");
            Incident createdIncident = incidentService.createIncident(incident);
            System.out.println("Successfully created incident with ID: " + createdIncident.getIncidentId());
            return new ResponseEntity<>(createdIncident, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("Error creating incident: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/technician/{technicianId}/history")
    public ResponseEntity<List<Incident>> getIncidentHistoryForTechnician(@PathVariable Long technicianId) {
        List<Incident> history = incidentService.getIncidentHistoryForTechnician(technicianId);
        return ResponseEntity.ok(history);
    }





    @Transactional
    public Incident assignTechnicianToIncident(String incidentId, Long technicianId,
                                               String taskDescription, String taskPriority,
                                               String taskDeadline) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found with id: " + incidentId));

        Staff technician = staffService.findById(technicianId)
                .orElseThrow(() -> new EntityNotFoundException("Technician not found with id: " + technicianId));

        incident.setAssignedTechnicianId(technician.getId());
        incident.setAssignedTechnicianName(technician.getFullName());

        try {
            incident.setStatus(Incident.IncidentStatus.ASSIGNED);
        } catch (Exception e) {
            throw new RuntimeException("Error setting incident status: " + e.getMessage(), e);
        }

        Map<String, String> additionalDetails = incident.getAdditionalDetails();
        if (additionalDetails == null) {
            additionalDetails = new HashMap<>();
        }

        additionalDetails.put("taskDescription", taskDescription);
        additionalDetails.put("taskPriority", taskPriority);
        additionalDetails.put("taskDeadline", taskDeadline);

        incident.setAdditionalDetails(additionalDetails);

        return incidentRepository.save(incident);
    }

    @PatchMapping("/{incidentId}/assign-technician")
    public ResponseEntity<?> assignTechnicianToIncident(
            @PathVariable("incidentId") String incidentId,
            @Valid @RequestBody AssignTechnicianRequest request) {
        try {
            if (!request.isValid()) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Invalid request data", "details", request));
            }

            Incident updatedIncident = incidentService.assignTechnicianToIncident(
                    incidentId,
                    request.getTechnicianId(),
                    request.getTaskDescription(),
                    request.getTaskPriority(),
                    request.getTaskDeadline());

            Task.Priority priority = Task.Priority.valueOf(request.getTaskPriority().toUpperCase());
            LocalDateTime deadline = request.getTaskDeadline() != null ?
                    LocalDateTime.parse(request.getTaskDeadline()) : null;

            Task task = taskService.createTask(
                    incidentId,
                    request.getTechnicianId(),
                    request.getTaskDescription(),
                    priority,
                    deadline);

            Staff technician = staffService.findById(request.getTechnicianId())
                    .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

            sendAssignmentEmail(technician, updatedIncident, request.getTaskDescription());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "incident", updatedIncident,
                    "task", task,
                    "technician", technician.getFullName()
            ));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/frequency")
    public ResponseEntity<Map<String, Double>> getIncidentFrequency() {
        Map<String, Double> frequencyMap = incidentService.calculateIncidentFrequency();
        return ResponseEntity.ok(frequencyMap);
    }

    @GetMapping("/severity")
    public ResponseEntity<Map<String, Double>> getIncidentSeverity() {
        Map<String, Double> severityMap = incidentService.calculateIncidentSeverity();
        return ResponseEntity.ok(severityMap);
    }

    @GetMapping("/trends")
    public ResponseEntity<Map<String, Double>> getIncidentTrends() {
        Map<String, Double> trendsMap = incidentService.calculateTrends();
        return ResponseEntity.ok(trendsMap);
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> generateIncidentReport() {
        Map<String, Object> report = incidentService.generateIncidentReport();
        return ResponseEntity.ok(report);
    }

    @GetMapping
    public ResponseEntity<List<Incident>> getAllIncidents() {
        List<Incident> incidents = incidentService.getAllIncidents();
        return ResponseEntity.ok(incidents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIncidentById(@PathVariable String id) {
        try {
            return incidentRepository.findById(id)
                    .map(incident -> ResponseEntity.ok(convertToDTO(incident)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch incident details: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Incident> updateIncident(
            @PathVariable("id") String incidentId,
            @RequestBody Incident incidentDetails) {
        try {
            Incident updatedIncident = incidentService.updateIncident(incidentId, incidentDetails);
            return ResponseEntity.ok(updatedIncident);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getIncidentStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String type) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;

            try {
                startDateTime = LocalDateTime.parse(startDate, formatter);
            } catch (DateTimeParseException e) {
                startDateTime = LocalDate.parse(startDate).atStartOfDay();
            }

            try {
                endDateTime = LocalDateTime.parse(endDate, formatter);
            } catch (DateTimeParseException e) {
                endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
            }

            Map<String, Object> stats = new HashMap<>();

            long totalIncidents;
            if (type != null && !type.equals("allIncidents")) {
                try {
                    Incident.IncidentType incidentType = Incident.IncidentType.valueOf(type);
                    totalIncidents = incidentRepository.countByIncidentTypeAndDateTimeBetween(
                            incidentType, startDateTime, endDateTime);
                } catch (IllegalArgumentException e) {
                    totalIncidents = incidentRepository.countByDateTimeBetween(startDateTime, endDateTime);
                }
            } else {
                totalIncidents = incidentRepository.countByDateTimeBetween(startDateTime, endDateTime);
            }

            long majorIncidents = incidentRepository.countByDateTimeBetweenAndSeverity(
                    startDateTime, endDateTime, Incident.Severity.HIGH) +
                    incidentRepository.countByDateTimeBetweenAndSeverity(
                            startDateTime, endDateTime, Incident.Severity.CRITICAL);

            Double avgResolutionTime = incidentRepository.getAverageResolutionTime(startDateTime, endDateTime);
            if (avgResolutionTime == null) {
                avgResolutionTime = 0.0;
            }

            double safetyScore = calculateSafetyScore(startDateTime, endDateTime);

            int totalIncidentsChange = calculatePercentageChange(totalIncidents, totalIncidents * 1.1);
            int majorIncidentsChange = calculatePercentageChange(majorIncidents, majorIncidents * 1.05);
            int avgResolutionTimeChange = -5;
            int safetyScoreChange = 3;

            List<Map<String, Object>> incidentsByType =
                    incidentRepository.getIncidentCountByType(startDateTime, endDateTime);
            if (incidentsByType == null || incidentsByType.isEmpty()) {
                incidentsByType = createDefaultIncidentTypes();
            }

            List<Map<String, Object>> incidentTrend =
                    incidentRepository.getIncidentTrend(startDateTime, endDateTime);
            if (incidentTrend == null || incidentTrend.isEmpty()) {
                incidentTrend = createDefaultIncidentTrend(startDateTime, endDateTime);
            }

            stats.put("totalIncidents", totalIncidents);
            stats.put("majorIncidents", majorIncidents);
            stats.put("avgResolutionTime", formatResolutionTime(avgResolutionTime));
            stats.put("safetyScore", Math.round(safetyScore * 100.0) / 100.0);

            stats.put("totalIncidentsChange", totalIncidentsChange);
            stats.put("majorIncidentsChange", majorIncidentsChange);
            stats.put("avgResolutionTimeChange", avgResolutionTimeChange);
            stats.put("safetyScoreChange", safetyScoreChange);

            Map<String, Object> typesData = new HashMap<>();
            typesData.put("labels", incidentsByType.stream().map(m -> m.get("type")).collect(Collectors.toList()));
            typesData.put("data", incidentsByType.stream().map(m -> m.get("count")).collect(Collectors.toList()));
            stats.put("incidentTypes", typesData);

            Map<String, Object> trendData = new HashMap<>();
            trendData.put("labels", incidentTrend.stream().map(m -> m.get("date")).collect(Collectors.toList()));
            trendData.put("totalData", incidentTrend.stream().map(m -> m.get("total")).collect(Collectors.toList()));
            trendData.put("majorData", incidentTrend.stream().map(m -> m.get("major")).collect(Collectors.toList()));
            stats.put("incidentTrend", trendData);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "startDate", startDate,
                            "endDate", endDate,
                            "type", type != null ? type : "null"
                    ));
        }
    }

    private int calculatePercentageChange(double current, double previous) {
        if (previous == 0) return 0;
        return (int) Math.round((current - previous) / previous * 100);
    }

    private List<Map<String, Object>> createDefaultIncidentTypes() {
        List<Map<String, Object>> defaults = new ArrayList<>();
        for (Incident.IncidentType type : Incident.IncidentType.values()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("type", type.name());
            entry.put("count", 0);
            defaults.add(entry);
        }
        return defaults;
    }

    private List<Map<String, Object>> createDefaultIncidentTrend(LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> defaults = new ArrayList<>();
        LocalDateTime current = startDate;
        while (current.isBefore(endDate)) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("date", current.format(DateTimeFormatter.ISO_LOCAL_DATE));
            entry.put("total", 0);
            entry.put("major", 0);
            defaults.add(entry);
            current = current.plusDays(7);
        }
        return defaults;
    }

    private String formatResolutionTime(Double hours) {
        if (hours == null) return "0h 0m";
        int h = (int) Math.floor(hours);
        int m = (int) Math.round((hours - h) * 60);
        return h + "h " + m + "m";
    }

    private double calculateSafetyScore(LocalDateTime startDate, LocalDateTime endDate) {
        long totalIncidents = incidentRepository.countByDateTimeBetween(startDate, endDate);
        long majorIncidents = incidentRepository.countByDateTimeBetweenAndSeverity(
                startDate, endDate, Incident.Severity.HIGH) +
                incidentRepository.countByDateTimeBetweenAndSeverity(
                        startDate, endDate, Incident.Severity.CRITICAL);
        if (totalIncidents == 0) return 100.0;
        return 100.0 - ((double) majorIncidents / totalIncidents * 100.0);
    }

    @GetMapping("/stats/incidents")
    public ResponseEntity<Map<String, Object>> getIncidentStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type) {
        Map<String, Object> stats = incidentService.getIncidentStatistics(startDate, endDate, type);
        return ResponseEntity.ok(stats);
    }

    private Map<String, Object> convertToDTO(Incident incident) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("incidentId", incident.getIncidentId());
        dto.put("incidentType", incident.getIncidentType() != null ? incident.getIncidentType().name() : null);
        dto.put("location", incident.getLocation());
        dto.put("severity", incident.getSeverity() != null ? incident.getSeverity().name() : null);
        dto.put("dateTime", incident.getDateTime() != null ? incident.getDateTime().toString() : null);
        dto.put("assignedTechnicianId", incident.getAssignedTechnicianId());
        dto.put("assignedTechnicianName", incident.getAssignedTechnicianName());
        dto.put("summary", incident.getSummary());
        dto.put("busId", incident.getBusId());
        dto.put("resolutionTime", incident.getResolutionTime() != null ? incident.getResolutionTime().toString() : null);
        dto.put("status", incident.getStatus() != null ? incident.getStatus().name() : null);
        dto.put("additionalDetails", incident.getAdditionalDetails());

        if (incident.getAssignedTeam() != null) {
            dto.put("assignedTeamId", incident.getAssignedTeam().getId());
            dto.put("assignedTeamName", incident.getAssignedTeam().getName());
        }

        return dto;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentIncidents(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;

            try {
                startDateTime = LocalDateTime.parse(startDate, formatter);
            } catch (DateTimeParseException e) {
                startDateTime = LocalDate.parse(startDate).atStartOfDay();
            }

            try {
                endDateTime = LocalDateTime.parse(endDate, formatter);
            } catch (DateTimeParseException e) {
                endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
            }

            List<Incident> incidents = incidentRepository.findByDateTimeBetweenOrderByDateTimeDesc(
                    startDateTime, endDateTime);

            if (incidents == null) {
                incidents = new ArrayList<>();
            }

            List<Map<String, Object>> incidentDTOs = incidents.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(incidentDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<IncidentDTO> assignTechnicianOrTeam(
            @PathVariable("id") String incidentId,
            @RequestBody IncidentService.AssignmentRequestDTO assignmentRequest) {
        IncidentDTO updatedIncident = incidentService.assignTechnicianOrTeam(incidentId, assignmentRequest);

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found with id: " + incidentId));

        if (assignmentRequest.isTeamAssignment()) {
            Team team = teamService.getTeamById(assignmentRequest.getTeamId()).getTeam();
            sendTeamAssignmentEmails(team, incident, assignmentRequest.getTaskDescription());

            webSocketService.sendIncidentUpdateByType(
                    "TEAM_ASSIGNED",
                    String.format("Team '%s' has been assigned to incident #%s", team.getName(), incidentId)
            );
        } else {
            for (Long techId : assignmentRequest.getTechnicianIds()) {
                Staff technician = staffService.findById(techId)
                        .orElseThrow(() -> new EntityNotFoundException("Technician not found with id: " + techId));

                sendAssignmentEmail(technician, incident, assignmentRequest.getTaskDescription());
            }

            if (!assignmentRequest.getTechnicianIds().isEmpty()) {
                int techCount = assignmentRequest.getTechnicianIds().size();
                Staff firstTech = staffService.findById(assignmentRequest.getTechnicianIds().get(0))
                        .orElseThrow(() -> new EntityNotFoundException("Technician not found"));

                webSocketService.sendIncidentUpdateByType(
                        "TECHNICIAN_ASSIGNED",
                        String.format("%d technician(s) have been assigned to incident #%s",
                                techCount, incidentId)
                );
            }
        }

        return new ResponseEntity<>(updatedIncident, HttpStatus.OK);
    }

    private void sendAssignmentEmail(Staff technician, Incident incident, String taskDescription) {
        String subject = "New Assignment - Bus Incident Alert";
        String body = String.format(
                "Dear %s,\n\n" +
                        "You have been assigned to handle a bus incident with the following details:\n\n" +
                        "Incident ID: %s\n" +
                        "Location: %s\n" +
                        "Date/Time: %s\n" +
                        "Description: %s\n\n" +
                        "Task Details:\n%s\n\n" +
                        "Please log in to the system to view complete details and update the status.\n\n" +
                        "Best regards,\nImasProject Team",
                technician.getFullName(),
                incident.getIncidentId(),
                incident.getLocation(),
                incident.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                incident.getSummary(),
                taskDescription
        );
        emailService.sendEmail(technician.getEmail(), subject, body);
    }

    private void sendTeamAssignmentEmails(Team team, Incident incident, String taskDescription) {
        if (team.getMembers() != null && !team.getMembers().isEmpty()) {
            for (Staff member : team.getMembers()) {
                sendTeamMemberAssignmentEmail(member, team, incident, taskDescription);
            }
        }
    }

    private void sendTeamMemberAssignmentEmail(Staff technician, Team team, Incident incident, String taskDescription) {
        String subject = "New Team Assignment - Bus Incident Alert";
        String body = String.format(
                "Dear %s,\n\n" +
                        "You and your team '%s' have been assigned to handle a bus incident with the following details:\n\n" +
                        "Incident ID: %s\n" +
                        "Location: %s\n" +
                        "Date/Time: %s\n" +
                        "Description: %s\n\n" +
                        "Task Details:\n%s\n\n" +
                        "Please coordinate with your team members and log in to the system to view complete details and update the status.\n\n" +
                        "Best regards,\nImasProject Team",
                technician.getFullName(),
                team.getName(),
                incident.getIncidentId(),
                incident.getLocation(),
                incident.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                incident.getSummary(),
                taskDescription
        );
        emailService.sendEmail(technician.getEmail(), subject, body);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Incident> updateIncidentStatus(
            @PathVariable("id") String incidentId,
            @RequestBody String status) {
        try {
            Incident.IncidentStatus newStatus = Incident.IncidentStatus.valueOf(status);
            Incident updatedIncident = incidentService.updateIncidentStatus(incidentId, newStatus);
            return ResponseEntity.ok(updatedIncident);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{id}/details")
    public ResponseEntity<Incident> addIncidentDetails(
            @PathVariable("id") String incidentId,
            @RequestBody Map<String, String> details) {
        try {
            Incident updatedIncident = incidentService.addAdditionalDetails(incidentId, details);
            return ResponseEntity.ok(updatedIncident);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable("id") String incidentId) {
        try {
            incidentService.deleteIncident(incidentId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/by-bus/{busId}")
    public ResponseEntity<Incident> getLatestIncidentByBusId(@PathVariable("busId") Long busId) {
        try {
            Incident incident = incidentService.getLatestIncidentByBusId(busId);
            return ResponseEntity.ok(incident);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
