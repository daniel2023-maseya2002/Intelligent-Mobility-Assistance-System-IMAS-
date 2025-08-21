package IMAS.ImasProject.services;

import IMAS.ImasProject.model.Bus;
import IMAS.ImasProject.dto.BusReport;
import IMAS.ImasProject.repository.BusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Transactional
public class BusService {

    @Autowired
    private BusRepository busRepository;

    @Autowired
    private NotificationService notificationService;

    public Bus save(Bus bus) {
        Bus savedBus = busRepository.save(bus);

        // FIX: Correction de l'erreur de syntaxe dans le message
        String message = "A new bus '" + savedBus.getName() + "' has been created with ID: " + savedBus.getId();
        String notificationType = "INFO";

        notificationService.createNotification(message, notificationType);

        if (savedBus.getDriver() != null && savedBus.getDriver().getId() != null) {
            notificationService.createNotificationForDriver(
                    "You have been assigned to a new bus: " + savedBus.getName(),
                    "INFO",
                    savedBus.getDriver().getId()
            );
        }
        return savedBus;
    }

    /**
     * Find all buses
     */
    public List<Bus> findAll() {
        return busRepository.findAll();
    }

    /**
     * Find bus by ID
     */
    public Bus findById(Long id) {
        return busRepository.findById(id).orElse(null);
    }

    /**
     * Find bus by ID with Optional wrapper
     */
    public Optional<Bus> findByIdOptional(Long id) {
        return busRepository.findById(id);
    }

    /**
     * Delete bus by ID
     */
    public void deleteById(Long id) {
        busRepository.deleteById(id);
    }

    /**
     * Find bus assigned to a specific driver
     */
    public Bus findByDriverId(Long driverId) {
        return busRepository.findByDriverId(driverId);
    }

    /**
     * Find bus by driver ID with Optional wrapper
     */
    public Optional<Bus> findByDriverIdOptional(Long driverId) {
        return Optional.ofNullable(busRepository.findByDriverId(driverId));
    }

    /**
     * Find all buses with driver and route information loaded
     */
    public List<Bus> findAllWithDriverAndRoute() {
        return busRepository.findAllWithDriverAndRoute();
    }

    /**
     * Find bus by ID with driver and route information loaded
     */
    public Bus findByIdWithDriverAndRoute(Long id) {
        return busRepository.findByIdWithDriverAndRoute(id);
    }

    /**
     * Check if a driver is assigned to any bus
     */
    public boolean isDriverAssignedToBus(Long driverId) {
        return busRepository.findByDriverId(driverId) != null;
    }

    /**
     * Find buses by route ID
     */
    public List<Bus> findByRouteId(Long routeId) {
        return busRepository.findByRouteId(routeId);
    }

    /**
     * Find active (running) buses
     */
    public List<Bus> findActiveBuses() {
        return busRepository.findByIsStoppedFalse();
    }

    /**
     * Find buses with accidents
     */
    public List<Bus> findBusesWithAccidents() {
        return busRepository.findByHasAccidentTrue();
    }



    @Transactional
    public Bus startBus(Long busId) {
        System.out.println("BusService.startBus() called with ID: " + busId);

        Bus bus = findById(busId);
        if (bus == null) {
            System.err.println("Bus not found in service with ID: " + busId);
            throw new RuntimeException("Bus not found with ID: " + busId);
        }

        System.out.println("Bus details - Name: " + bus.getName() +
                ", Stopped: " + bus.getStopped() +
                ", HasAccident: " + bus.getHasAccident() +
                ", Progress: " + bus.getProgress() +
                ", Driver: " + (bus.getDriver() != null ? bus.getDriver().getId() : "null") +
                ", Route: " + (bus.getRoute() != null ? bus.getRoute().getId() : "null"));

        if (bus.getHasAccident() != null && bus.getHasAccident()) {
            throw new RuntimeException("Cannot start a bus that has an accident");
        }

        // Vérification améliorée pour éviter les NullPointerException
        Boolean isStopped = bus.getStopped();
        Double progress = bus.getProgress();

        if (isStopped != null && !isStopped && progress != null && progress > 0) {
            throw new RuntimeException("Bus is already running");
        }

        if (bus.getDriver() == null) {
            throw new RuntimeException("Cannot start bus without a driver assigned");
        }

        if (bus.getRoute() == null) {
            throw new RuntimeException("Cannot start bus without a route assigned");
        }

        try {
            // Reset bus status for journey
            bus.setStopped(false);
            bus.setProgress(0.0);
            bus.setCurrentLat(bus.getStartLat());
            bus.setCurrentLng(bus.getStartLng());
            bus.setHasAccident(false);
            bus.setDepartureTime(LocalDateTime.now());

            // Calculate estimated arrival time if not set
            if (bus.getArrivalTime() == null && bus.getDepartureTime() != null) {
                bus.setArrivalTime(calculateEstimatedArrivalTime(bus));
            }

            System.out.println("About to save bus...");
            Bus savedBus = save(bus);
            System.out.println("Bus saved successfully");
            return savedBus;

        } catch (Exception e) {
            System.err.println("Error saving bus: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error starting bus: " + e.getMessage());
        }
    }




    /**
     * Complete a bus route
     */
    @Transactional
    public Bus completeRoute(Long busId) {
        Bus bus = findById(busId);
        if (bus == null) {
            throw new RuntimeException("Bus not found with ID: " + busId);
        }

        bus.setStopped(true);
        bus.setProgress(100.0);
        bus.setCurrentLat(bus.getEndLat());
        bus.setCurrentLng(bus.getEndLng());
        bus.setArrivalTime(LocalDateTime.now());

        // Reset passenger count when route is completed
        bus.setPassengers(0);

        return save(bus);
    }

    /**
     * Stop a bus
     */
    @Transactional
    public Bus stopBus(Long busId) {
        Bus bus = findById(busId);
        if (bus == null) {
            throw new RuntimeException("Bus not found with ID: " + busId);
        }

        bus.setStopped(true);
        return save(bus);
    }

    /**
     * Report accident for a bus
     */
    @Transactional
    public Bus reportAccident(Long busId) {
        Bus bus = findById(busId);
        if (bus == null) {
            throw new RuntimeException("Bus not found with ID: " + busId);
        }

        bus.setHasAccident(true);
        bus.setStopped(true);
        return save(bus);
    }

    /**
     * Update bus position and progress
     */
    @Transactional
    public Bus updatePosition(Long busId, Double currentLat, Double currentLng, Double progress) {
        Bus bus = findById(busId);
        if (bus == null) {
            throw new RuntimeException("Bus not found with ID: " + busId);
        }

        bus.setCurrentLat(currentLat);
        bus.setCurrentLng(currentLng);
        bus.setProgress(progress);

        // Update arrival time based on progress if bus is running
        if (!bus.getStopped() && progress != null && progress > 0) {
            bus.setArrivalTime(calculateUpdatedArrivalTime(bus, progress));
        }

        return save(bus);
    }

    /**
     * Update passenger count
     */
    @Transactional
    public Bus updatePassengerCount(Long busId, Integer passengerCount) {
        Bus bus = findById(busId);
        if (bus == null) {
            throw new RuntimeException("Bus not found with ID: " + busId);
        }

        if (passengerCount < 0 || passengerCount > bus.getCapacity()) {
            throw new RuntimeException("Invalid passenger count. Must be between 0 and " + bus.getCapacity());
        }

        bus.setPassengers(passengerCount);
        return save(bus);
    }

    /**
     * Calculate estimated arrival time based on route and current conditions
     */
    private LocalDateTime calculateEstimatedArrivalTime(Bus bus) {
        // Default estimation: 60 minutes if no specific route duration
        int estimatedDurationMinutes = 60;

        // You can enhance this with actual route calculation
        if (bus.getRoute() != null) {
            // This could be enhanced with actual route distance/time calculation
            estimatedDurationMinutes = calculateRouteDuration(bus);
        }

        return bus.getDepartureTime().plusMinutes(estimatedDurationMinutes);
    }

    /**
     * Calculate updated arrival time based on current progress
     */
    private LocalDateTime calculateUpdatedArrivalTime(Bus bus, Double progress) {
        if (bus.getDepartureTime() == null || progress <= 0) {
            return calculateEstimatedArrivalTime(bus);
        }

        // Calculate elapsed time since departure
        Duration elapsed = Duration.between(bus.getDepartureTime(), LocalDateTime.now());

        // Estimate remaining time based on progress
        if (progress >= 100.0) {
            return LocalDateTime.now();
        }

        double remainingProgress = 100.0 - progress;
        double timePerProgress = elapsed.toMinutes() / progress;
        long remainingMinutes = Math.round(remainingProgress * timePerProgress);

        return LocalDateTime.now().plusMinutes(remainingMinutes);
    }

    /**
     * Calculate route duration (placeholder for actual route calculation)
     */
    private int calculateRouteDuration(Bus bus) {
        // This is a placeholder - you would implement actual route calculation
        // considering distance, traffic, stops, etc.
        return 60; // Default 60 minutes
    }

    /**
     * Generate comprehensive bus report
     */
    public BusReport generateBusReport(Bus bus) {
        BusReport report = new BusReport();
        report.setBusName(bus.getName());
        report.setBusLine(bus.getBusLine());

        // Determine status
        String status;
        if (bus.getHasAccident()) {
            status = "Incident";
        } else if (bus.getStopped()) {
            status = "Stopped";
        } else {
            status = "Active";
        }
        report.setStatus(status);

        // Calculate metrics
        report.setSpeed(calculateCurrentSpeed(bus));
        report.setOccupancyRate(calculateOccupancyRate(bus));
        report.setPassengers(bus.getPassengers());
        report.setCapacity(bus.getCapacity());
        report.setProgress(bus.getProgress());

        // These would be calculated from historical data
        report.setTotalIncidents(0);
        report.setOnTimePercentage(95.0);
        report.setRecentIncidents(new ArrayList<>());
        report.setRecommendations(generateRecommendations(bus));

        return report;
    }

    /**
     * Calculate current speed (placeholder)
     */
    private Double calculateCurrentSpeed(Bus bus) {
        // This would calculate actual speed based on position history
        return bus.getStopped() ? 0.0 : 45.0; // Default 45 km/h when moving
    }

    /**
     * Calculate occupancy rate
     */
    private Double calculateOccupancyRate(Bus bus) {
        if (bus.getCapacity() == 0) return 0.0;
        return ((double) bus.getPassengers() / bus.getCapacity()) * 100.0;
    }

    /**
     * Generate recommendations based on bus status
     */
    private List<String> generateRecommendations(Bus bus) {
        List<String> recommendations = new ArrayList<>();

        if (bus.getHasAccident()) {
            recommendations.add("Immediate maintenance required due to accident");
            recommendations.add("Contact emergency services if needed");
        }

        if (calculateOccupancyRate(bus) > 90) {
            recommendations.add("Consider adding additional buses on this route");
        }

        if (bus.getProgress() != null && bus.getProgress() < 10 &&
                bus.getDepartureTime() != null &&
                Duration.between(bus.getDepartureTime(), LocalDateTime.now()).toMinutes() > 30) {
            recommendations.add("Bus appears to be delayed - investigate potential issues");
        }

        return recommendations;
    }

    /**
     * Check if bus can be deleted
     */
    public boolean canDeleteBus(Long busId) {
        Bus bus = findById(busId);
        if (bus == null) return false;

        // Bus cannot be deleted if it has active passengers or is running
        return (bus.getPassengers() == null || bus.getPassengers() == 0) &&
                (bus.getStopped() || bus.getProgress() == null || bus.getProgress() == 0);
    }

    /**
     * Get buses by status
     */
    public List<Bus> getBusesByStatus(String status) {
        return switch (status.toLowerCase()) {
            case "active" -> busRepository.findByIsStoppedFalse();
            case "stopped" -> busRepository.findByIsStoppedTrue();
            case "accident" -> busRepository.findByHasAccidentTrue();
            default -> findAll();
        };
    }

    /**
     * Calculate total distance for a bus (placeholder)
     */
    public Double calculateTotalDistance(Bus bus) {
        // This would calculate actual distance based on coordinates
        // For now, return a placeholder calculation
        if (bus.getStartLat() != null && bus.getStartLng() != null &&
                bus.getEndLat() != null && bus.getEndLng() != null) {
            return calculateDistance(bus.getStartLat(), bus.getStartLng(),
                    bus.getEndLat(), bus.getEndLng());
        }
        return 0.0;
    }



    /**
     * Find all buses assigned to a specific driver
     */
    public List<Bus> findAllByDriverId(Long driverId) {
        return busRepository.findAllByDriverId(driverId);
    }

    /**
     * Calculate distance between two points using Haversine formula
     */
    private Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }
}