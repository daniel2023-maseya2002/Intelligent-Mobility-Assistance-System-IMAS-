package IMAS.ImasProject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "maintenance_records")
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // CHANGEMENT PRINCIPAL : Fetch EAGER pour charger l'équipement avec ses données
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipment_id")
    // SUPPRESSION de @JsonBackReference pour permettre la sérialisation de l'équipment
    private Equipment equipment;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority;

    @Column(name = "description")
    private String description;

    // Enum pour la priorité
    public enum Priority {
        HIGH, MEDIUM, LOW
    }

    // Constructeurs
    public MaintenanceRecord() {
    }

    public MaintenanceRecord(Equipment equipment, LocalDate startDate, LocalDate endDate,
                             Integer estimatedHours, Priority priority, String description) {
        this.equipment = equipment;
        this.startDate = startDate;
        this.endDate = endDate;
        this.estimatedHours = estimatedHours;
        this.priority = priority;
        this.description = description;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "MaintenanceRecord{" +
                "id=" + id +
                ", equipmentId=" + (equipment != null ? equipment.getEquipmentId() : null) +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", estimatedHours=" + estimatedHours +
                ", priority=" + priority +
                ", description='" + description + '\'' +
                '}';
    }
}