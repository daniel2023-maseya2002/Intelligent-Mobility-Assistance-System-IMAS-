package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.Incident;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class IncidentDTO {
    private String incidentId;
    private String incidentType; // CORRECTION: Changé en String pour la sérialisation
    private String location;
    private String severity; // CORRECTION: Ajouté le champ severity manquant
    private LocalDateTime dateTime;
    private Long assignedTechnicianId;
    private String assignedTechnicianName;
    private String summary;
    private Long busId;
    private LocalDateTime resolutionTime; // CORRECTION: Ajouté le champ resolutionTime manquant
    private String status; // CORRECTION: Changé en String pour la sérialisation
    private Long assignedTeamId; // CORRECTION: Ajouté le champ assignedTeamId manquant
    private String assignedTeamName; // CORRECTION: Ajouté le champ assignedTeamName manquant
    private Map<String, String> additionalDetails;
    private Set<TechnicianAssignmentDTO> assignedTechnicians;

    // Constructors
    public IncidentDTO() {}

    public IncidentDTO(Incident incident) {
        this.incidentId = incident.getIncidentId();
        this.incidentType = incident.getIncidentType() != null ? incident.getIncidentType().name() : null;
        this.location = incident.getLocation();
        this.severity = incident.getSeverity() != null ? incident.getSeverity().name() : null;
        this.dateTime = incident.getDateTime();
        this.assignedTechnicianId = incident.getAssignedTechnicianId();
        this.assignedTechnicianName = incident.getAssignedTechnicianName();
        this.summary = incident.getSummary();
        this.busId = incident.getBusId();
        this.resolutionTime = incident.getResolutionTime();
        this.status = incident.getStatus() != null ? incident.getStatus().name() : null;
        this.assignedTeamId = incident.getAssignedTeamId();
        this.additionalDetails = incident.getAdditionalDetails() != null ?
                new HashMap<>(incident.getAdditionalDetails()) : new HashMap<>();

        // Set team name if team is assigned
        if (incident.getAssignedTeam() != null) {
            this.assignedTeamName = incident.getAssignedTeam().getName();
        }

        // Convert technician assignments if they exist
        if (incident.getAssignedTechnicians() != null) {
            this.assignedTechnicians = incident.getAssignedTechnicians().stream()
                    .map(TechnicianAssignmentDTO::new)
                    .collect(Collectors.toSet());
        }
    }

    // Getters and setters
    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    // CORRECTION: Ajout des getters/setters pour severity
    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Long getAssignedTechnicianId() {
        return assignedTechnicianId;
    }

    public void setAssignedTechnicianId(Long assignedTechnicianId) {
        this.assignedTechnicianId = assignedTechnicianId;
    }

    public String getAssignedTechnicianName() {
        return assignedTechnicianName;
    }

    public void setAssignedTechnicianName(String assignedTechnicianName) {
        this.assignedTechnicianName = assignedTechnicianName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getBusId() {
        return busId;
    }

    public void setBusId(Long busId) {
        this.busId = busId;
    }

    // CORRECTION: Ajout des getters/setters pour resolutionTime
    public LocalDateTime getResolutionTime() {
        return resolutionTime;
    }

    public void setResolutionTime(LocalDateTime resolutionTime) {
        this.resolutionTime = resolutionTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // CORRECTION: Ajout des getters/setters pour assignedTeamId
    public Long getAssignedTeamId() {
        return assignedTeamId;
    }

    public void setAssignedTeamId(Long assignedTeamId) {
        this.assignedTeamId = assignedTeamId;
    }

    // CORRECTION: Ajout des getters/setters pour assignedTeamName
    public String getAssignedTeamName() {
        return assignedTeamName;
    }

    public void setAssignedTeamName(String assignedTeamName) {
        this.assignedTeamName = assignedTeamName;
    }

    public Map<String, String> getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(Map<String, String> additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    public Set<TechnicianAssignmentDTO> getAssignedTechnicians() {
        return assignedTechnicians;
    }

    public void setAssignedTechnicians(Set<TechnicianAssignmentDTO> assignedTechnicians) {
        this.assignedTechnicians = assignedTechnicians;
    }

    @Override
    public String toString() {
        return "IncidentDTO{" +
                "incidentId='" + incidentId + '\'' +
                ", incidentType='" + incidentType + '\'' +
                ", location='" + location + '\'' +
                ", severity='" + severity + '\'' +
                ", dateTime=" + dateTime +
                ", assignedTechnicianId=" + assignedTechnicianId +
                ", assignedTechnicianName='" + assignedTechnicianName + '\'' +
                ", summary='" + summary + '\'' +
                ", busId=" + busId +
                ", resolutionTime=" + resolutionTime +
                ", status='" + status + '\'' +
                ", assignedTeamId=" + assignedTeamId +
                ", assignedTeamName='" + assignedTeamName + '\'' +
                ", additionalDetails=" + additionalDetails +
                ", assignedTechnicians=" + assignedTechnicians +
                '}';
    }
}