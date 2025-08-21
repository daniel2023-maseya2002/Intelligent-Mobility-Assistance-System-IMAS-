package IMAS.ImasProject.controller;


import IMAS.ImasProject.dto.ScheduleDTO;
import IMAS.ImasProject.model.MaintenanceTask;
import IMAS.ImasProject.model.Schedule;
import IMAS.ImasProject.services.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestBody ScheduleDTO scheduleDTO) {
        try {
            Schedule createdSchedule = scheduleService.createScheduleFromDTO(scheduleDTO);
            return ResponseEntity.ok(createdSchedule);
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            throw new RuntimeException("Error creating schedule: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        return scheduleService.getScheduleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        List<Schedule> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Long id, @RequestBody ScheduleDTO scheduleDTO) {
        Schedule updatedSchedule = scheduleService.updateScheduleFromDTO(id, scheduleDTO);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-technician/{technicianId}")
    public ResponseEntity<List<Schedule>> getSchedulesByTechnician(@PathVariable Long technicianId) {
        List<Schedule> schedules = scheduleService.getSchedulesByTechnician(technicianId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/by-task/{taskId}")
    public ResponseEntity<List<Schedule>> getSchedulesByTask(@PathVariable String taskId) {
        List<Schedule> schedules = scheduleService.getSchedulesByTask(taskId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<List<Schedule>> getSchedulesBetweenDates(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<Schedule> schedules = scheduleService.getSchedulesBetweenDates(start, end);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<Schedule>> getSchedulesByStatus(@PathVariable String status) {
        MaintenanceTask.Status statusEnum = MaintenanceTask.Status.valueOf(status.toUpperCase());
        List<Schedule> schedules = scheduleService.getSchedulesByStatus(statusEnum);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping("/from-task")
    public ResponseEntity<Schedule> createScheduleFromTask(
            @RequestParam String taskId,
            @RequestParam Long technicianId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        Schedule schedule = scheduleService.createScheduleFromTask(taskId, technicianId, startTime, endTime);
        return ResponseEntity.ok(schedule);
    }
}