package IMAS.ImasProject.controller;


import IMAS.ImasProject.model.Equipment;
import IMAS.ImasProject.model.MaintenanceRecord;
import IMAS.ImasProject.services.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/equipments")
@CrossOrigin(origins = "*") // Allow requests from any origin for testing
public class EquipmentController {

    private final EquipmentService equipmentService;

    @Autowired
    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    // Endpoints CRUD basiques
    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipments() {
        try {
            List<Equipment> equipments = equipmentService.getAllEquipments();
            return ResponseEntity.ok(equipments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        try {
            Optional<Equipment> equipment = equipmentService.getEquipmentById(id);
            return equipment.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Equipment> createEquipment(@RequestBody Equipment equipment) {
        try {
            Equipment savedEquipment = equipmentService.saveEquipment(equipment);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEquipment);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable Long id, @RequestBody Equipment equipment) {
        try {
            Optional<Equipment> existingEquipment = equipmentService.getEquipmentById(id);

            if (existingEquipment.isPresent()) {
                equipment.setEquipmentId(id);
                Equipment updatedEquipment = equipmentService.saveEquipment(equipment);
                return ResponseEntity.ok(updatedEquipment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        try {
            Optional<Equipment> existingEquipment = equipmentService.getEquipmentById(id);

            if (existingEquipment.isPresent()) {
                equipmentService.deleteEquipment(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoints pour les méthodes métier spécifiques
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Equipment.EquipmentStatus status) {
        try {
            Optional<Equipment> equipment = equipmentService.getEquipmentById(id);
            if (equipment.isPresent()) {
                equipmentService.updateStatus(id, status);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/maintenance-history")
    public ResponseEntity<List<MaintenanceRecord>> getMaintenanceHistory(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Optional<Equipment> equipment = equipmentService.getEquipmentById(id);
            if (equipment.isPresent()) {
                List<MaintenanceRecord> history;

                if (startDate != null && endDate != null) {
                    history = equipmentService.getMaintenanceHistory(id, startDate, endDate);
                } else {
                    history = equipmentService.getMaintenanceHistory(id);
                }

                return ResponseEntity.ok(history);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/reliability")
    public ResponseEntity<Double> calculateReliability(@PathVariable Long id) {
        try {
            Optional<Equipment> equipment = equipmentService.getEquipmentById(id);
            if (equipment.isPresent()) {
                double reliability = equipmentService.calculateReliability(id);
                return ResponseEntity.ok(reliability);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoints pour les méthodes de recherche
    @GetMapping("/search")
    public ResponseEntity<List<Equipment>> searchEquipments(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Equipment.EquipmentStatus status) {
        try {
            List<Equipment> equipments = equipmentService.searchEquipments(name, model, location, status);
            return ResponseEntity.ok(equipments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<List<Equipment>> findByName(@PathVariable String name) {
        try {
            List<Equipment> equipments = equipmentService.findByName(name);
            return ResponseEntity.ok(equipments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-model/{model}")
    public ResponseEntity<List<Equipment>> findByModel(@PathVariable String model) {
        try {
            List<Equipment> equipments = equipmentService.findByModel(model);
            return ResponseEntity.ok(equipments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-serial/{serialNumber}")
    public ResponseEntity<List<Equipment>> findBySerialNumber(@PathVariable String serialNumber) {
        try {
            List<Equipment> equipments = equipmentService.findBySerialNumber(serialNumber);
            return ResponseEntity.ok(equipments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-location/{location}")
    public ResponseEntity<List<Equipment>> findByLocation(@PathVariable String location) {
        try {
            List<Equipment> equipments = equipmentService.findByLocation(location);
            return ResponseEntity.ok(equipments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<Equipment>> findByStatus(@PathVariable Equipment.EquipmentStatus status) {
        try {
            List<Equipment> equipments = equipmentService.findByStatus(status);
            return ResponseEntity.ok(equipments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/need-maintenance/{dayThreshold}")
    public ResponseEntity<List<Equipment>> findEquipmentsNeedingMaintenance(@PathVariable int dayThreshold) {
        try {
            List<Equipment> equipments = equipmentService.findEquipmentsNeedingMaintenance(dayThreshold);
            return ResponseEntity.ok(equipments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/maintenance")
    public ResponseEntity<Void> performMaintenance(
            @PathVariable Long id,
            @RequestParam MaintenanceRecord.Priority priority,
            @RequestParam String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam Integer estimatedHours) {
        try {
            Optional<Equipment> optionalEquipment = equipmentService.getEquipmentById(id);
            if (optionalEquipment.isPresent()) {
                equipmentService.performMaintenance(id, priority, description, estimatedHours, startDate, endDate);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Endpoints pour les statistiques
    @GetMapping("/stats/by-location")
    public ResponseEntity<Map<String, Long>> getEquipmentStatsByLocation() {
        try {
            Map<String, Long> stats = equipmentService.getEquipmentStatsByLocation();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats/by-status")
    public ResponseEntity<Map<Equipment.EquipmentStatus, Long>> getEquipmentStatsByStatus() {
        try {
            Map<Equipment.EquipmentStatus, Long> stats = equipmentService.getEquipmentStatsByStatus();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}