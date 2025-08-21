/*
// VehicleService.java
package IMAS.ImasProject.services;

import IMAS.ImasProject.dto.*;
import IMAS.ImasProject.model.*;
import IMAS.ImasProject.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;

    public VehicleResponseDTO createVehicle(VehicleCreateDTO createDTO) {
        log.info("Creating new vehicle with number: {}", createDTO.getVehicleNumber());

        if (vehicleRepository.findByVehicleNumber(createDTO.getVehicleNumber()).isPresent()) {
            throw new IllegalArgumentException("Vehicle with number " + createDTO.getVehicleNumber() + " already exists");
        }

        if (vehicleRepository.findByLicensePlate(createDTO.getLicensePlate()).isPresent()) {
            throw new IllegalArgumentException("Vehicle with license plate " + createDTO.getLicensePlate() + " already exists");
        }

        // Create vehicle manually since builder() method doesn't exist
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber(createDTO.getVehicleNumber());
        vehicle.setLicensePlate(createDTO.getLicensePlate());
        vehicle.setCapacity(createDTO.getCapacity());
        vehicle.setVehicleType(createDTO.getVehicleType());
        vehicle.setAccessible(createDTO.getIsAccessible());
        vehicle.setStatus(createDTO.getStatus());
        vehicle.setManufacturer(createDTO.getManufacturer());
        vehicle.setModel(createDTO.getModel());
        vehicle.setYear(createDTO.getYear());
        vehicle.setFuelCapacity(createDTO.getFuelCapacity());
        vehicle.setFuelType(createDTO.getFuelType());
        vehicle.setHasAirConditioning(createDTO.getHasAirConditioning());
        vehicle.setHasWifi(createDTO.getHasWifi());
        vehicle.setHasGps(createDTO.getHasGps());
        vehicle.setLastMaintenance(createDTO.getLastMaintenance());
        vehicle.setNextMaintenance(createDTO.getNextMaintenance());
        vehicle.setOdometerReading(createDTO.getOdometerReading());

        if (createDTO.getRouteId() != null) {
            Route route = routeRepository.findById(createDTO.getRouteId())
                    .orElseThrow(() -> new EntityNotFoundException("Route not found with id: " + createDTO.getRouteId()));
            vehicle.setRoute(route);
        }

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with ID: {}", savedVehicle.getId());

        return convertToResponseDTO(savedVehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO getVehicleById(Long id) {
        log.info("Retrieving vehicle with ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

        return convertToResponseDTO(vehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponseDTO getVehicleByNumber(String vehicleNumber) {
        log.info("Retrieving vehicle with number: {}", vehicleNumber);

        Vehicle vehicle = vehicleRepository.findByVehicleNumber(vehicleNumber)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with number: " + vehicleNumber));

        return convertToResponseDTO(vehicle);
    }

    public VehicleResponseDTO updateVehicle(Long id, VehicleUpdateDTO updateDTO) {
        log.info("Updating vehicle with ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));

        if (updateDTO.getVehicleNumber() != null &&
                !updateDTO.getVehicleNumber().equals(vehicle.getVehicleNumber())) {
            if (vehicleRepository.findByVehicleNumber(updateDTO.getVehicleNumber()).isPresent()) {
                throw new IllegalArgumentException("Vehicle with number " + updateDTO.getVehicleNumber() + " already exists");
            }
        }

        if (updateDTO.getLicensePlate() != null &&
                !updateDTO.getLicensePlate().equals(vehicle.getLicensePlate())) {
            if (vehicleRepository.findByLicensePlate(updateDTO.getLicensePlate()).isPresent()) {
                throw new IllegalArgumentException("Vehicle with license plate " + updateDTO.getLicensePlate() + " already exists");
            }
        }

        if (updateDTO.getVehicleNumber() != null) vehicle.setVehicleNumber(updateDTO.getVehicleNumber());
        if (updateDTO.getLicensePlate() != null) vehicle.setLicensePlate(updateDTO.getLicensePlate());
        if (updateDTO.getCapacity() != null) vehicle.setCapacity(updateDTO.getCapacity());
        if (updateDTO.getVehicleType() != null) vehicle.setVehicleType(updateDTO.getVehicleType());
        if (updateDTO.getIsAccessible() != null) vehicle.setAccessible(updateDTO.getIsAccessible());
        if (updateDTO.getStatus() != null) vehicle.setStatus(updateDTO.getStatus());
        if (updateDTO.getManufacturer() != null) vehicle.setManufacturer(updateDTO.getManufacturer());
        if (updateDTO.getModel() != null) vehicle.setModel(updateDTO.getModel());
        if (updateDTO.getYear() != null) vehicle.setYear(updateDTO.getYear());
        if (updateDTO.getFuelCapacity() != null) vehicle.setFuelCapacity(updateDTO.getFuelCapacity());
        if (updateDTO.getFuelType() != null) vehicle.setFuelType(updateDTO.getFuelType());
        if (updateDTO.getHasAirConditioning() != null) vehicle.setHasAirConditioning(updateDTO.getHasAirConditioning());
        if (updateDTO.getHasWifi() != null) vehicle.setHasWifi(updateDTO.getHasWifi());
        if (updateDTO.getHasGps() != null) vehicle.setHasGps(updateDTO.getHasGps());
        if (updateDTO.getLastMaintenance() != null) vehicle.setLastMaintenance(updateDTO.getLastMaintenance());
        if (updateDTO.getNextMaintenance() != null) vehicle.setNextMaintenance(updateDTO.getNextMaintenance());
        if (updateDTO.getOdometerReading() != null) vehicle.setOdometerReading(updateDTO.getOdometerReading());
        if (updateDTO.getIsActive() != null) vehicle.setActive(updateDTO.getIsActive());

        if (updateDTO.getRouteId() != null) {
            Long newRouteId = updateDTO.getRouteId();
            if (newRouteId == 0) {
                vehicle.setRoute(null);
            } else {
                Route route = routeRepository.findById(newRouteId)
                        .orElseThrow(() -> new EntityNotFoundException("Route not found with id: " + newRouteId));
                vehicle.setRoute(route);
            }
        }

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        log.info("Vehicle updated successfully with ID: {}", savedVehicle.getId());

        return convertToResponseDTO(savedVehicle);
    }

    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with ID: {}", id);

        if (!vehicleRepository.existsById(id)) {
            throw new EntityNotFoundException("Vehicle not found with id: " + id);
        }

        vehicleRepository.deleteById(id);
        log.info("Vehicle deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public Page<VehicleResponseDTO> getAllVehicles(Pageable pageable) {
        log.info("Retrieving all vehicles with pagination");

        return vehicleRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getVehiclesByStatus(VehicleStatus status) {
        log.info("Retrieving vehicles with status: {}", status);

        return vehicleRepository.findByStatus(status).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getVehiclesByRoute(Long routeId) {
        log.info("Retrieving vehicles for route ID: {}", routeId);

        return vehicleRepository.findByRouteId(routeId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAvailableVehicles() {
        log.info("Retrieving available vehicles");

        return vehicleRepository.findByStatusAndIsActiveTrue(VehicleStatus.ACTIVE).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getVehiclesByType(String vehicleType) {
        log.info("Retrieving vehicles of type: {}", vehicleType);

        return vehicleRepository.findByVehicleType(vehicleType).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getAccessibleVehicles() {
        log.info("Retrieving accessible vehicles");

        return vehicleRepository.findByIsAccessibleTrue().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> getVehiclesRequiringMaintenance() {
        log.info("Retrieving vehicles requiring maintenance");

        return vehicleRepository.findByNextMaintenanceBefore(LocalDateTime.now()).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public VehicleResponseDTO assignVehicleToRoute(Long vehicleId, Long routeId) {
        log.info("Assigning vehicle {} to route {}", vehicleId, routeId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new EntityNotFoundException("Route not found with id: " + routeId));

        // Use setRoute instead of assignToRoute if method doesn't exist
        vehicle.setRoute(route);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return convertToResponseDTO(savedVehicle);
    }

    public VehicleResponseDTO removeVehicleFromRoute(Long vehicleId) {
        log.info("Removing vehicle {} from route", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));

        vehicle.setRoute(null);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return convertToResponseDTO(savedVehicle);
    }

    public VehicleResponseDTO updateVehicleStatus(Long vehicleId, VehicleStatus status) {
        log.info("Updating vehicle {} status to {}", vehicleId, status);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));

        vehicle.setStatus(status);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return convertToResponseDTO(savedVehicle);
    }

    public VehicleResponseDTO performMaintenance(Long vehicleId) {
        log.info("Performing maintenance on vehicle {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));

        // If performMaintenance method doesn't exist, set status manually
        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        vehicle.setLastMaintenance(LocalDateTime.now());
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return convertToResponseDTO(savedVehicle);
    }

    public VehicleResponseDTO completeMaintenance(Long vehicleId) {
        log.info("Completing maintenance on vehicle {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + vehicleId));

        // If completeMaintenance method doesn't exist, set status manually
        vehicle.setStatus(VehicleStatus.ACTIVE);
        vehicle.setLastMaintenance(LocalDateTime.now());
        // Set next maintenance to 3 months from now (adjust as needed)
        vehicle.setNextMaintenance(LocalDateTime.now().plusMonths(3));
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return convertToResponseDTO(savedVehicle);
    }

    @Transactional(readOnly = true)
    public VehicleStatisticsDTO getVehicleStatistics() {
        log.info("Generating vehicle statistics");

        long totalVehicles = vehicleRepository.count();
        long activeVehicles = vehicleRepository.countByStatus(VehicleStatus.ACTIVE);
        long inactiveVehicles = vehicleRepository.countByStatus(VehicleStatus.INACTIVE);
        long inTransitVehicles = vehicleRepository.countByStatus(VehicleStatus.IN_TRANSIT);
        long maintenanceVehicles = vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE);
        long breakdownVehicles = vehicleRepository.countByStatus(VehicleStatus.BREAKDOWN);

        // Create manually since builder() method doesn't exist
        VehicleStatisticsDTO statistics = new VehicleStatisticsDTO();
        statistics.setTotalVehicles(totalVehicles);
        statistics.setActiveVehicles(activeVehicles);
        statistics.setInactiveVehicles(inactiveVehicles);
        statistics.setInTransitVehicles(inTransitVehicles);
        statistics.setMaintenanceVehicles(maintenanceVehicles);
        statistics.setBreakdownVehicles(breakdownVehicles);
        statistics.setUtilizationRate(totalVehicles > 0 ? (double) activeVehicles / totalVehicles * 100 : 0.0);

        return statistics;
    }

    public BulkOperationResultDTO performBulkOperation(BulkVehicleOperationDTO operationDTO) {
        log.info("Performing bulk operation: {}", operationDTO.getOperation());

        List<Vehicle> vehicles = vehicleRepository.findAllById(operationDTO.getVehicleIds());

        if (vehicles.size() != operationDTO.getVehicleIds().size()) {
            throw new EntityNotFoundException("Some vehicles not found");
        }

        int successCount = 0;

        switch (operationDTO.getOperation()) {
            case "UPDATE_STATUS":
                for (Vehicle vehicle : vehicles) {
                    vehicle.setStatus(operationDTO.getNewStatus());
                    successCount++;
                }
                break;
            case "ASSIGN_ROUTE":
                if (operationDTO.getNewRouteId() != null) {
                    Route route = routeRepository.findById(operationDTO.getNewRouteId())
                            .orElseThrow(() -> new EntityNotFoundException("Route not found"));
                    for (Vehicle vehicle : vehicles) {
                        vehicle.setRoute(route);
                        successCount++;
                    }
                }
                break;
            case "REMOVE_ROUTE":
                for (Vehicle vehicle : vehicles) {
                    vehicle.setRoute(null);
                    successCount++;
                }
                break;
            case "ACTIVATE":
                for (Vehicle vehicle : vehicles) {
                    vehicle.setActive(true);
                    vehicle.setStatus(VehicleStatus.ACTIVE);
                    successCount++;
                }
                break;
            case "DEACTIVATE":
                for (Vehicle vehicle : vehicles) {
                    vehicle.setActive(false);
                    vehicle.setStatus(VehicleStatus.INACTIVE);
                    successCount++;
                }
                break;
        }

        vehicleRepository.saveAll(vehicles);

        // Create manually since builder() method doesn't exist
        BulkOperationResultDTO result = new BulkOperationResultDTO();
        result.setTotalRequested(operationDTO.getVehicleIds().size());
        result.setSuccessCount(successCount);
        result.setFailureCount(operationDTO.getVehicleIds().size() - successCount);

        return result;
    }

    private VehicleResponseDTO convertToResponseDTO(Vehicle vehicle) {
        // Create manually since builder() method doesn't exist
        VehicleResponseDTO dto = new VehicleResponseDTO();
        dto.setId(vehicle.getId());
        dto.setVehicleNumber(vehicle.getVehicleNumber());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setCapacity(vehicle.getCapacity());
        dto.setVehicleType(vehicle.getVehicleType());
        dto.setIsAccessible(vehicle.getAccessible());
        dto.setStatus(vehicle.getStatus());
        dto.setManufacturer(vehicle.getManufacturer());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setFuelCapacity(vehicle.getFuelCapacity());
        dto.setFuelType(vehicle.getFuelType());
        dto.setHasAirConditioning(vehicle.getHasAirConditioning());
        dto.setHasWifi(vehicle.getHasWifi());
        dto.setHasGps(vehicle.getHasGps());
        dto.setLastMaintenance(vehicle.getLastMaintenance());
        dto.setNextMaintenance(vehicle.getNextMaintenance());
        dto.setOdometerReading(vehicle.getOdometerReading());
        dto.setIsActive(vehicle.getActive());
        dto.setCreatedAt(vehicle.getCreatedAt());
        dto.setUpdatedAt(vehicle.getUpdatedAt());
        dto.setCreatedBy(vehicle.getCreatedBy());
        dto.setUpdatedBy(vehicle.getUpdatedBy());
        dto.setRouteId(vehicle.getRoute() != null ? vehicle.getRoute().getId() : null);
        dto.setRouteName(vehicle.getRoute() != null ? vehicle.getRoute().getName() : null);
        dto.setCurrentLocation(vehicle.getCurrentLocation() != null ?
                convertToLocationDTO(vehicle.getCurrentLocation()) : null);

        // Set additional calculated fields
        if (vehicle.getCapacity() != null && vehicle.getCurrentLocation() != null &&
                vehicle.getCurrentLocation().getPassengerCount() != null) {
            dto.setOccupancyRate((double) vehicle.getCurrentLocation().getPassengerCount() / vehicle.getCapacity() * 100);
        }

        // Check if operational (not in maintenance or breakdown)
        dto.setIsOperational(vehicle.getStatus() != VehicleStatus.MAINTENANCE &&
                vehicle.getStatus() != VehicleStatus.BREAKDOWN);

        // Check if maintenance is required
        dto.setIsMaintenanceRequired(vehicle.getNextMaintenance() != null &&
                vehicle.getNextMaintenance().isBefore(LocalDateTime.now()));

        // Calculate days since last maintenance
        if (vehicle.getLastMaintenance() != null) {
            dto.setDaysSinceLastMaintenance(
                    java.time.temporal.ChronoUnit.DAYS.between(vehicle.getLastMaintenance(), LocalDateTime.now())
            );
        }

        // Set features list (you may need to implement getFeatures() in Vehicle entity)
        dto.setFeatures(getVehicleFeatures(vehicle));

        return dto;
    }

    private VehicleLocationDTO convertToLocationDTO(VehicleLocation location) {
        // Create manually since builder() method doesn't exist
        VehicleLocationDTO dto = new VehicleLocationDTO();
        dto.setId(location.getId());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setSpeed(location.getSpeed());
        dto.setHeading(location.getHeading());
        dto.setPassengerCount(location.getPassengerCount());
        dto.setTimestamp(location.getTimestamp());
        return dto;
    }

    private List<String> getVehicleFeatures(Vehicle vehicle) {
        List<String> features = new java.util.ArrayList<>();
        if (vehicle.getHasAirConditioning() != null && vehicle.getHasAirConditioning()) {
            features.add("Air Conditioning");
        }
        if (vehicle.getHasWifi() != null && vehicle.getHasWifi()) {
            features.add("WiFi");
        }
        if (vehicle.getHasGps() != null && vehicle.getHasGps()) {
            features.add("GPS");
        }
        if (vehicle.getAccessible() != null && vehicle.getAccessible()) {
            features.add("Wheelchair Accessible");
        }
        return features;
    }
}*/
