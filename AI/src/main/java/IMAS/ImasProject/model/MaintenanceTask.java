package IMAS.ImasProject.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "maintenance_tasks")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class MaintenanceTask implements Serializable {

    // Enum for task priority levels
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }



    public enum Status {
        PLANNED,       // Tâche planifiée mais pas encore programmée
        SCHEDULED,     // Tâche programmée dans le calendrier (ajoutez cette valeur)
        PENDING,       // En attente de traitement
        ASSIGNED,      // Assignée à un technicien
        IN_PROGRESS,   // En cours de réalisation
        ON_HOLD,       // En pause
        COMPLETED,     // Terminée
        CANCELLED      // Annulée
    }

    // Attributes
    @Id
    private String taskId;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    private long estimatedDurationMinutes; // Store duration as minutes for JPA compatibility

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_required_skills", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "skill")
    private List<String> requiredSkills = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_required_parts", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "part")
    private List<String> requiredParts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // Relation avec Equipment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_equipment_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Equipment relatedEquipment;

    // Relation avec Staff (technician)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_technician_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Staff assignedTechnicianStaff;

    // Garder aussi l'ID pour compatibilité - CORRIGÉ: utiliser Long au lieu de String
    private Long assignedTechnician;

    @Column(nullable = false)
    private double completionPercentage = 0.0;

    // Dates
    @Column(nullable = false)
    private LocalDateTime creationDate;

    private LocalDateTime dueDate;

    private LocalDateTime lastUpdated;

    private LocalDateTime completionDate;

    // Constructeurs
    public MaintenanceTask() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.status = Status.PLANNED;
        this.completionPercentage = 0.0;
        this.requiredSkills = new ArrayList<>();
        this.requiredParts = new ArrayList<>();
    }

    public MaintenanceTask(String taskId, String description, Priority priority) {
        this();
        this.taskId = taskId;
        this.description = description;
        this.priority = priority;
    }

    // Getters et Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public long getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(long estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public List<String> getRequiredSkills() {
        if (requiredSkills == null) {
            requiredSkills = new ArrayList<>();
        }
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
    }

    public List<String> getRequiredParts() {
        if (requiredParts == null) {
            requiredParts = new ArrayList<>();
        }
        return requiredParts;
    }

    public void setRequiredParts(List<String> requiredParts) {
        this.requiredParts = requiredParts != null ? requiredParts : new ArrayList<>();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }

    public Equipment getRelatedEquipment() {
        return relatedEquipment;
    }

    public void setRelatedEquipment(Equipment relatedEquipment) {
        this.relatedEquipment = relatedEquipment;
    }

    public Staff getAssignedTechnicianStaff() {
        return assignedTechnicianStaff;
    }

    public void setAssignedTechnicianStaff(Staff assignedTechnicianStaff) {
        this.assignedTechnicianStaff = assignedTechnicianStaff;
        if (assignedTechnicianStaff != null) {
            // CORRIGÉ: utiliser getId() au lieu de getStaffId()
            this.assignedTechnician = assignedTechnicianStaff.getId();
        } else {
            this.assignedTechnician = null;
        }
    }

    // CORRIGÉ: changer le type de retour de String à Long
    public Long getAssignedTechnician() {
        return assignedTechnician;
    }

    // CORRIGÉ: changer le paramètre de String à Long
    public void setAssignedTechnician(Long assignedTechnician) {
        this.assignedTechnician = assignedTechnician;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
        this.lastUpdated = LocalDateTime.now();
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
    }

    // Méthodes utilitaires
    @PrePersist
    public void prePersist() {
        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
        if (requiredSkills == null) {
            requiredSkills = new ArrayList<>();
        }
        if (requiredParts == null) {
            requiredParts = new ArrayList<>();
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "MaintenanceTask{" +
                "taskId='" + taskId + '\'' +
                ", description='" + description + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", completionPercentage=" + completionPercentage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaintenanceTask)) return false;
        MaintenanceTask that = (MaintenanceTask) o;
        return taskId != null && taskId.equals(that.taskId);
    }

    @Override
    public int hashCode() {
        return taskId != null ? taskId.hashCode() : 0;
    }
}