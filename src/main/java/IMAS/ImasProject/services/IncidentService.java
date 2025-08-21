package IMAS.ImasProject.services;

import IMAS.ImasProject.dto.IncidentDTO;
import IMAS.ImasProject.exception.ResourceNotFoundException;
import IMAS.ImasProject.model.Incident;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.Task;
import IMAS.ImasProject.model.Team;
import IMAS.ImasProject.repository.IncidentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final StaffService staffService;
    private TaskService taskService;

    // Constructor injection with @Lazy to avoid circular dependency
    public IncidentService(IncidentRepository incidentRepository,
                           StaffService staffService,
                           @Lazy TaskService taskService) {
        this.incidentRepository = incidentRepository;
        this.staffService = staffService;
        this.taskService = taskService;
    }

    // Alternative setter injection for TaskService if needed
    @Autowired
    @Lazy
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Transactional
    public Incident createIncident(Incident incident) {
        if (incident.getDateTime() == null) {
            incident.setDateTime(LocalDateTime.now());
        }
        if (incident.getStatus() == null) {
            incident.setStatus(Incident.IncidentStatus.REPORTED);
        }
        return incidentRepository.save(incident);
    }

    public Optional<Incident> getIncidentById(String incidentId) {
        return incidentRepository.findById(incidentId);
    }

    public Incident findIncidentById(String incidentId) {
        return incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));
    }

    public List<Incident> getIncidentHistoryForTechnician(Long technicianId) {
        return incidentRepository.findByAssignedTechnicianIdOrderByDateTimeDesc(technicianId);
    }

    @Transactional
    public Incident assignTechnicianToIncident(String incidentId, Long technicianId,
                                               String taskDescription, String taskPriority,
                                               String taskDeadline) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Bus incident not found with id: " + incidentId));

        Staff technician = staffService.findById(technicianId)
                .orElseThrow(() -> new EntityNotFoundException("Technician not found with id: " + technicianId));

        incident.setAssignedTechnicianId(technician.getId());
        incident.setAssignedTechnicianName(technician.getFirstName() + " " + technician.getLastName());
        incident.setStatus(Incident.IncidentStatus.ASSIGNED);

        Map<String, String> additionalDetails = incident.getAdditionalDetails();
        if (additionalDetails == null) {
            additionalDetails = new java.util.HashMap<>();
        }
        additionalDetails.put("taskDescription", taskDescription);
        additionalDetails.put("taskPriority", taskPriority);
        additionalDetails.put("taskDeadline", taskDeadline);
        incident.setAdditionalDetails(additionalDetails);

        return incidentRepository.save(incident);
    }

    public long countByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return 0;
        }
        return incidentRepository.findAll().stream()
                .filter(incident -> location.equals(incident.getLocation()))
                .count();
    }

    public List<Incident> findByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return List.of();
        }
        return incidentRepository.findAll().stream()
                .filter(incident -> location.equals(incident.getLocation()))
                .collect(Collectors.toList());
    }

    public Map<String, Double> calculateIncidentFrequency() {
        List<Incident> incidents = incidentRepository.findAll();
        return incidents.stream()
                .collect(Collectors.groupingBy(
                        incident -> incident.getIncidentType().name(),
                        Collectors.averagingLong(incident -> 1L)));
    }

    public Map<String, Double> calculateIncidentSeverity() {
        List<Incident> incidents = incidentRepository.findAll();
        return incidents.stream()
                .collect(Collectors.groupingBy(
                        incident -> incident.getSeverity().name(),
                        Collectors.averagingLong(incident -> 1L)));
    }

    public Map<String, Double> calculateTrends() {
        List<Incident> incidents = incidentRepository.findAll();
        return incidents.stream()
                .collect(Collectors.groupingBy(
                        incident -> incident.getDateTime().toLocalDate().toString(),
                        Collectors.averagingLong(incident -> 1L)));
    }

    public Map<String, Object> generateIncidentReport() {
        List<Incident> incidents = incidentRepository.findAll();
        Map<String, Object> report = new java.util.HashMap<>();
        report.put("totalBusIncidents", (long) incidents.size());
        report.put("byType", incidents.stream()
                .collect(Collectors.groupingBy(
                        incident -> incident.getIncidentType().name(),
                        Collectors.counting())));
        report.put("bySeverity", incidents.stream()
                .collect(Collectors.groupingBy(
                        incident -> incident.getSeverity().name(),
                        Collectors.counting())));
        return report;
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    @Transactional
    public Incident updateIncident(String incidentId, Incident incidentDetails) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Bus incident not found with id: " + incidentId));

        incident.setIncidentType(incidentDetails.getIncidentType());
        incident.setLocation(incidentDetails.getLocation());
        incident.setDateTime(incidentDetails.getDateTime());
        incident.setSummary(incidentDetails.getSummary());
        incident.setStatus(incidentDetails.getStatus());
        incident.setBusId(incidentDetails.getBusId());
        incident.setSeverity(incidentDetails.getSeverity());
        incident.setAdditionalDetails(incidentDetails.getAdditionalDetails());

        return incidentRepository.save(incident);
    }

    public Map<String, Object> getIncidentStatistics(LocalDate startDate, LocalDate endDate, String type) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        Map<String, Object> stats = new java.util.HashMap<>();
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

        stats.put("totalBusIncidents", totalIncidents);
        stats.put("majorBusIncidents", incidentRepository.countByDateTimeBetweenAndSeverity(
                startDateTime, endDateTime, Incident.Severity.HIGH) +
                incidentRepository.countByDateTimeBetweenAndSeverity(
                        startDateTime, endDateTime, Incident.Severity.CRITICAL));

        return stats;
    }

    @Transactional
    public IncidentDTO assignTechnicianOrTeam(String incidentId, AssignmentRequestDTO assignmentRequest) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Bus incident not found with id: " + incidentId));

        if (assignmentRequest.isTeamAssignment()) {
            incident.setAssignedTeamId(assignmentRequest.getTeamId());
        } else {
            List<Long> technicianIds = assignmentRequest.getTechnicianIds();
            if (!technicianIds.isEmpty()) {
                Staff technician = staffService.findById(technicianIds.get(0))
                        .orElseThrow(() -> new EntityNotFoundException("Technician not found"));
                incident.setAssignedTechnicianId(technician.getId());
                incident.setAssignedTechnicianName(technician.getFirstName() + " " + technician.getLastName());
            }
        }

        incident.setStatus(Incident.IncidentStatus.ASSIGNED);
        Incident savedIncident = incidentRepository.save(incident);

        return convertToDTO(savedIncident);
    }

    @Transactional
    public Incident updateIncidentStatus(String incidentId, Incident.IncidentStatus status) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Bus incident not found with id: " + incidentId));

        incident.setStatus(status);
        if (status == Incident.IncidentStatus.RESOLVED) {
            incident.setResolutionTime(LocalDateTime.now());
        }

        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident addAdditionalDetails(String incidentId, Map<String, String> details) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new EntityNotFoundException("Bus incident not found with id: " + incidentId));

        Map<String, String> existingDetails = incident.getAdditionalDetails();
        if (existingDetails == null) {
            existingDetails = new java.util.HashMap<>();
        }
        existingDetails.putAll(details);
        incident.setAdditionalDetails(existingDetails);

        return incidentRepository.save(incident);
    }

    @Transactional
    public void deleteIncident(String incidentId) {
        if (!incidentRepository.existsById(incidentId)) {
            throw new EntityNotFoundException("Bus incident not found with id: " + incidentId);
        }
        incidentRepository.deleteById(incidentId);
    }

    public Incident getLatestIncidentByBusId(Long busId) {
        return incidentRepository.findTopByBusIdOrderByDateTimeDesc(busId)
                .orElseThrow(() -> new EntityNotFoundException("No incidents found for bus id: " + busId));
    }

    public List<Incident> findByAssignedTechnicianIdOrderByDateTimeDesc(Long technicianId) {
        return incidentRepository.findByAssignedTechnicianIdOrderByDateTimeDesc(technicianId);
    }

    public long countByIncidentTypeAndDateTimeBetween(Incident.IncidentType type, LocalDateTime start, LocalDateTime end) {
        return incidentRepository.countByIncidentTypeAndDateTimeBetween(type, start, end);
    }

    public long countByDateTimeBetween(LocalDateTime start, LocalDateTime end) {
        return incidentRepository.countByDateTimeBetween(start, end);
    }

    public long countByDateTimeBetweenAndSeverity(LocalDateTime start, LocalDateTime end, Incident.Severity severity) {
        return incidentRepository.countByDateTimeBetweenAndSeverity(start, end, severity);
    }

    public Optional<Incident> findTopByBusIdOrderByDateTimeDesc(Long busId) {
        return incidentRepository.findTopByBusIdOrderByDateTimeDesc(busId);
    }

    private IncidentDTO convertToDTO(Incident incident) {
        IncidentDTO dto = new IncidentDTO();
        dto.setIncidentId(incident.getIncidentId());
        dto.setIncidentType(incident.getIncidentType() != null ? incident.getIncidentType().name() : null);
        dto.setLocation(incident.getLocation());
        dto.setSeverity(incident.getSeverity() != null ? incident.getSeverity().name() : null);
        dto.setDateTime(incident.getDateTime());
        dto.setAssignedTechnicianId(incident.getAssignedTechnicianId());
        dto.setAssignedTechnicianName(incident.getAssignedTechnicianName());
        dto.setSummary(incident.getSummary());
        dto.setBusId(incident.getBusId());
        dto.setResolutionTime(incident.getResolutionTime());
        dto.setStatus(incident.getStatus() != null ? incident.getStatus().name() : null);
        dto.setAdditionalDetails(incident.getAdditionalDetails());

        if (incident.getAssignedTeam() != null) {
            dto.setAssignedTeamId(incident.getAssignedTeam().getId());
            dto.setAssignedTeamName(incident.getAssignedTeam().getName());
        } else if (incident.getAssignedTeamId() != null) {
            dto.setAssignedTeamId(incident.getAssignedTeamId());
        }

        return dto;
    }

    // Inner DTO class for assignment requests
    public static class AssignmentRequestDTO {
        private Long teamId;
        private List<Long> technicianIds;
        private String taskDescription;

        public AssignmentRequestDTO() {
            // Default constructor
        }

        public boolean isTeamAssignment() {
            return teamId != null;
        }

        public Long getTeamId() {
            return teamId;
        }

        public void setTeamId(Long teamId) {
            this.teamId = teamId;
        }

        public List<Long> getTechnicianIds() {
            return technicianIds != null ? technicianIds : new java.util.ArrayList<>();
        }

        public void setTechnicianIds(List<Long> technicianIds) {
            this.technicianIds = technicianIds;
        }

        public String getTaskDescription() {
            return taskDescription;
        }

        public void setTaskDescription(String taskDescription) {
            this.taskDescription = taskDescription;
        }
    }
}