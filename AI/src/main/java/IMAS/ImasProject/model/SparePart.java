package IMAS.ImasProject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Entity
@Table(name = "spare_parts")
public class SparePart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long partId;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Part number is required")
    private String partNumber;

    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Column(name = "minimum_stock_level", nullable = false)
    @NotNull(message = "Minimum stock level is required")
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minimumStockLevel;

    @Column
    private String location;

    @Column
    private String supplier;

    @Column
    private Double unitPrice;

    @Column
    private String category;

    // Constructors, getters, setters, and other methods remain unchanged
    public SparePart() {
    }

    public SparePart(String partNumber, String name, String description, Integer quantity,
                     Integer minimumStockLevel, String location, String supplier,
                     Double unitPrice, String category) {
        this.partNumber = partNumber;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.minimumStockLevel = minimumStockLevel;
        this.location = location;
        this.supplier = supplier;
        this.unitPrice = unitPrice;
        this.category = category;
    }

    public Long getPartId() {
        return partId;
    }

    public void setPartId(Long partId) {
        this.partId = partId;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getMinimumStockLevel() {
        return minimumStockLevel;
    }

    public void setMinimumStockLevel(Integer minimumStockLevel) {
        this.minimumStockLevel = minimumStockLevel;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean decreaseStock(int amount) {
        if (amount <= 0) {
            return false;
        }
        if (this.quantity >= amount) {
            this.quantity -= amount;
            return true;
        }
        return false;
    }

    public boolean increaseStock(int amount) {
        if (amount <= 0) {
            return false;
        }
        this.quantity += amount;
        return true;
    }

    public boolean isLowStock() {
        return this.quantity <= this.minimumStockLevel;
    }

    public String orderPart(int amount) {
        if (amount <= 0) {
            return "Invalid order amount";
        }
        return String.format("Ordering %d units of %s (Part Number: %s) from supplier %s",
                amount, this.name, this.partNumber, this.supplier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SparePart sparePart = (SparePart) o;
        return Objects.equals(partId, sparePart.partId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partId);
    }

    @Override
    public String toString() {
        return "SparePart{" +
                "partId=" + partId +
                ", partNumber='" + partNumber + '\'' +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                ", minimumStockLevel=" + minimumStockLevel +
                ", location='" + location + '\'' +
                ", supplier='" + supplier + '\'' +
                ", unitPrice=" + unitPrice +
                ", category='" + category + '\'' +
                '}';
    }
}