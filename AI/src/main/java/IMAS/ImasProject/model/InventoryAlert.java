// ========== INVENTORY ALERT ENTITY ==========
package IMAS.ImasProject.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_alerts")
public class InventoryAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alertId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    @JsonBackReference
    private Inventory inventory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status;

    @Column(nullable = false)
    private String message;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    @Column(name = "resolved_by")
    private String resolvedBy;

    // Enums
    public enum AlertType {
        LOW_STOCK, OVERSTOCK, EXPIRED, OBSOLETE, AUDIT_REQUIRED
    }

    public enum AlertStatus {
        ACTIVE, RESOLVED, DISMISSED
    }

    // Constructors
    public InventoryAlert() {
        this.createdDate = LocalDateTime.now();
        this.status = AlertStatus.ACTIVE;
    }

    public InventoryAlert(Inventory inventory, AlertType type, String message) {
        this();
        this.inventory = inventory;
        this.type = type;
        this.message = message;
    }

    // Getters and Setters
    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public void resolve(String resolvedBy) {
        this.status = AlertStatus.RESOLVED;
        this.resolvedDate = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
    }
}