package IMAS.ImasProject.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "incident_id", updatable = false, nullable = false)
    private String incidentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "incident_type", nullable = false)
    private IncidentType incidentType;

    @Column(name = "location", nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    private Long assignedTechnicianId;

    private String assignedTechnicianName;

    @Column(name = "summary", length = 500)
    private String summary;

    @Column(name = "bus_id")
    private Long busId;

    @Column(name = "resolution_time")
    private LocalDateTime resolutionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", insertable = false, updatable = false)
    private Bus bus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IncidentStatus status;

    @ManyToOne
    @JoinColumn(name = "assigned_team_id")
    private Team assignedTeam;

    // CORRECTION: Ajout du champ assignedTeamId manquant
    @Column(name = "assigned_team_id", insertable = false, updatable = false)
    private Long assignedTeamId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "incident_details", joinColumns = @JoinColumn(name = "incident_id"))
    @MapKeyColumn(name = "detail_key")
    @Column(name = "detail_value", length = 500)
    private Map<String, String> additionalDetails = new HashMap<>();

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<TechnicianAssignment> assignedTechnicians = new HashSet<>();

    // Enum for Incident Type - adapté pour les bus
    public enum IncidentType {
        COLLISION,              // Collision avec un autre véhicule
        BREAKDOWN,              // Panne mécanique
        TRAFFIC_ACCIDENT,       // Accident de circulation
        PASSENGER_INJURY,       // Blessure de passager
        EQUIPMENT_MALFUNCTION,  // Dysfonctionnement équipement
        TIRE_BLOWOUT,          // Crevaison
        ENGINE_FAILURE,        // Panne moteur
        BRAKE_FAILURE,         // Panne de freins
        DOOR_MALFUNCTION,      // Dysfonctionnement des portes
        FUEL_LEAK,             // Fuite de carburant
        FIRE,                  // Incendie
        VANDALISM,             // Vandalisme
        ROAD_OBSTRUCTION,      // Obstruction de la route
        WEATHER_RELATED        // Incident lié aux conditions météo
    }

    // Enum for Incident Status
    public enum IncidentStatus {
        REPORTED,       // Signalé
        INVESTIGATING,  // En cours d'enquête
        RESPONDING,     // Intervention en cours
        ASSIGNED,       // Assigné à une équipe
        CONTAINED,      // Maîtrisé
        RESOLVED,       // Résolu
        CLOSED         // Fermé
    }

    public enum Severity {
        LOW,        // Faible
        MEDIUM,     // Moyen
        HIGH,       // Élevé
        CRITICAL    // Critique
    }

    // CORRECTION: Ajout des getters/setters pour assignedTeamId
    public Long getAssignedTeamId() {
        return assignedTeamId;
    }

    public void setAssignedTeamId(Long assignedTeamId) {
        this.assignedTeamId = assignedTeamId;
        // Mise à jour de l'équipe assignée si nécessaire
        if (assignedTeamId != null && (this.assignedTeam == null || !assignedTeamId.equals(this.assignedTeam.getId()))) {
            // Note: Vous devrez implémenter la logique pour charger l'équipe depuis la base de données
            // ou utiliser un service pour le faire
        }
    }

    // Method to add additional details
    public void addAdditionalDetail(String key, String value) {
        if (this.additionalDetails == null) {
            this.additionalDetails = new HashMap<>();
        }
        this.additionalDetails.put(key, value);
    }

    // Method to remove additional detail
    public void removeAdditionalDetail(String key) {
        if (this.additionalDetails != null) {
            this.additionalDetails.remove(key);
        }
    }

    // Method to get additional detail
    public String getAdditionalDetail(String key) {
        if (this.additionalDetails != null) {
            return this.additionalDetails.get(key);
        }
        return null;
    }

    // Method to update status
    public void updateStatus(IncidentStatus newStatus) {
        this.status = newStatus;
    }

    // Method to get Bus safely
    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
        if (bus != null) {
            this.busId = bus.getId();
        }
    }

    // Method to check if incident is resolved
    public boolean isResolved() {
        return this.status == IncidentStatus.RESOLVED || this.status == IncidentStatus.CLOSED;
    }

    // Method to check if incident is critical
    public boolean isCritical() {
        return this.severity == Severity.CRITICAL;
    }

    // Method to check if incident is active (not resolved or closed)
    public boolean isActive() {
        return this.status != IncidentStatus.RESOLVED && this.status != IncidentStatus.CLOSED;
    }

    // Method to calculate incident duration
    public Long getDurationInMinutes() {
        if (this.resolutionTime != null && this.dateTime != null) {
            return java.time.Duration.between(this.dateTime, this.resolutionTime).toMinutes();
        }
        return null;
    }

    // Method to get incident age in minutes
    public Long getAgeInMinutes() {
        if (this.dateTime != null) {
            return java.time.Duration.between(this.dateTime, LocalDateTime.now()).toMinutes();
        }
        return null;
    }

    // Method to assign technician
    public void assignTechnician(Long technicianId, String technicianName) {
        this.assignedTechnicianId = technicianId;
        this.assignedTechnicianName = technicianName;
    }

    // Method to clear technician assignment
    public void clearTechnicianAssignment() {
        this.assignedTechnicianId = null;
        this.assignedTechnicianName = null;
    }

    // Method to resolve incident
    public void resolveIncident() {
        this.status = IncidentStatus.RESOLVED;
        this.resolutionTime = LocalDateTime.now();
    }

    // Method to close incident
    public void closeIncident() {
        this.status = IncidentStatus.CLOSED;
        if (this.resolutionTime == null) {
            this.resolutionTime = LocalDateTime.now();
        }
    }

    // Getters et setters
    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public IncidentType getIncidentType() {
        return incidentType;
    }

    public void setIncidentType(IncidentType incidentType) {
        this.incidentType = incidentType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
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

    public LocalDateTime getResolutionTime() {
        return resolutionTime;
    }

    public void setResolutionTime(LocalDateTime resolutionTime) {
        this.resolutionTime = resolutionTime;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public Team getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(Team assignedTeam) {
        this.assignedTeam = assignedTeam;
        if (assignedTeam != null) {
            this.assignedTeamId = assignedTeam.getId();
        } else {
            this.assignedTeamId = null;
        }
    }

    public Map<String, String> getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(Map<String, String> additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    public Set<TechnicianAssignment> getAssignedTechnicians() {
        return assignedTechnicians;
    }

    public void setAssignedTechnicians(Set<TechnicianAssignment> assignedTechnicians) {
        this.assignedTechnicians = assignedTechnicians;
    }

    // Override toString for better logging
    @Override
    public String toString() {
        return "Incident{" +
                "incidentId='" + incidentId + '\'' +
                ", incidentType=" + incidentType +
                ", location='" + location + '\'' +
                ", severity=" + severity +
                ", dateTime=" + dateTime +
                ", status=" + status +
                ", summary='" + (summary != null ? summary.substring(0, Math.min(summary.length(), 20)) + "..." : "null") + '\'' +
                ", busId=" + busId +
                ", assignedTechnicianId=" + assignedTechnicianId +
                ", assignedTechnicianName='" + assignedTechnicianName + '\'' +
                ", assignedTeamId=" + assignedTeamId +
                ", resolutionTime=" + resolutionTime +
                ", additionalDetails=" + (additionalDetails != null ? additionalDetails.size() + " items" : "null") +
                ", assignedTechnicians=" + (assignedTechnicians != null ? assignedTechnicians.size() + " technicians" : "null") +
                '}';
    }

    // Override equals and hashCode based on incidentId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Incident incident = (Incident) o;
        return incidentId != null && incidentId.equals(incident.incidentId);
    }

    @Override
    public int hashCode() {
        return incidentId != null ? incidentId.hashCode() : 0;
    }
}