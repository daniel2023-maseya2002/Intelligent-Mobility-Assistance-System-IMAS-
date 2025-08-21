package IMAS.ImasProject.services;


import IMAS.ImasProject.model.Equipment;
import IMAS.ImasProject.model.Inventory;
import IMAS.ImasProject.model.MaintenanceRecord;
import IMAS.ImasProject.repository.EquipmentRepository;
import IMAS.ImasProject.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public EquipmentService(EquipmentRepository equipmentRepository,
                            InventoryRepository inventoryRepository) {
        this.equipmentRepository = equipmentRepository;
        this.inventoryRepository = inventoryRepository;
    }

    // Méthodes CRUD de base
    public List<Equipment> getAllEquipments() {
        return equipmentRepository.findAll();
    }

    public Optional<Equipment> getEquipmentById(Long id) {
        return equipmentRepository.findById(id);
    }

    @Transactional
    public Equipment saveEquipment(Equipment equipment) {
        // Vérifier si l'équipement est associé à un inventaire
        if (equipment.getInventoryId() != null) {
            Optional<Inventory> inventoryOpt = inventoryRepository.findById(equipment.getInventoryId());

            if (inventoryOpt.isPresent()) {
                Inventory inventory = inventoryOpt.get();

                // Vérifier que le stock est disponible
                if (inventory.getAvailableQuantity() <= 0) {
                    throw new IllegalStateException("Cannot add equipment - no available stock in inventory");
                }

                // Décrémenter le stock
                inventory.setAvailableQuantity(inventory.getAvailableQuantity() - 1);
                inventory.setTotalQuantity(inventory.getTotalQuantity() - 1);
                inventoryRepository.save(inventory);
            } else {
                throw new IllegalArgumentException("Inventory not found with id: " + equipment.getInventoryId());
            }
        }

        return equipmentRepository.save(equipment);
    }

    @Transactional
    public void deleteEquipment(Long id) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findById(id);

        if (equipmentOpt.isPresent()) {
            Equipment equipment = equipmentOpt.get();

            // Si l'équipement est associé à un inventaire, réapprovisionner le stock
            if (equipment.getInventoryId() != null) {
                Optional<Inventory> inventoryOpt = inventoryRepository.findById(equipment.getInventoryId());

                if (inventoryOpt.isPresent()) {
                    Inventory inventory = inventoryOpt.get();
                    inventory.setAvailableQuantity(inventory.getAvailableQuantity() + 1);
                    inventory.setTotalQuantity(inventory.getTotalQuantity() + 1);
                    inventoryRepository.save(inventory);
                }
            }

            equipmentRepository.deleteById(id);
        }
    }

    // Méthodes métier spécifiques
    @Transactional
    public void updateStatus(Long equipmentId, Equipment.EquipmentStatus newStatus) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findById(equipmentId);
        equipmentOpt.ifPresent(equipment -> {
            equipment.updateStatus(newStatus);
            equipmentRepository.save(equipment);
        });
    }

    public List<MaintenanceRecord> getMaintenanceHistory(Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .map(Equipment::getMaintenanceHistory)
                .orElse(List.of());
    }

    public List<MaintenanceRecord> getMaintenanceHistory(Long equipmentId, LocalDate startDate, LocalDate endDate) {
        return equipmentRepository.findById(equipmentId)
                .map(equipment -> equipment.getMaintenanceHistory(startDate, endDate))
                .orElse(List.of());
    }

    public double calculateReliability(Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .map(Equipment::calculateReliability)
                .orElse(0.0);
    }

    // Méthodes de recherche
    public List<Equipment> findByName(String name) {
        return equipmentRepository.findByName(name);
    }

    public List<Equipment> findByModel(String model) {
        return equipmentRepository.findByModel(model);
    }

    public List<Equipment> findBySerialNumber(String serialNumber) {
        return equipmentRepository.findBySerialNumber(serialNumber);
    }

    public List<Equipment> findByLocation(String location) {
        return equipmentRepository.findByLocation(location);
    }

    public List<Equipment> findByStatus(Equipment.EquipmentStatus status) {
        return equipmentRepository.findByStatus(status);
    }

    public List<Equipment> searchEquipments(String name, String model, String location, Equipment.EquipmentStatus status) {
        return equipmentRepository.searchEquipments(name, model, location, status);
    }

    public List<Equipment> findEquipmentsNeedingMaintenance(int dayThreshold) {
        LocalDate thresholdDate = LocalDate.now().minusDays(dayThreshold);
        return equipmentRepository.findEquipmentsNeedingMaintenance(thresholdDate);
    }

    public List<Equipment> findOldEquipments(int yearThreshold) {
        LocalDate thresholdDate = LocalDate.now().minusYears(yearThreshold);
        return equipmentRepository.findOldEquipmentsStillInService(thresholdDate);
    }

    @Transactional
    public void performMaintenance(Long equipmentId, MaintenanceRecord.Priority priority,
                                   String description, Integer estimatedHours,
                                   LocalDate startDate, LocalDate endDate) {
        equipmentRepository.findById(equipmentId).ifPresent(equipment -> {
            MaintenanceRecord record = new MaintenanceRecord(
                    equipment, startDate, endDate, estimatedHours, priority, description
            );

            equipment.addMaintenanceRecord(record);
            equipment.updateStatus(Equipment.EquipmentStatus.OPERATIONAL);
            equipmentRepository.save(equipment);
        });
    }

    public Map<String, Long> getEquipmentStatsByLocation() {
        List<Equipment> allEquipments = equipmentRepository.findAll();
        return allEquipments.stream()
                .collect(Collectors.groupingBy(
                        Equipment::getLocation,
                        Collectors.counting()
                ));
    }

    public Map<Equipment.EquipmentStatus, Long> getEquipmentStatsByStatus() {
        List<Equipment> allEquipments = equipmentRepository.findAll();
        return allEquipments.stream()
                .collect(Collectors.groupingBy(
                        Equipment::getStatus,
                        Collectors.counting()
                ));
    }
}