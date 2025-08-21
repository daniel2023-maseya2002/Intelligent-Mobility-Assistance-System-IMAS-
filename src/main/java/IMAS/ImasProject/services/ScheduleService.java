package IMAS.ImasProject.services;


import IMAS.ImasProject.dto.ScheduleDTO;
import IMAS.ImasProject.model.MaintenanceTask;
import IMAS.ImasProject.model.Schedule;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.repository.MaintenanceTaskRepository;
import IMAS.ImasProject.repository.ScheduleRepository;
import IMAS.ImasProject.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MaintenanceTaskRepository taskRepository;

    @Autowired
    private StaffRepository staffRepository;

    public Schedule createScheduleFromDTO(ScheduleDTO scheduleDTO) {
        // Validation des données
        if (scheduleDTO.getTaskId() == null || scheduleDTO.getTaskId().trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID is required");
        }
        if (scheduleDTO.getTechnicianId() == null) {
            throw new IllegalArgumentException("Technician ID is required");
        }
        if (scheduleDTO.getStartTime() == null) {
            throw new IllegalArgumentException("Start time is required");
        }
        if (scheduleDTO.getEndTime() == null) {
            throw new IllegalArgumentException("End time is required");
        }
        if (scheduleDTO.getEndTime().isBefore(scheduleDTO.getStartTime()) ||
                scheduleDTO.getEndTime().isEqual(scheduleDTO.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Vérifier que la tâche existe - FIXED: Use findById instead of findByTaskId
        MaintenanceTask task = taskRepository.findById(scheduleDTO.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + scheduleDTO.getTaskId()));

        // Vérifier que le technicien existe et est actif
        Staff technician = staffRepository.findById(scheduleDTO.getTechnicianId())
                .orElseThrow(() -> new IllegalArgumentException("Technician not found with ID: " + scheduleDTO.getTechnicianId()));

        if (!technician.isActive()) {
            throw new IllegalArgumentException("Technician is not active");
        }

        // Vérifier les conflits d'horaires
        List<Schedule> conflictingSchedules = scheduleRepository.findConflictingSchedules(
                scheduleDTO.getTechnicianId(),
                scheduleDTO.getStartTime(),
                scheduleDTO.getEndTime()
        );

        if (!conflictingSchedules.isEmpty()) {
            throw new IllegalArgumentException("Technician has conflicting schedules during this time period");
        }

        // Créer le schedule
        Schedule schedule = new Schedule();
        schedule.setTaskId(scheduleDTO.getTaskId());
        schedule.setTechnician(technician);
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setPriority(scheduleDTO.getPriorityEnum());
        schedule.setStatus(scheduleDTO.getStatusEnum());
        schedule.setNotes(scheduleDTO.getNotes());
        schedule.setIsRecurring(scheduleDTO.getIsRecurring() != null ? scheduleDTO.getIsRecurring() : false);

        if (Boolean.TRUE.equals(schedule.getIsRecurring())) {
            schedule.setRecurrenceType(scheduleDTO.getRecurrenceTypeEnum());
            schedule.setRecurrenceEndDate(scheduleDTO.getRecurrenceEndDate());
        }

        return scheduleRepository.save(schedule);
    }

    public Schedule updateScheduleFromDTO(Long id, ScheduleDTO scheduleDTO) {
        Schedule existingSchedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with ID: " + id));

        // Validation des données
        if (scheduleDTO.getTaskId() != null && !scheduleDTO.getTaskId().trim().isEmpty()) {
            // FIXED: Use findById instead of findByTaskId
            MaintenanceTask task = taskRepository.findById(scheduleDTO.getTaskId())
                    .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + scheduleDTO.getTaskId()));
            existingSchedule.setTaskId(scheduleDTO.getTaskId());
        }

        if (scheduleDTO.getTechnicianId() != null) {
            Staff technician = staffRepository.findById(scheduleDTO.getTechnicianId())
                    .orElseThrow(() -> new IllegalArgumentException("Technician not found with ID: " + scheduleDTO.getTechnicianId()));

            if (!technician.isActive()) {
                throw new IllegalArgumentException("Technician is not active");
            }
            existingSchedule.setTechnician(technician);
        }

        if (scheduleDTO.getStartTime() != null) {
            existingSchedule.setStartTime(scheduleDTO.getStartTime());
        }

        if (scheduleDTO.getEndTime() != null) {
            existingSchedule.setEndTime(scheduleDTO.getEndTime());
        }

        // Vérifier que l'heure de fin est après l'heure de début
        if (existingSchedule.getEndTime().isBefore(existingSchedule.getStartTime()) ||
                existingSchedule.getEndTime().isEqual(existingSchedule.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Vérifier les conflits d'horaires (exclure le schedule actuel)
        List<Schedule> conflictingSchedules = scheduleRepository.findConflictingSchedulesExcludingCurrent(
                existingSchedule.getTechnician().getId(),
                existingSchedule.getStartTime(),
                existingSchedule.getEndTime(),
                id
        );

        if (!conflictingSchedules.isEmpty()) {
            throw new IllegalArgumentException("Technician has conflicting schedules during this time period");
        }

        if (scheduleDTO.getPriority() != null) {
            existingSchedule.setPriority(scheduleDTO.getPriorityEnum());
        }

        if (scheduleDTO.getStatus() != null) {
            existingSchedule.setStatus(scheduleDTO.getStatusEnum());
        }

        if (scheduleDTO.getNotes() != null) {
            existingSchedule.setNotes(scheduleDTO.getNotes());
        }

        if (scheduleDTO.getIsRecurring() != null) {
            existingSchedule.setIsRecurring(scheduleDTO.getIsRecurring());

            if (Boolean.TRUE.equals(scheduleDTO.getIsRecurring())) {
                existingSchedule.setRecurrenceType(scheduleDTO.getRecurrenceTypeEnum());
                existingSchedule.setRecurrenceEndDate(scheduleDTO.getRecurrenceEndDate());
            } else {
                existingSchedule.setRecurrenceType(null);
                existingSchedule.setRecurrenceEndDate(null);
            }
        }

        return scheduleRepository.save(existingSchedule);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Optional<Schedule> getScheduleById(Long id) {
        return scheduleRepository.findByIdWithDetails(id);
    }

    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Schedule not found with ID: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByTechnician(Long technicianId) {
        return scheduleRepository.findByTechnicianIdWithDetails(technicianId);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByTask(String taskId) {
        return scheduleRepository.findByTaskIdWithDetails(taskId);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesBetweenDates(LocalDateTime start, LocalDateTime end) {
        return scheduleRepository.findByStartTimeBetweenWithDetails(start, end);
    }

    @Transactional(readOnly = true)
    public List<Schedule> getSchedulesByStatus(MaintenanceTask.Status status) {
        // Convertir MaintenanceTask.Status vers Schedule.Status
        Schedule.Status scheduleStatus;
        try {
            scheduleStatus = Schedule.Status.valueOf(status.name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        return scheduleRepository.findByStatusWithDetails(scheduleStatus);
    }

    public Schedule createScheduleFromTask(String taskId, Long technicianId, LocalDateTime startTime, LocalDateTime endTime) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        scheduleDTO.setTaskId(taskId);
        scheduleDTO.setTechnicianId(technicianId);
        scheduleDTO.setStartTime(startTime);
        scheduleDTO.setEndTime(endTime);
        scheduleDTO.setPriority("MEDIUM");
        scheduleDTO.setStatus("SCHEDULED");

        return createScheduleFromDTO(scheduleDTO);
    }
}