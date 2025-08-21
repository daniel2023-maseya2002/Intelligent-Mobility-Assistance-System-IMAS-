package IMAS.ImasProject.services;

import IMAS.ImasProject.model.*;
import IMAS.ImasProject.repository.ScheduleBusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleBusService {

    @Autowired
    private ScheduleBusRepository scheduleBusRepository;

    @Autowired
    private BusService busService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private RouteService routeService;

    public ScheduleBus save(ScheduleBus scheduleBus) {
        if (scheduleBus == null) {
            throw new IllegalArgumentException("ScheduleBus cannot be null");
        }

        // Validate required fields
        if (scheduleBus.getBus() == null) {
            throw new IllegalArgumentException("Bus cannot be null");
        }
        if (scheduleBus.getDriver() == null) {
            throw new IllegalArgumentException("Driver cannot be null");
        }
        if (scheduleBus.getRoute() == null) {
            throw new IllegalArgumentException("Route cannot be null");
        }
        if (scheduleBus.getDepartureTime() == null) {
            throw new IllegalArgumentException("Departure time cannot be null");
        }
        if (scheduleBus.getEstimatedDurationMinutes() == null || scheduleBus.getEstimatedDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Estimated duration must be a positive integer");
        }

        // Auto-calculate arrival time if not set
        if (scheduleBus.getArrivalTime() == null) {
            LocalDateTime arrivalTime = scheduleBus.getDepartureTime().plusMinutes(scheduleBus.getEstimatedDurationMinutes());
            scheduleBus.setArrivalTime(arrivalTime);
        }

        // Auto-calculate day of week if not set
        if (scheduleBus.getDayOfWeek() == null) {
            DayOfWeek dayOfWeek = scheduleBus.getDepartureTime().getDayOfWeek();
            scheduleBus.setDayOfWeek(dayOfWeek);
        }

        scheduleBus.setUpdatedAt(LocalDateTime.now());
        return scheduleBusRepository.save(scheduleBus);
    }

    public List<ScheduleBus> findAll() {
        return scheduleBusRepository.findAll();
    }

    public ScheduleBus findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Schedule ID cannot be null");
        }
        return scheduleBusRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        if (id == null || !scheduleBusRepository.existsById(id)) {
            throw new IllegalArgumentException("Schedule not found for ID: " + id);
        }
        scheduleBusRepository.deleteById(id);
    }

    public List<ScheduleBus> findByBusId(Long busId) {
        if (busId == null) {
            throw new IllegalArgumentException("Bus ID cannot be null");
        }
        return scheduleBusRepository.findByBusId(busId);
    }

    public List<ScheduleBus> findByDayOfWeek(DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Day of week cannot be null");
        }
        return scheduleBusRepository.findByDayOfWeekAndIsActiveTrue(dayOfWeek);
    }

    public List<ScheduleBus> findByDriverId(Long driverId) {
        if (driverId == null) {
            throw new IllegalArgumentException("Driver ID cannot be null");
        }
        return scheduleBusRepository.findByDriverId(driverId);
    }

    public List<ScheduleBus> findByRouteId(Long routeId) {
        if (routeId == null) {
            throw new IllegalArgumentException("Route ID cannot be null");
        }
        return scheduleBusRepository.findByRouteId(routeId);
    }

    public List<ScheduleBus> findActiveSchedules() {
        return scheduleBusRepository.findByIsActiveTrue();
    }

    public List<ScheduleBus> findSchedulesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        return scheduleBusRepository.findByDepartureTimeBetween(startDate, endDate);
    }

    public long countActiveSchedules() {
        return scheduleBusRepository.countByIsActiveTrue();
    }

    public long countSchedulesByDay(DayOfWeek dayOfWeek) {
        return scheduleBusRepository.countByDayOfWeekAndIsActiveTrue(dayOfWeek);
    }

    public List<ScheduleBus> createWeeklySchedule(Bus bus, Staff driver, Route route, LocalDateTime startTime, Integer estimatedDurationMinutes) {
        if (bus == null || driver == null || route == null || startTime == null || estimatedDurationMinutes == null) {
            throw new IllegalArgumentException("All parameters are required for weekly schedule creation");
        }

        List<ScheduleBus> weeklySchedules = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDateTime departureTime = startTime.plusDays(i);
            LocalDateTime arrivalTime = departureTime.plusMinutes(estimatedDurationMinutes);

            ScheduleBus schedule = new ScheduleBus();
            schedule.setBus(bus);
            schedule.setDriver(driver);
            schedule.setRoute(route);
            schedule.setDayOfWeek(departureTime.getDayOfWeek());
            schedule.setDepartureTime(departureTime);
            schedule.setArrivalTime(arrivalTime);
            schedule.setEstimatedDurationMinutes(estimatedDurationMinutes);
            schedule.setIsActive(true);

            weeklySchedules.add(save(schedule));
        }

        return weeklySchedules;
    }

    public ScheduleBus updateSchedule(Long id, ScheduleBus updatedSchedule) {
        if (id == null || updatedSchedule == null) {
            throw new IllegalArgumentException("Schedule ID and updated schedule cannot be null");
        }

        ScheduleBus existingSchedule = findById(id);
        if (existingSchedule == null) {
            throw new IllegalArgumentException("Schedule not found for ID: " + id);
        }

        // Update fields only if provided in updatedSchedule
        if (updatedSchedule.getBus() != null) {
            existingSchedule.setBus(updatedSchedule.getBus());
        }
        if (updatedSchedule.getDriver() != null) {
            existingSchedule.setDriver(updatedSchedule.getDriver());
        }
        if (updatedSchedule.getRoute() != null) {
            existingSchedule.setRoute(updatedSchedule.getRoute());
        }
        if (updatedSchedule.getDayOfWeek() != null) {
            existingSchedule.setDayOfWeek(updatedSchedule.getDayOfWeek());
        }
        if (updatedSchedule.getDepartureTime() != null) {
            existingSchedule.setDepartureTime(updatedSchedule.getDepartureTime());
        }
        if (updatedSchedule.getArrivalTime() != null) {
            existingSchedule.setArrivalTime(updatedSchedule.getArrivalTime());
        }
        if (updatedSchedule.getEstimatedDurationMinutes() != null) {
            existingSchedule.setEstimatedDurationMinutes(updatedSchedule.getEstimatedDurationMinutes());
        }
        if (updatedSchedule.getIsActive() != null) {
            existingSchedule.setIsActive(updatedSchedule.getIsActive());
        }

        return save(existingSchedule);
    }

    public List<ScheduleBus> rescheduleBus(Long busId, LocalDateTime newDepartureTime, Integer newDuration) {
        if (busId == null || newDepartureTime == null || newDuration == null || newDuration <= 0) {
            throw new IllegalArgumentException("Bus ID, departure time, and duration must be valid");
        }

        List<ScheduleBus> existingSchedules = findByBusId(busId);
        if (existingSchedules.isEmpty()) {
            throw new IllegalArgumentException("No schedules found for bus ID: " + busId);
        }

        for (ScheduleBus schedule : existingSchedules) {
            schedule.setDepartureTime(newDepartureTime);
            schedule.setArrivalTime(newDepartureTime.plusMinutes(newDuration));
            schedule.setEstimatedDurationMinutes(newDuration);
            schedule.setDayOfWeek(newDepartureTime.getDayOfWeek());
            save(schedule);
        }
        return existingSchedules;
    }

    public void deactivateSchedule(Long id) {
        ScheduleBus schedule = findById(id);
        if (schedule != null) {
            schedule.setIsActive(false);
            save(schedule);
        }
    }

    public void activateSchedule(Long id) {
        ScheduleBus schedule = findById(id);
        if (schedule != null) {
            schedule.setIsActive(true);
            save(schedule);
        }
    }
}