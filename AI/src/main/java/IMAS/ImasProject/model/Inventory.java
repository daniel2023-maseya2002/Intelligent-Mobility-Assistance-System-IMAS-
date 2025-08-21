package IMAS.ImasProject.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    @Column(nullable = false, unique = true)
    private String inventoryCode;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @Column(nullable = false)
    private Integer totalQuantity;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Integer reservedQuantity;

    @Column
    private String location;

    @Column
    private String category;

    @Column(name = "minimum_threshold")
    private Integer minimumThreshold;

    @Column(name = "maximum_threshold")
    private Integer maximumThreshold;

    @Column(name = "unit_cost")
    private Double unitCost;

    @Column(name = "total_value")
    private Double totalValue;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "last_audit_date")
    private LocalDateTime lastAuditDate;

    // Relationships
    @OneToOne
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @OneToOne
    @JoinColumn(name = "spare_part_id")
    private SparePart sparePart;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<InventoryTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<InventoryAlert> alerts = new ArrayList<>();

    // Enums
    public enum ItemType {
        EQUIPMENT, SPARE_PART, CONSUMABLE, TOOL, ACCESSORY
    }

    public enum ItemStatus {
        ACTIVE, INACTIVE, DISCONTINUED, OBSOLETE, UNDER_REVIEW
    }

    // Constructors
    public Inventory() {
        this.createdDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.totalQuantity = 0;
        this.availableQuantity = 0;
        this.reservedQuantity = 0;
    }

    public Inventory(String inventoryCode, String name, String description, ItemType itemType) {
        this();
        this.inventoryCode = inventoryCode;
        this.name = name;
        this.description = description;
        this.itemType = itemType;
        this.status = ItemStatus.ACTIVE;
    }

    // Getters and Setters
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

    public String getInventoryCode() { return inventoryCode; }
    public void setInventoryCode(String inventoryCode) { this.inventoryCode = inventoryCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer availableQuantity) { this.availableQuantity = availableQuantity; }

    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getMinimumThreshold() { return minimumThreshold; }
    public void setMinimumThreshold(Integer minimumThreshold) { this.minimumThreshold = minimumThreshold; }

    public Integer getMaximumThreshold() { return maximumThreshold; }
    public void setMaximumThreshold(Integer maximumThreshold) { this.maximumThreshold = maximumThreshold; }

    public Double getUnitCost() { return unitCost; }
    public void setUnitCost(Double unitCost) { this.unitCost = unitCost; }

    public Double getTotalValue() { return totalValue; }
    public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public LocalDateTime getLastAuditDate() { return lastAuditDate; }
    public void setLastAuditDate(LocalDateTime lastAuditDate) { this.lastAuditDate = lastAuditDate; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    public SparePart getSparePart() { return sparePart; }
    public void setSparePart(SparePart sparePart) { this.sparePart = sparePart; }

    public List<InventoryTransaction> getTransactions() { return transactions; }
    public void setTransactions(List<InventoryTransaction> transactions) { this.transactions = transactions; }

    public List<InventoryAlert> getAlerts() { return alerts; }
    public void setAlerts(List<InventoryAlert> alerts) { this.alerts = alerts; }

    // Business Methods
    public boolean addStock(int quantity, String reason, String performedBy) {
        if (quantity <= 0) return false;

        this.totalQuantity += quantity;
        this.availableQuantity += quantity;
        this.lastUpdated = LocalDateTime.now();

        // Update total value if unit cost is available
        if (this.unitCost != null) {
            this.totalValue = this.totalQuantity * this.unitCost;
        }

        // Create transaction record
        InventoryTransaction transaction = new InventoryTransaction(
                this, InventoryTransaction.TransactionType.STOCK_IN,
                quantity, reason, performedBy
        );
        this.transactions.add(transaction);

        return true;
    }

    public boolean removeStock(int quantity, String reason, String performedBy) {
        if (quantity <= 0 || quantity > this.availableQuantity) return false;

        this.totalQuantity -= quantity;
        this.availableQuantity -= quantity;
        this.lastUpdated = LocalDateTime.now();

        // Update total value
        if (this.unitCost != null) {
            this.totalValue = this.totalQuantity * this.unitCost;
        }

        // Create transaction record
        InventoryTransaction transaction = new InventoryTransaction(
                this, InventoryTransaction.TransactionType.STOCK_OUT,
                quantity, reason, performedBy
        );
        this.transactions.add(transaction);

        return true;
    }

    public boolean reserveStock(int quantity, String reason, String performedBy) {
        if (quantity <= 0 || quantity > this.availableQuantity) return false;

        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
        this.lastUpdated = LocalDateTime.now();

        // Create transaction record
        InventoryTransaction transaction = new InventoryTransaction(
                this, InventoryTransaction.TransactionType.RESERVED,
                quantity, reason, performedBy
        );
        this.transactions.add(transaction);

        return true;
    }

    public boolean releaseReservedStock(int quantity, String reason, String performedBy) {
        if (quantity <= 0 || quantity > this.reservedQuantity) return false;

        this.availableQuantity += quantity;
        this.reservedQuantity -= quantity;
        this.lastUpdated = LocalDateTime.now();

        // Create transaction record
        InventoryTransaction transaction = new InventoryTransaction(
                this, InventoryTransaction.TransactionType.RELEASED,
                quantity, reason, performedBy
        );
        this.transactions.add(transaction);

        return true;
    }

    public boolean isLowStock() {
        return this.minimumThreshold != null && this.totalQuantity <= this.minimumThreshold;
    }

    public boolean isOverStock() {
        return this.maximumThreshold != null && this.totalQuantity >= this.maximumThreshold;
    }

    public double getStockLevel() {
        if (this.maximumThreshold == null) return 0.0;
        return (double) this.totalQuantity / this.maximumThreshold;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryId=" + inventoryId +
                ", inventoryCode='" + inventoryCode + '\'' +
                ", name='" + name + '\'' +
                ", itemType=" + itemType +
                ", totalQuantity=" + totalQuantity +
                ", availableQuantity=" + availableQuantity +
                ", location='" + location + '\'' +
                '}';
    }
}