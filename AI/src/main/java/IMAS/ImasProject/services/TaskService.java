package IMAS.ImasProject.services;

import IMAS.ImasProject.dto.StaffDTO;
import IMAS.ImasProject.exception.ResourceNotFoundException;
import IMAS.ImasProject.model.Incident;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.Task;
import IMAS.ImasProject.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final IncidentService incidentService;
    private final StaffService staffService;
    private final EmailService emailService;

    // Manual constructor to replace @RequiredArgsConstructor
    @Autowired
    public TaskService(TaskRepository taskRepository,
                       IncidentService incidentService,
                       StaffService staffService,
                       EmailService emailService) {
        this.taskRepository = taskRepository;
        this.incidentService = incidentService;
        this.staffService = staffService;
        this.emailService = emailService;
    }

    @Transactional
    public Task createTask(String incidentId, Long technicianId, String description,
                           Task.Priority priority, LocalDateTime deadline) {
        logger.info("Creating task for incident: {} and technician: {}", incidentId, technicianId);

        Incident incident = incidentService.getIncidentById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + incidentId));

        Staff technician = staffService.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + technicianId));

        Task task = Task.builder()
                .incident(incident)
                .technician(technician)
                .description(description)
                .priority(priority != null ? priority : Task.Priority.MEDIUM)
                .deadline(deadline)
                .progress(0)
                .status(Task.Status.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);

        // Envoyer une notification par email au technicien
        try {
            sendTaskAssignmentNotification(savedTask);
        } catch (Exception e) {
            logger.warn("Failed to send task assignment notification", e);
            // Ne pas échouer la création de tâche si l'email échoue
        }

        logger.info("Task created successfully with ID: {}", savedTask.getTaskId());
        return savedTask;
    }

    @Transactional
    public Task updateTaskProgress(String taskId, int progress, String status, String notes) {
        logger.info("Updating task progress: {} to {}%", taskId, progress);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        // Sauvegarder l'ancien statut pour notification
        Task.Status oldStatus = task.getStatus();

        // Mettre à jour les champs
        if (progress >= 0 && progress <= 100) {
            task.setProgress(progress);
        }

        if (status != null && !status.trim().isEmpty()) {
            try {
                Task.Status newStatus = Task.Status.valueOf(status.toUpperCase());
                task.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid status provided: {}", status);
                throw new IllegalArgumentException("Invalid status: " + status);
            }
        }

        if (notes != null && !notes.trim().isEmpty()) {
            task.addProgressNote(notes);
        }

        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);

        // Envoyer notification si le statut a changé
        if (!oldStatus.equals(updatedTask.getStatus())) {
            try {
                sendTaskStatusChangeNotification(updatedTask, oldStatus);
            } catch (Exception e) {
                logger.warn("Failed to send task status change notification", e);
            }
        }

        logger.info("Task updated successfully: {}", taskId);
        return updatedTask;
    }

    @Transactional
    public Task completeTask(String taskId, String completionNotes) {
        logger.info("Completing task: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        task.markAsCompleted();

        if (completionNotes != null && !completionNotes.trim().isEmpty()) {
            task.addProgressNote("Task completed: " + completionNotes);
        }

        Task completedTask = taskRepository.save(task);

        try {
            sendTaskCompletionNotification(completedTask);
        } catch (Exception e) {
            logger.warn("Failed to send task completion notification", e);
        }

        logger.info("Task completed successfully: {}", taskId);
        return completedTask;
    }

    @Transactional
    public Task cancelTask(String taskId, String reason) {
        logger.info("Cancelling task: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        task.markAsCancelled();

        if (reason != null && !reason.trim().isEmpty()) {
            task.addProgressNote("Task cancelled: " + reason);
        }

        Task cancelledTask = taskRepository.save(task);

        try {
            sendTaskCancellationNotification(cancelledTask, reason);
        } catch (Exception e) {
            logger.warn("Failed to send task cancellation notification", e);
        }

        logger.info("Task cancelled successfully: {}", taskId);
        return cancelledTask;
    }

    @Transactional
    public Task reassignTask(String taskId, Long newTechnicianId) {
        logger.info("Reassigning task: {} to technician: {}", taskId, newTechnicianId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        Staff oldTechnician = task.getTechnician();
        Staff newTechnician = staffService.findById(newTechnicianId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + newTechnicianId));

        task.setTechnician(newTechnician);
        task.addProgressNote("Task reassigned from " + oldTechnician.getFirstName() + " " +
                oldTechnician.getLastName() + " to " + newTechnician.getFirstName() +
                " " + newTechnician.getLastName());

        Task reassignedTask = taskRepository.save(task);

        try {
            sendTaskReassignmentNotification(reassignedTask, oldTechnician);
        } catch (Exception e) {
            logger.warn("Failed to send task reassignment notification", e);
        }

        logger.info("Task reassigned successfully: {}", taskId);
        return reassignedTask;
    }

    public List<Task> getTasksByTechnician(Long technicianId) {
        logger.debug("Fetching tasks for technician: {}", technicianId);
        return taskRepository.findByTechnicianId(technicianId);
    }

    public List<Task> getTasksByIncident(String incidentId) {
        logger.debug("Fetching tasks for incident: {}", incidentId);
        return taskRepository.findByIncidentIncidentId(incidentId);
    }

    public List<Task> getTasksByStatus(Task.Status status) {
        logger.debug("Fetching tasks with status: {}", status);
        return taskRepository.findByStatus(status);
    }

    public Task getTaskById(String taskId) {
        logger.debug("Fetching task by ID: {}", taskId);
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    public Optional<Task> findTaskById(String taskId) {
        logger.debug("Finding task by ID: {}", taskId);
        return taskRepository.findById(taskId);
    }

    public List<Task> getAllTasks() {
        logger.debug("Fetching all tasks");
        return taskRepository.findAll();
    }

    public List<Task> getOverdueTasks() {
        logger.debug("Fetching overdue tasks");
        return taskRepository.findAll().stream()
                .filter(Task::isOverdue)
                .toList();
    }

    public List<Task> getTasksForTechnicianInDateRange(Long technicianId, LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching tasks for technician: {} between {} and {}", technicianId, startDate, endDate);
        return taskRepository.findByTechnicianIdAndCreatedAtBetween(technicianId, startDate, endDate);
    }

    public List<Task> getTasksInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        logger.debug("Fetching tasks between {} and {}", startDate, endDate);
        return taskRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public long countTasksByTechnicianAndStatus(Long technicianId, Task.Status status) {
        return taskRepository.countByTechnicianIdAndStatus(technicianId, status);
    }

    public long countTasksByTechnicianStatusAndDateRange(Long technicianId, Task.Status status,
                                                         LocalDateTime startDate, LocalDateTime endDate) {
        return taskRepository.countByTechnicianIdAndStatusAndDateBetween(technicianId, status, startDate, endDate);
    }

    public Double getAverageCompletionTimeForTechnician(Long technicianId, LocalDateTime startDate, LocalDateTime endDate) {
        return taskRepository.findAverageCompletionTimeByTechnicianAndDateBetween(technicianId, startDate, endDate);
    }

    @Transactional
    public void deleteTask(String taskId) {
        logger.info("Deleting task: {}", taskId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        taskRepository.delete(task);
        logger.info("Task deleted successfully: {}", taskId);
    }

    // Méthodes privées pour les notifications
    private void sendTaskAssignmentNotification(Task task) {
        try {
            StaffDTO technicianDto = staffService.convertToDTO(task.getTechnician());
            String subject = "New Task Assignment - " + task.getPriority().getDisplayName() + " Priority";
            String body = String.format(
                    "Dear %s %s,\n\n" +
                            "You have been assigned a new task:\n\n" +
                            "Task ID: %s\n" +
                            "Description: %s\n" +
                            "Priority: %s\n" +
                            "Deadline: %s\n" +
                            "Incident ID: %s\n\n" +
                            "Please log into the system to view more details and start working on this task.\n\n" +
                            "Best regards,\nIMAS Management System",
                    technicianDto.getFirstName(),
                    technicianDto.getLastName(),
                    task.getTaskId(),
                    task.getDescription(),
                    task.getPriority().getDisplayName(),
                    task.getDeadline() != null ? task.getDeadline().toString() : "Not specified",
                    task.getIncident().getIncidentId()
            );

            emailService.sendEmail(technicianDto.getEmail(), subject, body);
        } catch (Exception e) {
            logger.error("Failed to send task assignment notification", e);
        }
    }

    private void sendTaskStatusChangeNotification(Task task, Task.Status oldStatus) {
        try {
            StaffDTO technicianDto = staffService.convertToDTO(task.getTechnician());
            String subject = "Task Status Update - " + task.getTaskId();
            String body = String.format(
                    "Dear %s %s,\n\n" +
                            "Your task status has been updated:\n\n" +
                            "Task ID: %s\n" +
                            "Description: %s\n" +
                            "Previous Status: %s\n" +
                            "New Status: %s\n" +
                            "Progress: %d%%\n\n" +
                            "Best regards,\nIMAS Management System",
                    technicianDto.getFirstName(),
                    technicianDto.getLastName(),
                    task.getTaskId(),
                    task.getDescription(),
                    oldStatus.getDisplayName(),
                    task.getStatus().getDisplayName(),
                    task.getProgress()
            );

            emailService.sendEmail(technicianDto.getEmail(), subject, body);
        } catch (Exception e) {
            logger.error("Failed to send task status change notification", e);
        }
    }

    private void sendTaskCompletionNotification(Task task) {
        try {
            StaffDTO technicianDto = staffService.convertToDTO(task.getTechnician());
            String subject = "Task Completed - " + task.getTaskId();
            String body = String.format(
                    "Dear %s %s,\n\n" +
                            "Congratulations! You have successfully completed the following task:\n\n" +
                            "Task ID: %s\n" +
                            "Description: %s\n" +
                            "Completion Time: %s\n" +
                            "Duration: %s hours\n\n" +
                            "Thank you for your excellent work.\n\n" +
                            "Best regards,\nIMAS Management System",
                    technicianDto.getFirstName(),
                    technicianDto.getLastName(),
                    task.getTaskId(),
                    task.getDescription(),
                    task.getUpdatedAt().toString(),
                    task.getDurationInHours() != null ? task.getDurationInHours().toString() : "N/A"
            );

            emailService.sendEmail(technicianDto.getEmail(), subject, body);
        } catch (Exception e) {
            logger.error("Failed to send task completion notification", e);
        }
    }

    private void sendTaskCancellationNotification(Task task, String reason) {
        try {
            StaffDTO technicianDto = staffService.convertToDTO(task.getTechnician());
            String subject = "Task Cancelled - " + task.getTaskId();
            String body = String.format(
                    "Dear %s %s,\n\n" +
                            "The following task has been cancelled:\n\n" +
                            "Task ID: %s\n" +
                            "Description: %s\n" +
                            "Reason: %s\n" +
                            "Cancellation Time: %s\n\n" +
                            "Please contact your supervisor if you have any questions.\n\n" +
                            "Best regards,\nIMAS Management System",
                    technicianDto.getFirstName(),
                    technicianDto.getLastName(),
                    task.getTaskId(),
                    task.getDescription(),
                    reason != null ? reason : "Not specified",
                    task.getUpdatedAt().toString()
            );

            emailService.sendEmail(technicianDto.getEmail(), subject, body);
        } catch (Exception e) {
            logger.error("Failed to send task cancellation notification", e);
        }
    }

    private void sendTaskReassignmentNotification(Task task, Staff oldTechnician) {
        try {
            // Notifier le nouveau technicien
            StaffDTO newTechnicianDto = staffService.convertToDTO(task.getTechnician());
            String subjectNew = "Task Reassigned to You - " + task.getTaskId();
            String bodyNew = String.format(
                    "Dear %s %s,\n\n" +
                            "A task has been reassigned to you:\n\n" +
                            "Task ID: %s\n" +
                            "Description: %s\n" +
                            "Priority: %s\n" +
                            "Deadline: %s\n" +
                            "Current Progress: %d%%\n\n" +
                            "Please log into the system to view more details.\n\n" +
                            "Best regards,\nIMAS Management System",
                    newTechnicianDto.getFirstName(),
                    newTechnicianDto.getLastName(),
                    task.getTaskId(),
                    task.getDescription(),
                    task.getPriority().getDisplayName(),
                    task.getDeadline() != null ? task.getDeadline().toString() : "Not specified",
                    task.getProgress()
            );

            emailService.sendEmail(newTechnicianDto.getEmail(), subjectNew, bodyNew);

            // Notifier l'ancien technicien
            StaffDTO oldTechnicianDto = staffService.convertToDTO(oldTechnician);
            String subjectOld = "Task Reassigned - " + task.getTaskId();
            String bodyOld = String.format(
                    "Dear %s %s,\n\n" +
                            "The following task has been reassigned to another technician:\n\n" +
                            "Task ID: %s\n" +
                            "Description: %s\n" +
                            "Reassigned to: %s %s\n" +
                            "Reassignment Time: %s\n\n" +
                            "Thank you for your work on this task.\n\n" +
                            "Best regards,\nIMAS Management System",
                    oldTechnicianDto.getFirstName(),
                    oldTechnicianDto.getLastName(),
                    task.getTaskId(),
                    task.getDescription(),
                    newTechnicianDto.getFirstName(),
                    newTechnicianDto.getLastName(),
                    task.getUpdatedAt().toString()
            );

            emailService.sendEmail(oldTechnicianDto.getEmail(), subjectOld, bodyOld);

        } catch (Exception e) {
            logger.error("Failed to send task reassignment notification", e);
        }
    }
}