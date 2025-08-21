package IMAS.ImasProject.services;

import IMAS.ImasProject.model.Trip;
import IMAS.ImasProject.repository.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TripService {
    private static final Logger log = LoggerFactory.getLogger(TripService.class);

    private final TripRepository tripRepository;
    private final StaffService staffService;

    @Autowired
    public TripService(TripRepository tripRepository, StaffService staffService) {
        this.tripRepository = tripRepository;
        this.staffService = staffService;
    }

    @Transactional
    public Trip save(Trip trip) {
        validateTrip(trip);
        return tripRepository.save(trip);
    }

    @Transactional(readOnly = true)
    public List<Trip> findAll() {
        return tripRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Trip> findById(Long id) {
        return tripRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Trip> findByDriverId(Long driverId) {
        if (!staffService.isDriver(driverId)) {
            throw new IllegalArgumentException("Staff ID " + driverId + " is not a driver");
        }
        return tripRepository.findByDriverId(driverId);
    }

    @Transactional(readOnly = true)
    public List<Trip> findByBusId(Long busId) {
        return tripRepository.findByBusId(busId);
    }

    @Transactional
    public void deleteById(Long id) {
        tripRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long getDriverTripCount(Long driverId) {
        validateDriver(driverId);
        return tripRepository.countByDriverId(driverId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDriverTripStats(Long driverId) {
        validateDriver(driverId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTrips", tripRepository.countByDriverId(driverId));
        stats.put("onTimeTrips", tripRepository.countOnTimeTripsByDriverId(driverId));

        return stats;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDriverMonthlyStats(Long driverId) {
        validateDriver(driverId);

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(11).withDayOfMonth(1);
        List<Map<String, Object>> monthlyStats = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            LocalDate currentMonth = startDate.plusMonths(i);
            LocalDateTime startOfMonth = currentMonth.atStartOfDay();
            LocalDateTime endOfMonth = currentMonth.plusMonths(1).atStartOfDay();

            long tripCount = tripRepository.findByDriverIdAndCreatedAtBetween(driverId, startOfMonth, endOfMonth).size();

            Map<String, Object> monthStats = new HashMap<>();
            monthStats.put("year", currentMonth.getYear());
            monthStats.put("month", currentMonth.getMonthValue());
            monthStats.put("tripCount", tripCount);
            monthlyStats.add(monthStats);
        }

        return monthlyStats;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDriverWeeklyStats(Long driverId) {
        validateDriver(driverId);

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusWeeks(3).with(java.time.DayOfWeek.MONDAY);
        List<Map<String, Object>> weeklyStats = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            LocalDate currentWeek = startDate.plusWeeks(i);
            LocalDateTime startOfWeek = currentWeek.atStartOfDay();
            LocalDateTime endOfWeek = currentWeek.plusDays(7).atStartOfDay();

            long tripCount = tripRepository.findByDriverIdAndCreatedAtBetween(driverId, startOfWeek, endOfWeek).size();

            Map<String, Object> weekStats = new HashMap<>();
            weekStats.put("year", currentWeek.getYear());
            weekStats.put("week", i + 1);
            weekStats.put("tripCount", tripCount);
            weeklyStats.add(weekStats);
        }

        return weeklyStats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDriverOnTimeStats(Long driverId) {
        validateDriver(driverId);

        long totalTrips = tripRepository.countByDriverId(driverId);
        long onTimeTrips = tripRepository.countOnTimeTripsByDriverId(driverId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTrips", totalTrips);
        stats.put("onTimeTrips", onTimeTrips);
        stats.put("onTimePercentage", totalTrips > 0 ? (double) onTimeTrips / totalTrips * 100 : 0.0);

        return stats;
    }

    private void validateDriver(Long driverId) {
        if (!staffService.isDriver(driverId)) {
            throw new IllegalArgumentException("Staff ID " + driverId + " is not a driver");
        }
    }

    private void validateTrip(Trip trip) {
        if (trip == null) {
            throw new IllegalArgumentException("Trip cannot be null");
        }
        if (trip.getBus() == null) {
            throw new IllegalArgumentException("Trip must have a bus");
        }
        if (trip.getDriver() == null) {
            throw new IllegalArgumentException("Trip must have a driver");
        }
        if (trip.getOrigin() == null || trip.getOrigin().isBlank()) {
            throw new IllegalArgumentException("Trip must have an origin");
        }
        if (trip.getDestination() == null || trip.getDestination().isBlank()) {
            throw new IllegalArgumentException("Trip must have a destination");
        }
    }
}