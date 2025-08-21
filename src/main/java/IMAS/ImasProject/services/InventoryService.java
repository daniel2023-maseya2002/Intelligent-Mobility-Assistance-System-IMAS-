// ========== INVENTORY SERVICE ==========
package IMAS.ImasProject.services;


import IMAS.ImasProject.model.Equipment;
import IMAS.ImasProject.model.Inventory;
import IMAS.ImasProject.model.InventoryAlert;
import IMAS.ImasProject.model.SparePart;
import IMAS.ImasProject.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    // Create inventory item from equipment
    public Inventory createInventoryFromEquipment(Equipment equipment) {
        Inventory inventory = new Inventory();
        inventory.setInventoryCode("EQ-" + equipment.getEquipmentId());
        inventory.setName(equipment.getName());
        inventory.setDescription(equipment.getModel() + " - " + equipment.getSerialNumber());
        inventory.setItemType(Inventory.ItemType.EQUIPMENT);
        inventory.setStatus(mapEquipmentStatusToInventoryStatus(equipment.getStatus()));
        inventory.setTotalQuantity(1);
        inventory.setAvailableQuantity(equipment.getStatus() == Equipment.EquipmentStatus.OPERATIONAL ? 1 : 0);
        inventory.setReservedQuantity(0);
        inventory.setLocation(equipment.getLocation());
        inventory.setEquipment(equipment);

        return inventoryRepository.save(inventory);
    }

    // Create inventory item from spare part
    public Inventory createInventoryFromSparePart(SparePart sparePart) {
        Inventory inventory = new Inventory();
        inventory.setInventoryCode("SP-" + sparePart.getPartId());
        inventory.setName(sparePart.getName());
        inventory.setDescription(sparePart.getDescription());
        inventory.setItemType(Inventory.ItemType.SPARE_PART);
        inventory.setStatus(Inventory.ItemStatus.ACTIVE);
        inventory.setTotalQuantity(sparePart.getQuantity());
        inventory.setAvailableQuantity(sparePart.getQuantity());
        inventory.setReservedQuantity(0);
        inventory.setLocation(sparePart.getLocation());
        inventory.setMinimumThreshold(sparePart.getMinimumStockLevel());
        inventory.setSparePart(sparePart);

        return inventoryRepository.save(inventory);
    }

    // Synchronize inventory with spare part changes
    public void syncInventoryWithSparePart(SparePart sparePart) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findBySparePartId(sparePart.getPartId());
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            inventory.setTotalQuantity(sparePart.getQuantity());
            inventory.setAvailableQuantity(sparePart.getQuantity() - inventory.getReservedQuantity());
            inventory.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inventory);

            // Check for alerts
            checkAndCreateAlerts(inventory);
        }
    }

    // Synchronize inventory with equipment changes
    public void syncInventoryWithEquipment(Equipment equipment) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByEquipmentId(equipment.getEquipmentId());
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            inventory.setStatus(mapEquipmentStatusToInventoryStatus(equipment.getStatus()));
            inventory.setAvailableQuantity(equipment.getStatus() == Equipment.EquipmentStatus.OPERATIONAL ? 1 : 0);
            inventory.setLocation(equipment.getLocation());
            inventory.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inventory);
        }
    }

    // Stock management methods
    public boolean addStock(Long inventoryId, int quantity, String reason, String performedBy) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findById(inventoryId);
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            boolean success = inventory.addStock(quantity, reason, performedBy);
            if (success) {
                inventoryRepository.save(inventory);
                syncWithRelatedEntities(inventory);
                checkAndCreateAlerts(inventory);
            }
            return success;
        }
        return false;
    }

    public boolean removeStock(Long inventoryId, int quantity, String reason, String performedBy) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findById(inventoryId);
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            boolean success = inventory.removeStock(quantity, reason, performedBy);
            if (success) {
                inventoryRepository.save(inventory);
                syncWithRelatedEntities(inventory);
                checkAndCreateAlerts(inventory);
            }
            return success;
        }
        return false;
    }

    public boolean reserveStock(Long inventoryId, int quantity, String reason, String performedBy) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findById(inventoryId);
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            boolean success = inventory.reserveStock(quantity, reason, performedBy);
            if (success) {
                inventoryRepository.save(inventory);
                checkAndCreateAlerts(inventory);
            }
            return success;
        }
        return false;
    }

    // Alert management
    private void checkAndCreateAlerts(Inventory inventory) {
        // Check for low stock
        if (inventory.isLowStock()) {
            InventoryAlert alert = new InventoryAlert(inventory, InventoryAlert.AlertType.LOW_STOCK,
                    "Stock level is below minimum threshold: " + inventory.getTotalQuantity() +
                            " <= " + inventory.getMinimumThreshold());
            inventory.getAlerts().add(alert);
        }

        // Check for overstock
        if (inventory.isOverStock()) {
            InventoryAlert alert = new InventoryAlert(inventory, InventoryAlert.AlertType.OVERSTOCK,
                    "Stock level exceeds maximum threshold: " + inventory.getTotalQuantity() +
                            " >= " + inventory.getMaximumThreshold());
            inventory.getAlerts().add(alert);
        }
    }

    // Sync with related entities
    private void syncWithRelatedEntities(Inventory inventory) {
        if (inventory.getSparePart() != null) {
            SparePart sparePart = inventory.getSparePart();
            sparePart.setQuantity(inventory.getTotalQuantity());
        }
    }

    // Utility methods
    private Inventory.ItemStatus mapEquipmentStatusToInventoryStatus(Equipment.EquipmentStatus equipmentStatus) {
        switch (equipmentStatus) {
            case OPERATIONAL:
                return Inventory.ItemStatus.ACTIVE;
            case UNDER_MAINTENANCE:
                return Inventory.ItemStatus.UNDER_REVIEW;
            case DEFECTIVE:
                return Inventory.ItemStatus.INACTIVE;
            case RETIRED:
                return Inventory.ItemStatus.OBSOLETE;
            default:
                return Inventory.ItemStatus.INACTIVE;
        }
    }

    // CRUD operations
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Optional<Inventory> getInventoryById(Long id) {
        return inventoryRepository.findById(id);
    }

    public Optional<Inventory> getInventoryByCode(String code) {
        return inventoryRepository.findByInventoryCode(code);
    }

    public List<Inventory> getInventoryByType(Inventory.ItemType type) {
        return inventoryRepository.findByItemType(type);
    }

    public List<Inventory> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public List<Inventory> getOverStockItems() {
        return inventoryRepository.findOverStockItems();
    }

    public List<Inventory> searchInventory(String keyword) {
        return inventoryRepository.searchByKeyword(keyword);
    }

    public Double getTotalInventoryValue() {
        return inventoryRepository.getTotalInventoryValue();
    }

    public Inventory saveInventory(Inventory inventory) {
        inventory.setLastUpdated(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }

    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }
}