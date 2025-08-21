/*
package IMAS.ImasProject.services;

import IMAS.ImasProject.model.Vehicle;
import IMAS.ImasProject.model.VehicleLocation;
import IMAS.ImasProject.repository.VehicleRepository;
import IMAS.ImasProject.repository.VehicleLocationRepository;
import IMAS.ImasProject.dto.*;
import IMAS.ImasProject.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GPSTrackingService {

    private static final Logger log = LoggerFactory.getLogger(GPSTrackingService.class);

    private final VehicleRepository vehicleRepository;
    private final VehicleLocationRepository vehicleLocationRepository;

    // Constructor explicite pour l'injection de dépendance
    @Autowired
    public GPSTrackingService(VehicleRepository vehicleRepository,
                              VehicleLocationRepository vehicleLocationRepository) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleLocationRepository = vehicleLocationRepository;
    }

    */
/**
     * Update vehicle location (GPS tracking)
     *//*

    public VehicleLocationDTO updateVehicleLocation(VehicleLocationCreateDTO locationDTO) {
        log.info("Updating location for vehicle ID: {}", locationDTO.getVehicleId());

        Vehicle vehicle = vehicleRepository.findById(locationDTO.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + locationDTO.getVehicleId()));

        // Create VehicleLocation without using builder() since it's not available
        VehicleLocation location = new VehicleLocation();
        location.setVehicle(vehicle);
        location.setLatitude(locationDTO.getLatitude());
        location.setLongitude(locationDTO.getLongitude());
        location.setSpeed(locationDTO.getSpeed());
        location.setHeading(locationDTO.getHeading());
        location.setPassengerCount(locationDTO.getPassengerCount());
        location.setTimestamp(locationDTO.getTimestamp() != null ? locationDTO.getTimestamp() : LocalDateTime.now());
        location.setAccuracy(locationDTO.getAccuracy());
        location.setAltitude(locationDTO.getAltitude());

        VehicleLocation savedLocation = vehicleLocationRepository.save(location);
        log.info("Location updated for vehicle ID: {} at coordinates: {}, {}",
                locationDTO.getVehicleId(), locationDTO.getLatitude(), locationDTO.getLongitude());

        return convertToLocationDTO(savedLocation);
    }

    */
/**
     * Get current location of a vehicle
     *//*

    @Transactional(readOnly = true)
    public VehicleLocationDTO getCurrentVehicleLocation(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        VehicleLocation currentLocation = vehicle.getCurrentLocation();
        if (currentLocation == null) {
            throw new ResourceNotFoundException("No location data found for vehicle ID: " + vehicleId);
        }

        return convertToLocationDTO(currentLocation);
    }

    */
/**
     * Get vehicle location history
     *//*

    @Transactional(readOnly = true)
    public List<VehicleLocationDTO> getVehicleLocationHistory(Long vehicleId, int limit) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        List<VehicleLocation> recentLocations = vehicle.getRecentLocations(limit);
        return recentLocations.stream()
                .map(this::convertToLocationDTO)
                .collect(Collectors.toList());
    }

    */
/**
     * Get vehicle location history within a time range
     *//*

    @Transactional(readOnly = true)
    public List<VehicleLocationDTO> getVehicleLocationHistory(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        List<VehicleLocation> locations = vehicleLocationRepository
                .findByVehicleIdAndTimestampBetween(vehicleId, startTime, endTime);

        return locations.stream()
                .map(this::convertToLocationDTO)
                .collect(Collectors.toList());
    }

    */
/**
     * Get all vehicles with their current GPS positions
     *//*

    @Transactional(readOnly = true)
    public List<VehicleGPSTrackingDTO> getAllVehiclesGPSTracking() {
        List<Vehicle> vehicles = vehicleRepository.findByHasGpsTrue();

        return vehicles.stream()
                .map(this::convertToGPSTrackingDTO)
                .collect(Collectors.toList());
    }

    */
/**
     * Get vehicles with recent location updates
     *//*

    @Transactional(readOnly = true)
    public List<VehicleGPSTrackingDTO> getVehiclesWithRecentLocations(int minutesAgo) {
        LocalDateTime sinceTime = LocalDateTime.now().minusMinutes(minutesAgo);
        List<Vehicle> vehicles = vehicleRepository.findVehiclesWithRecentLocations(sinceTime);

        return vehicles.stream()
                .map(this::convertToGPSTrackingDTO)
                .collect(Collectors.toList());
    }

    */
/**
     * Get currently moving vehicles
     *//*

    @Transactional(readOnly = true)
    public List<VehicleGPSTrackingDTO> getMovingVehicles() {
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(5); // Consider last 5 minutes
        List<Vehicle> movingVehicles = vehicleRepository.findMovingVehicles(recentTime);

        return movingVehicles.stream()
                .map(this::convertToGPSTrackingDTO)
                .collect(Collectors.toList());
    }

    */
/**
     * Find vehicles within a specific radius of a location
     *//*

    @Transactional(readOnly = true)
    public List<VehicleGPSTrackingDTO> findVehiclesInRadius(Double latitude, Double longitude, Double radiusKm) {
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(30); // Consider last 30 minutes
        List<Vehicle> vehiclesInRadius = vehicleRepository.findVehiclesInRadius(latitude, longitude, radiusKm, recentTime);

        return vehiclesInRadius.stream()
                .map(this::convertToGPSTrackingDTO)
                .collect(Collectors.toList());
    }

    */
/**
     * Get vehicles within coordinate bounds (for map view)
     *//*

    @Transactional(readOnly = true)
    public List<VehicleGPSTrackingDTO> getVehiclesInBounds(Double minLatitude, Double maxLatitude,
                                                           Double minLongitude, Double maxLongitude) {
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(30);
        List<VehicleLocation> locationsInBounds = vehicleLocationRepository
                .findLocationsInBounds(minLatitude, maxLatitude, minLongitude, maxLongitude, recentTime);

        return locationsInBounds.stream()
                .map(location -> convertToGPSTrackingDTO(location.getVehicle()))
                .distinct()
                .collect(Collectors.toList());
    }

    */
/**
     * Get real-time tracking data for a specific vehicle
     *//*

    @Transactional(readOnly = true)
    public VehicleGPSTrackingDTO getVehicleGPSTracking(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + vehicleId));

        return convertToGPSTrackingDTO(vehicle);
    }

    */
/**
     * Calculate distance traveled by vehicle in a time period
     *//*

    @Transactional(readOnly = true)
    public Double calculateDistanceTraveled(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        List<VehicleLocation> route = vehicleLocationRepository
                .getVehicleRouteInTimeRange(vehicleId, startTime, endTime);

        if (route.size() < 2) {
            return 0.0;
        }

        double totalDistance = 0.0;
        for (int i = 1; i < route.size(); i++) {
            VehicleLocation previous = route.get(i - 1);
            VehicleLocation current = route.get(i);
            totalDistance += previous.distanceTo(current);
        }

        return totalDistance;
    }

    */
/**
     * Get average speed for a vehicle in a time period
     *//*

    @Transactional(readOnly = true)
    public Double getAverageSpeed(Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {
        return vehicleLocationRepository.getAverageSpeedByVehicleIdAndTimeRange(vehicleId, startTime, endTime);
    }

    */
/**
     * Clean up old location data
     *//*

    public void cleanupOldLocationData(int daysToKeep) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        vehicleLocationRepository.deleteByTimestampBefore(cutoffTime);
        log.info("Cleaned up location data older than {} days", daysToKeep);
    }

    */
/**
     * Batch update vehicle locations
     *//*

    public List<VehicleLocationDTO> batchUpdateVehicleLocations(List<VehicleLocationCreateDTO> locationUpdates) {
        log.info("Processing batch location update for {} vehicles", locationUpdates.size());

        List<VehicleLocation> locations = locationUpdates.stream()
                .map(dto -> {
                    Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with ID: " + dto.getVehicleId()));

                    // Create VehicleLocation without using builder()
                    VehicleLocation location = new VehicleLocation();
                    location.setVehicle(vehicle);
                    location.setLatitude(dto.getLatitude());
                    location.setLongitude(dto.getLongitude());
                    location.setSpeed(dto.getSpeed());
                    location.setHeading(dto.getHeading());
                    location.setPassengerCount(dto.getPassengerCount());
                    location.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now());
                    location.setAccuracy(dto.getAccuracy());
                    location.setAltitude(dto.getAltitude());

                    return location;
                })
                .collect(Collectors.toList());

        List<VehicleLocation> savedLocations = vehicleLocationRepository.saveAll(locations);

        return savedLocations.stream()
                .map(this::convertToLocationDTO)
                .collect(Collectors.toList());
    }

    */
/**
     * Convert VehicleLocation to VehicleLocationDTO
     *//*

    private VehicleLocationDTO convertToLocationDTO(VehicleLocation location) {
        // Create VehicleLocationDTO without using builder()
        VehicleLocationDTO dto = new VehicleLocationDTO();
        dto.setId(location.getId());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setSpeed(location.getSpeed());
        dto.setHeading(location.getHeading());
        dto.setPassengerCount(location.getPassengerCount());
        dto.setTimestamp(location.getTimestamp());
        dto.setAccuracy(location.getAccuracy());
        dto.setAltitude(location.getAltitude());
        dto.setVehicleId(location.getVehicle().getId());

        return dto;
    }

    */
/**
     * Convert Vehicle to VehicleGPSTrackingDTO
     *//*

    private VehicleGPSTrackingDTO convertToGPSTrackingDTO(Vehicle vehicle) {
        VehicleLocation currentLocation = vehicle.getCurrentLocation();
        VehicleLocationDTO currentLocationDTO = null;

        if (currentLocation != null) {
            currentLocationDTO = convertToLocationDTO(currentLocation);
        }

        // Create VehicleGPSTrackingDTO without using builder()
        VehicleGPSTrackingDTO dto = new VehicleGPSTrackingDTO();
        dto.setVehicleId(vehicle.getId());
        dto.setVehicleNumber(vehicle.getVehicleNumber());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setStatus(vehicle.getStatus());
        dto.setCurrentLocation(currentLocationDTO);
        dto.setCurrentSpeed(vehicle.getCurrentSpeed());
        dto.setCurrentPassengerCount(vehicle.getCurrentPassengerCount());
        dto.setOccupancyRate(vehicle.getOccupancyRate());
        dto.setIsMoving(vehicle.isMoving());
        dto.setLastLocationUpdate(currentLocation != null ? currentLocation.getTimestamp() : null);
        dto.setRouteName(vehicle.getRoute() != null ? vehicle.getRoute().getName() : null);
        dto.setRouteId(vehicle.getRoute() != null ? vehicle.getRoute().getId() : null);

        return dto;
    }
}*/
