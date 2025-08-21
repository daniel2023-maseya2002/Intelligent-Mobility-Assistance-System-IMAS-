package IMAS.ImasProject.services;

import IMAS.ImasProject.model.MaintenanceRecord;
import IMAS.ImasProject.model.Equipment;
import IMAS.ImasProject.repository.MaintenanceRecordRepository;
import IMAS.ImasProject.repository.EquipmentRepository;
import IMAS.ImasProject.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MaintenanceRecordService {

    @Autowired
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    // Créer un nouvel enregistrement de maintenance
    public MaintenanceRecord createMaintenanceRecord(MaintenanceRecord maintenanceRecord) {
        validateMaintenanceRecord(maintenanceRecord);

        // Vérifier que l'équipement existe
        if (maintenanceRecord.getEquipment() != null && maintenanceRecord.getEquipment().getEquipmentId() != null) {
            Equipment equipment = equipmentRepository.findById(maintenanceRecord.getEquipment().getEquipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Equipment not found with id: " + maintenanceRecord.getEquipment().getEquipmentId()));
            maintenanceRecord.setEquipment(equipment);
        }

        // Sauvegarder le maintenance record
        MaintenanceRecord savedRecord = maintenanceRecordRepository.save(maintenanceRecord);

        // NOUVEAU : Mettre à jour la date de dernière maintenance de l'équipement si endDate existe
        if (savedRecord.getEndDate() != null && savedRecord.getEquipment() != null) {
            updateEquipmentLastMaintenanceDate(savedRecord.getEquipment().getEquipmentId(), savedRecord.getEndDate());
        }

        return savedRecord;
    }

    // Obtenir tous les enregistrements de maintenance
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getAllMaintenanceRecords() {
        return maintenanceRecordRepository.findAll();
    }

    // Obtenir un enregistrement par ID
    @Transactional(readOnly = true)
    public MaintenanceRecord getMaintenanceRecordById(Long id) {
        return maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord not found with id: " + id));
    }






    public MaintenanceRecord updateMaintenanceRecord(Long id, MaintenanceRecord maintenanceRecordDetails) {
        MaintenanceRecord existingRecord = getMaintenanceRecordById(id);

        validateMaintenanceRecord(maintenanceRecordDetails);

        // Mettre à jour les champs
        existingRecord.setStartDate(maintenanceRecordDetails.getStartDate());
        existingRecord.setEndDate(maintenanceRecordDetails.getEndDate());
        existingRecord.setEstimatedHours(maintenanceRecordDetails.getEstimatedHours());
        existingRecord.setPriority(maintenanceRecordDetails.getPriority());
        existingRecord.setDescription(maintenanceRecordDetails.getDescription());

        // Mettre à jour l'équipement si fourni
        if (maintenanceRecordDetails.getEquipment() != null &&
                maintenanceRecordDetails.getEquipment().getEquipmentId() != null) {
            Equipment equipment = equipmentRepository.findById(maintenanceRecordDetails.getEquipment().getEquipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Equipment not found with id: " + maintenanceRecordDetails.getEquipment().getEquipmentId()));
            existingRecord.setEquipment(equipment);
        }

        MaintenanceRecord updatedRecord = maintenanceRecordRepository.save(existingRecord);

        // NOUVEAU : Mettre à jour la date de dernière maintenance de l'équipement si endDate existe
        if (updatedRecord.getEndDate() != null && updatedRecord.getEquipment() != null) {
            updateEquipmentLastMaintenanceDate(updatedRecord.getEquipment().getEquipmentId(), updatedRecord.getEndDate());
        }

        return updatedRecord;
    }






    // Supprimer un enregistrement de maintenance
    public void deleteMaintenanceRecord(Long id) {
        MaintenanceRecord existingRecord = getMaintenanceRecordById(id);
        maintenanceRecordRepository.delete(existingRecord);
    }

    // Rechercher par équipement
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getMaintenanceRecordsByEquipmentId(Long equipmentId) {
        return maintenanceRecordRepository.findByEquipmentEquipmentId(equipmentId);
    }

    // Rechercher par priorité
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getMaintenanceRecordsByPriority(MaintenanceRecord.Priority priority) {
        return maintenanceRecordRepository.findByPriority(priority);
    }

    // Rechercher par plage de dates
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getMaintenanceRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        return maintenanceRecordRepository.findByStartDateBetween(startDate, endDate);
    }

    // Rechercher par heures estimées
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getMaintenanceRecordsByMaxHours(Integer maxHours) {
        return maintenanceRecordRepository.findByEstimatedHoursLessThanEqual(maxHours);
    }

    // Rechercher par mot-clé dans la description
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getMaintenanceRecordsByKeyword(String keyword) {
        return maintenanceRecordRepository.findByDescriptionContainingIgnoreCase(keyword);
    }

    // Obtenir les maintenances en cours
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getOngoingMaintenanceRecords() {
        return maintenanceRecordRepository.findOngoingMaintenance();
    }

    // Obtenir les maintenances terminées
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getCompletedMaintenanceRecords() {
        return maintenanceRecordRepository.findCompletedMaintenance();
    }

    // Obtenir les maintenances prévues
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getScheduledMaintenanceRecords() {
        return maintenanceRecordRepository.findScheduledMaintenance();
    }

    // Recherche avancée
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> searchMaintenanceRecords(
            Long equipmentId,
            MaintenanceRecord.Priority priority,
            LocalDate startDate,
            LocalDate endDate,
            Integer maxHours,
            String keyword) {
        return maintenanceRecordRepository.searchMaintenanceRecords(
                equipmentId, priority, startDate, endDate, maxHours, keyword);
    }

    // Compter les maintenances par priorité
    @Transactional(readOnly = true)
    public Long countMaintenanceRecordsByPriority(MaintenanceRecord.Priority priority) {
        return maintenanceRecordRepository.countByPriority(priority);
    }

    // Obtenir les maintenances les plus récentes
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getRecentMaintenanceRecords() {
        return maintenanceRecordRepository.findAllOrderByStartDateDesc();
    }

    // Marquer une maintenance comme terminée
    public MaintenanceRecord completeMaintenanceRecord(Long id) {
        MaintenanceRecord record = getMaintenanceRecordById(id);
        record.setEndDate(LocalDate.now());
        MaintenanceRecord completedRecord = maintenanceRecordRepository.save(record);

        // NOUVEAU : Mettre à jour la date de dernière maintenance de l'équipement
        if (completedRecord.getEquipment() != null) {
            updateEquipmentLastMaintenanceDate(completedRecord.getEquipment().getEquipmentId(), completedRecord.getEndDate());
        }

        return completedRecord;
    }




    // Obtenir les maintenances par équipement et plage de dates
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getMaintenanceRecordsByEquipmentAndDateRange(
            Long equipmentId, LocalDate startDate, LocalDate endDate) {
        return maintenanceRecordRepository.findByEquipmentAndDateRange(equipmentId, startDate, endDate);
    }

    // Validation des données d'entrée
    private void validateMaintenanceRecord(MaintenanceRecord maintenanceRecord) {
        if (maintenanceRecord == null) {
            throw new IllegalArgumentException("MaintenanceRecord cannot be null");
        }

        if (maintenanceRecord.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }

        if (maintenanceRecord.getEndDate() != null &&
                maintenanceRecord.getStartDate().isAfter(maintenanceRecord.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        if (maintenanceRecord.getEstimatedHours() != null && maintenanceRecord.getEstimatedHours() <= 0) {
            throw new IllegalArgumentException("Estimated hours must be positive");
        }

        if (maintenanceRecord.getPriority() == null) {
            throw new IllegalArgumentException("Priority is required");
        }
    }

    private void updateEquipmentLastMaintenanceDate(Long equipmentId, LocalDate maintenanceDate) {
        Equipment equipment = equipmentRepository.findById(equipmentId).orElse(null);
        if (equipment != null) {
            // Mettre à jour seulement si cette date est plus récente que la date actuelle
            if (equipment.getLastMaintenanceDate() == null ||
                    maintenanceDate.isAfter(equipment.getLastMaintenanceDate())) {
                equipment.setLastMaintenanceDate(maintenanceDate);
                equipmentRepository.save(equipment);
            }
        }
    }
}