package IMAS.ImasProject.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipments")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long equipmentId;

    private String name;
    private String model;
    private String serialNumber;
    private LocalDate installationDate;
    private String location;

    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    private LocalDate lastMaintenanceDate;

    @Column(name = "inventory_id")
    private Long inventoryId;

    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore  // AJOUTER cette ligne pour éviter la référence circulaire
    private List<MaintenanceRecord> maintenanceHistory = new ArrayList<>();

    // Enums
    public enum EquipmentStatus {
        OPERATIONAL, UNDER_MAINTENANCE, DEFECTIVE, RETIRED
    }

    // Constructeurs
    public Equipment() {
    }

    public Equipment(String name, String model, String serialNumber, LocalDate installationDate,
                     String location, EquipmentStatus status, LocalDate lastMaintenanceDate) {
        this.name = name;
        this.model = model;
        this.serialNumber = serialNumber;
        this.installationDate = installationDate;
        this.location = location;
        this.status = status;
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    // Getters et Setters
    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(LocalDate installationDate) {
        this.installationDate = installationDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public List getMaintenanceHistory() {
        return maintenanceHistory;
    }

    public void setMaintenanceHistory(List<MaintenanceRecord> maintenanceHistory) {
        this.maintenanceHistory = maintenanceHistory;
    }

    // Méthodes métier<MaintenanceRecord>
    public void updateStatus(EquipmentStatus newStatus) {
        this.status = newStatus;
    }

    public List<MaintenanceRecord> getMaintenanceHistory(LocalDate startDate, LocalDate endDate) {
        List<MaintenanceRecord> filteredHistory = new ArrayList<>();
        for (MaintenanceRecord record : maintenanceHistory) {
            LocalDate recordStartDate = record.getStartDate();
            if ((recordStartDate.isEqual(startDate) || recordStartDate.isAfter(startDate)) &&
                    (recordStartDate.isEqual(endDate) || recordStartDate.isBefore(endDate))) {
                filteredHistory.add(record);
            }
        }
        return filteredHistory;
    }

    public double calculateReliability() {
        if (maintenanceHistory.isEmpty()) {
            return 1.0;
        }

        int totalIssues = 0;
        for (MaintenanceRecord record : maintenanceHistory) {
            if (record.getPriority() == MaintenanceRecord.Priority.HIGH) {
                totalIssues++;
            }
        }

        double timeSinceInstallation = Math.max(1, LocalDate.now().toEpochDay() - installationDate.toEpochDay());
        double issuesPerDay = totalIssues / timeSinceInstallation;

        return Math.max(0, 1 - (issuesPerDay * 100));
    }

    public void addMaintenanceRecord(MaintenanceRecord record) {
        if (record != null) {
            if (record.getEquipment() == null) {
                record.setEquipment(this);
            }
            this.maintenanceHistory.add(record);
            // CHANGER : Utiliser endDate au lieu de startDate pour lastMaintenanceDate
            if (record.getEndDate() != null) {
                this.lastMaintenanceDate = record.getEndDate();
            }
        }
    }

    @Override
    public String toString() {
        return "Equipment{" +
                "equipmentId=" + equipmentId +
                ", name='" + name + '\'' +
                ", model='" + model + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", status=" + status +
                ", location='" + location + '\'' +
                '}';
    }
}