package IMAS.ImasProject.controller;


import IMAS.ImasProject.model.Equipment;
import IMAS.ImasProject.model.MaintenanceTask;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.StaffRole;
import IMAS.ImasProject.repository.EquipmentRepository;
import IMAS.ImasProject.repository.ScheduleRepository;
import IMAS.ImasProject.repository.StaffRepository;
import IMAS.ImasProject.services.EmailService;
import IMAS.ImasProject.services.MaintenanceTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MaintenanceTaskController {

    // AJOUT: Logger pour le débogage
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceTaskController.class);

    private final MaintenanceTaskService taskService;
    private final EquipmentRepository equipmentRepository;
    private final StaffRepository staffRepository;
    private final EmailService emailService; // AJOUT
    private final ScheduleRepository scheduleRepository;



    @Autowired
    public MaintenanceTaskController(MaintenanceTaskService taskService,
                                     EquipmentRepository equipmentRepository,
                                     StaffRepository staffRepository,
                                     ScheduleRepository scheduleRepository,
                                     EmailService emailService) { // AJOUT du paramètre
        this.taskService = taskService;
        this.equipmentRepository = equipmentRepository;
        this.staffRepository = staffRepository;
        this.scheduleRepository= scheduleRepository;
        this.emailService = emailService; // AJOUT
    }

    // AJOUT: Méthode privée pour envoyer l'email de notification
    private void sendTaskAssignmentEmail(MaintenanceTask task, Staff technician) {
        try {
            String subject = "New Maintenance Task Assigned - " + task.getTaskId();

            String body = buildTaskAssignmentEmailBody(task, technician);

            emailService.sendEmail(technician.getEmail(), subject, body);
            logger.info("Task assignment email sent successfully to technician: {} for task: {}",
                    technician.getEmail(), task.getTaskId());

        } catch (Exception e) {
            logger.error("Failed to send task assignment email to technician: {} for task: {}. Error: {}",
                    technician.getEmail(), task.getTaskId(), e.getMessage());
            // Ne pas faire échouer l'assignation si l'email échoue
        }
    }

    // AJOUT: Méthode pour construire le corps de l'email
    private String buildTaskAssignmentEmailBody(MaintenanceTask task, Staff technician) {
        StringBuilder body = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");

        body.append("Dear ").append(technician.getFirstName()).append(" ").append(technician.getLastName()).append(",\n\n");

        body.append("You have been assigned a new maintenance task. Please find the details below:\n\n");

        body.append("TASK INFORMATION:\n");
        body.append("═══════════════\n");
        body.append("Task ID: ").append(task.getTaskId()).append("\n");
        body.append("Description: ").append(task.getDescription()).append("\n");
        body.append("Priority: ").append(task.getPriority().toString()).append("\n");
        body.append("Status: ").append(task.getStatus().toString()).append("\n");
        body.append("Estimated Duration: ").append(task.getEstimatedDurationMinutes()).append(" minutes\n");
        body.append("Assigned Date: ").append(task.getCreationDate().format(formatter)).append("\n");

        if (task.getDueDate() != null) {
            body.append("Due Date: ").append(task.getDueDate().format(formatter)).append("\n");
        }

        if (task.getRelatedEquipment() != null) {
            body.append("\nEQUIPMENT INFORMATION:\n");
            body.append("═══════════════════\n");
            body.append("Equipment: ").append(task.getRelatedEquipment().getName()).append("\n");
            body.append("Location: ").append(task.getRelatedEquipment().getLocation()).append("\n");
            body.append("Serial Number: ").append(task.getRelatedEquipment().getSerialNumber()).append("\n");
        }

        if (!task.getRequiredSkills().isEmpty()) {
            body.append("\nREQUIRED SKILLS:\n");
            body.append("══════════════\n");
            for (String skill : task.getRequiredSkills()) {
                body.append("• ").append(skill).append("\n");
            }
        }

        if (!task.getRequiredParts().isEmpty()) {
            body.append("\nREQUIRED PARTS:\n");
            body.append("═════════════\n");
            for (String part : task.getRequiredParts()) {
                body.append("• ").append(part).append("\n");
            }
        }

        body.append("\nNEXT STEPS:\n");
        body.append("═════════\n");
        body.append("1. Review the task details carefully\n");
        body.append("2. Prepare the necessary tools and parts\n");
        body.append("3. Update the task status once you begin working\n");
        body.append("4. Contact your supervisor if you have any questions\n");

        body.append("\nPlease log into the maintenance system to view complete task details and update the progress.\n\n");

        body.append("If you have any questions or concerns about this assignment, please don't hesitate to contact your supervisor.\n\n");

        body.append("Best regards,\n");
        body.append("Maintenance Management System\n");
        body.append("Automated Notification Service");

        return body.toString();
    }





    // MODIFICATION: Méthode createTask modifiée pour inclure l'envoi d'email
    @PostMapping
    public ResponseEntity<MaintenanceTask> createTask(@RequestBody Map<String, Object> requestBody) {
        try {
            validateRequestBody(requestBody, true);

            String taskId = (String) requestBody.get("taskId");
            String description = (String) requestBody.get("description");
            MaintenanceTask.Priority priority = MaintenanceTask.Priority.valueOf(
                    ((String) requestBody.get("priority")).toUpperCase());
            Duration estimatedDuration = Duration.ofMinutes(
                    Long.parseLong(requestBody.get("estimatedDurationMinutes").toString()));

            MaintenanceTask task = taskService.createTask(taskId, description, priority, estimatedDuration);

            // Assign equipment if provided
            if (requestBody.containsKey("equipmentId")) {
                Long equipmentId = Long.parseLong(requestBody.get("equipmentId").toString());
                task = taskService.assignEquipment(task.getTaskId(), equipmentId);
                task = taskService.getTaskById(task.getTaskId()).orElse(task);
            }

            // MODIFICATION: Assign technician if provided et envoyer email
            if (requestBody.containsKey("technicianId")) {
                Long technicianId = Long.parseLong(requestBody.get("technicianId").toString());
                task = taskService.assignStaffTechnician(task.getTaskId(), technicianId);

                // AJOUT: Envoyer email de notification
                Staff technician = staffRepository.findById(technicianId).orElse(null);
                if (technician != null) {
                    sendTaskAssignmentEmail(task, technician);
                }
            }

            // Add required skills if provided
            if (requestBody.containsKey("requiredSkills")) {
                @SuppressWarnings("unchecked")
                List<String> skills = (List<String>) requestBody.get("requiredSkills");
                for (String skill : skills) {
                    if (skill != null && !skill.trim().isEmpty()) {
                        taskService.addRequiredSkill(task.getTaskId(), skill.trim());
                    }
                }
            }

            // Add required parts if provided
            if (requestBody.containsKey("requiredParts")) {
                @SuppressWarnings("unchecked")
                List<String> parts = (List<String>) requestBody.get("requiredParts");
                for (String part : parts) {
                    if (part != null && !part.trim().isEmpty()) {
                        taskService.addRequiredPart(task.getTaskId(), part.trim());
                    }
                }
            }

            task = taskService.getTaskById(task.getTaskId()).orElse(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(task);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error creating task: " + e.getMessage());
        }
    }





    @GetMapping("/{taskId}")
    public ResponseEntity<MaintenanceTask> getTaskById(@PathVariable String taskId) {
        try {
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            return taskService.getTaskById(taskId)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with ID: " + taskId));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving task: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceTask>> getAllTasks() {
        try {
            List<MaintenanceTask> tasks = taskService.getAllTasks();
            return ResponseEntity.ok(tasks != null ? tasks : List.of());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving tasks: " + e.getMessage());
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<MaintenanceTask> updateTask(@PathVariable String taskId,
                                                      @RequestBody Map<String, Object> requestBody) {
        try {
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            validateRequestBody(requestBody, false);

            String description = (String) requestBody.get("description");
            MaintenanceTask.Priority priority = requestBody.containsKey("priority") ?
                    MaintenanceTask.Priority.valueOf(((String) requestBody.get("priority")).toUpperCase()) : null;

            Duration estimatedDuration = requestBody.containsKey("estimatedDurationMinutes") ?
                    Duration.ofMinutes(Long.parseLong(requestBody.get("estimatedDurationMinutes").toString())) : null;

            MaintenanceTask task = taskService.updateTask(taskId, description, priority, estimatedDuration);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request data: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error updating task: " + e.getMessage());
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskId) {
        try {
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            taskService.deleteTask(taskId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deleting task: " + e.getMessage());
        }
    }




    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<MaintenanceTask>> getTasksByStatus(@PathVariable String status) {
        try {
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            MaintenanceTask.Status statusEnum;
            try {
                statusEnum = MaintenanceTask.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid status: " + status + ". Valid statuses are: " +
                                Arrays.toString(MaintenanceTask.Status.values()));
            }

            List<MaintenanceTask> tasks = taskService.getTasksByStatus(statusEnum);
            return ResponseEntity.ok(tasks != null ? tasks : List.of());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving tasks by status: " + e.getMessage());
        }
    }

    @GetMapping("/by-statuses/{statuses}")
    public ResponseEntity<List<MaintenanceTask>> getTasksByStatuses(@PathVariable String statuses) {
        try {
            if (statuses == null || statuses.trim().isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<MaintenanceTask.Status> statusEnums;
            try {
                statusEnums = Arrays.stream(statuses.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> MaintenanceTask.Status.valueOf(s.toUpperCase()))
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid status in: " + statuses + ". Valid statuses are: " +
                                Arrays.toString(MaintenanceTask.Status.values()));
            }

            if (statusEnums.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<MaintenanceTask> tasks = taskService.getTasksByStatuses(statusEnums);
            return ResponseEntity.ok(tasks != null ? tasks : List.of());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving tasks by statuses: " + e.getMessage());
        }
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<MaintenanceTask> updateTaskStatus(@PathVariable String taskId,
                                                            @RequestBody Map<String, Object> requestBody) {
        try {
            logger.info("Updating task status for task: {} with body: {}", taskId, requestBody);

            // Validation des entrées de base
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            if (requestBody == null || !requestBody.containsKey("status")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
            }

            String statusStr = requestBody.get("status").toString();
            MaintenanceTask.Status newStatus = parseStatus(statusStr);

            logger.info("Parsed new status: {}", newStatus);

            // IMPORTANT: Récupérer la tâche AVANT la mise à jour pour comparer les statuts
            MaintenanceTask oldTask = taskService.getTaskById(taskId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

            MaintenanceTask.Status oldStatus = oldTask.getStatus();
            logger.info("Old status: {}, New status: {}", oldStatus, newStatus);

            // Récupérer le technicien depuis la requête ou depuis la tâche existante
            Staff technician = null;

            if (requestBody.containsKey("technicianId")) {
                try {
                    final Long technicianId = Long.parseLong(requestBody.get("technicianId").toString());
                    logger.info("Technician ID from request: {}", technicianId);

                    technician = staffRepository.findById(technicianId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Technician not found with ID: " + technicianId));
                    logger.info("Found technician: {} ({})", technician.getFullName(), technician.getEmail());
                } catch (NumberFormatException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid technician ID format");
                }
            } else if (oldTask.getAssignedTechnicianStaff() != null) {
                // Utiliser le technicien déjà assigné à la tâche
                technician = oldTask.getAssignedTechnicianStaff();
                logger.info("Using existing technician: {} (ID: {})",
                        technician.getFullName(), technician.getId());
            }

            // Mettre à jour le statut de la tâche
            MaintenanceTask updatedTask = taskService.updateTaskStatus(taskId, newStatus);
            logger.info("Task status updated successfully");

            // MODIFICATION: Logique d'envoi d'email spécifiquement pour les ADMINS
            boolean shouldSendEmailToAdmins = false;
            String emailAction = null;

            // Cas 1: ASSIGNED -> IN_PROGRESS (Acceptation) - NOTIFIER LES ADMINS
            if (oldStatus == MaintenanceTask.Status.ASSIGNED &&
                    newStatus == MaintenanceTask.Status.IN_PROGRESS) {
                shouldSendEmailToAdmins = true;
                emailAction = "ACCEPTED";
                logger.info("Task accepted - will notify all admins");
            }
            // Cas 2: ASSIGNED -> PLANNED (Rejet) - NOTIFIER LES ADMINS
            else if (oldStatus == MaintenanceTask.Status.ASSIGNED &&
                    newStatus == MaintenanceTask.Status.PLANNED) {
                shouldSendEmailToAdmins = true;
                emailAction = "REJECTED";
                logger.info("Task rejected - will notify all admins");
            }
            // Cas 3: IN_PROGRESS/ON_HOLD -> COMPLETED (Achèvement) - NOTIFIER LES ADMINS
            else if ((oldStatus == MaintenanceTask.Status.IN_PROGRESS ||
                    oldStatus == MaintenanceTask.Status.ON_HOLD) &&
                    newStatus == MaintenanceTask.Status.COMPLETED) {
                shouldSendEmailToAdmins = true;
                emailAction = "COMPLETED";
                logger.info("Task completed - will notify all admins");
            }

            // Envoyer l'email aux ADMINS si nécessaire
            if (shouldSendEmailToAdmins && technician != null) {
                logger.info("Sending {} notification email to all admins for task: {}", emailAction, taskId);
                try {
                    sendTaskStatusChangeEmail(updatedTask, technician, emailAction);
                    logger.info("Admin notification emails sent successfully");
                } catch (Exception emailError) {
                    logger.error("Failed to send admin notification emails: {}", emailError.getMessage(), emailError);
                    // Continue without failing the request
                }
            } else {
                logger.info("No admin email notification needed. ShouldSend: {}, Technician: {}",
                        shouldSendEmailToAdmins, technician != null ? "present" : "null");
            }

            return ResponseEntity.ok(updatedTask);

        } catch (ResponseStatusException e) {
            logger.error("ResponseStatusException in updateTaskStatus: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error in updateTaskStatus: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error updating task status: " + e.getMessage());
        }
    }











    @PatchMapping("/{taskId}/technician")
    public ResponseEntity<MaintenanceTask> assignTechnician(@PathVariable String taskId,
                                                            @RequestBody Map<String, Object> requestBody) {
        try {
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            if (requestBody == null || !requestBody.containsKey("technicianId")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Technician ID is required");
            }

            Object technicianIdObj = requestBody.get("technicianId");
            if (technicianIdObj == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Technician ID cannot be null");
            }

            Long technicianId;
            try {
                technicianId = Long.parseLong(technicianIdObj.toString());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid technician ID format");
            }

            MaintenanceTask task = taskService.assignStaffTechnician(taskId, technicianId);

            // AJOUT: Envoyer email de notification
            Staff technician = staffRepository.findById(technicianId).orElse(null);
            if (technician != null) {
                sendTaskAssignmentEmail(task, technician);
            }

            return ResponseEntity.ok(task);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error assigning technician: " + e.getMessage());
        }
    }


    @PatchMapping("/{taskId}/completion")
    public ResponseEntity<MaintenanceTask> updateCompletionPercentage(@PathVariable String taskId,
                                                                      @RequestBody Map<String, Object> requestBody) {
        try {
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            if (requestBody == null || !requestBody.containsKey("completionPercentage")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completion percentage is required");
            }

            double completionPercentage;
            try {
                completionPercentage = Double.parseDouble(requestBody.get("completionPercentage").toString());
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid completion percentage format");
            }

            if (completionPercentage < 0 || completionPercentage > 100) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Completion percentage must be between 0 and 100");
            }

            MaintenanceTask task = taskService.updateCompletionPercentage(taskId, completionPercentage);

            // CORRECTION: Vérification avec Double.compare ou >= 100.0
            if (completionPercentage >= 100.0 && requestBody.containsKey("technicianId")) {
                Long technicianId = Long.parseLong(requestBody.get("technicianId").toString());
                Staff technician = staffRepository.findById(technicianId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Technician not found"));

                sendTaskStatusChangeEmail(task, technician, "COMPLETED");
            }

            return ResponseEntity.ok(task);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error updating completion percentage: " + e.getMessage());
        }
    }
    @PostMapping("/{taskId}/skills")
    public ResponseEntity<MaintenanceTask> addRequiredSkill(@PathVariable String taskId,
                                                            @RequestBody Map<String, String> requestBody) {
        try {
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            if (requestBody == null || !requestBody.containsKey("skill")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Skill is required");
            }

            String skill = requestBody.get("skill");
            if (skill == null || skill.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Skill cannot be empty");
            }

            MaintenanceTask task = taskService.addRequiredSkill(taskId, skill.trim());
            return ResponseEntity.ok(task);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error adding required skill: " + e.getMessage());
        }
    }

    @PostMapping("/{taskId}/parts")
    public ResponseEntity<MaintenanceTask> addRequiredPart(@PathVariable String taskId,
                                                           @RequestBody Map<String, String> requestBody) {
        try {
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            if (requestBody == null || !requestBody.containsKey("part")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Part is required");
            }

            String part = requestBody.get("part");
            if (part == null || part.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Part cannot be empty");
            }

            MaintenanceTask task = taskService.addRequiredPart(taskId, part.trim());
            return ResponseEntity.ok(task);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error adding required part: " + e.getMessage());
        }
    }

    @GetMapping("/by-priority/{priority}")
    public ResponseEntity<List<MaintenanceTask>> getTasksByPriority(@PathVariable String priority) {
        try {
            if (priority == null || priority.trim().isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            MaintenanceTask.Priority priorityEnum;
            try {
                priorityEnum = MaintenanceTask.Priority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid priority: " + priority + ". Valid priorities are: " +
                                Arrays.toString(MaintenanceTask.Priority.values()));
            }

            List<MaintenanceTask> tasks = taskService.getTasksByPriority(priorityEnum);
            return ResponseEntity.ok(tasks != null ? tasks : List.of());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving tasks by priority: " + e.getMessage());
        }
    }




    // TaskController.java - Remplacez la méthode getTasksByTechnician existante par celle-ci

    /**
     * Récupère toutes les tâches assignées à un technicien spécifique
     * Supporte à la fois les IDs numériques et les anciennes références string
     */
    @GetMapping("/by-technician/{technicianId}")
    public ResponseEntity<List<MaintenanceTask>> getTasksByTechnician(@PathVariable String technicianId) {
        try {
            if (technicianId == null || technicianId.trim().isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<MaintenanceTask> tasks = new ArrayList<>();

            // Essayer d'abord de parser comme Long (nouveau système)
            try {
                Long technicianIdLong = Long.parseLong(technicianId);

                // Vérifier que le technicien existe et a le bon rôle
                Staff technician = staffRepository.findById(technicianIdLong)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Technician not found with ID: " + technicianIdLong));

                if (technician.getRole() != StaffRole.TECHNICIAN) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Staff member with ID " + technicianIdLong + " is not a technician");
                }

                tasks = taskService.getTasksByTechnicianStaffId(technicianIdLong);

            } catch (NumberFormatException e) {
                // Fallback vers l'ancienne méthode pour la compatibilité
                tasks = taskService.getTasksByTechnician(technicianId);
            }

            // Trier les tâches par priorité et date
            tasks = tasks.stream()
                    .sorted((t1, t2) -> {
                        // D'abord par priorité (HIGH > MEDIUM > LOW)
                        int priorityCompare = comparePriority(t1.getPriority(), t2.getPriority());
                        if (priorityCompare != 0) return priorityCompare;

                        // Ensuite par statut (IN_PROGRESS > ASSIGNED > autres)
                        int statusCompare = compareStatus(t1.getStatus(), t2.getStatus());
                        if (statusCompare != 0) return statusCompare;

                        // Enfin par date de création (plus récent en premier)
                        if (t1.getCreationDate() != null && t2.getCreationDate() != null) {
                            return t2.getCreationDate().compareTo(t1.getCreationDate());
                        }
                        return 0;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(tasks != null ? tasks : List.of());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving tasks by technician: " + e.getMessage());
        }
    }




    @GetMapping("/available-for-scheduling")
    public ResponseEntity<List<MaintenanceTask>> getTasksAvailableForScheduling() {
        try {
            List<MaintenanceTask> tasks = taskService.getTasksByStatuses(
                    Arrays.asList(MaintenanceTask.Status.PLANNED, MaintenanceTask.Status.PENDING)
            );
            return ResponseEntity.ok(tasks != null ? tasks : List.of());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving tasks available for scheduling: " + e.getMessage());
        }
    }

/*    @PostMapping("/{taskId}/schedule")
    public ResponseEntity<Schedule> scheduleTask(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            if (!requestBody.containsKey("technicianId") ||
                    !requestBody.containsKey("startTime") ||
                    !requestBody.containsKey("endTime")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Technician ID, start time and end time are required");
            }

            Long technicianId = Long.parseLong(requestBody.get("technicianId").toString());
            LocalDateTime startTime = LocalDateTime.parse(requestBody.get("startTime").toString());
            LocalDateTime endTime = LocalDateTime.parse(requestBody.get("endTime").toString());

            MaintenanceTask task = taskService.getTaskById(taskId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Task not found with ID: " + taskId));

            // Update task status to SCHEDULED
            task.setStatus(MaintenanceTask.Status.SCHEDULED);
            taskService.updateTaskStatus(taskId, MaintenanceTask.Status.SCHEDULED);

            // Create schedule
            Schedule schedule = new Schedule();
            schedule.setTask(task);
            schedule.setTechnician(staffRepository.findById(technicianId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Technician not found with ID: " + technicianId)));
            schedule.setStartTime(startTime);
            schedule.setEndTime(endTime);
            schedule.setPriority(task.getPriority());
            schedule.setStatus(MaintenanceTask.Status.SCHEDULED);

            Schedule savedSchedule = scheduleRepository.save(schedule);

            return ResponseEntity.ok(savedSchedule);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error scheduling task: " + e.getMessage());
        }
    }*/
    /**
     * Nouveau endpoint spécifique pour les techniciens avec filtrage par statut
     */
    @GetMapping("/by-technician/{technicianId}/status/{status}")
    public ResponseEntity<List<MaintenanceTask>> getTasksByTechnicianAndStatus(
            @PathVariable String technicianId,
            @PathVariable String status) {
        try {
            if (technicianId == null || technicianId.trim().isEmpty() ||
                    status == null || status.trim().isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            Long technicianIdLong = Long.parseLong(technicianId);
            MaintenanceTask.Status statusEnum = MaintenanceTask.Status.valueOf(status.toUpperCase());

            List<MaintenanceTask> tasks = taskService.getTasksByTechnicianAndStatus(technicianIdLong, statusEnum);
            return ResponseEntity.ok(tasks != null ? tasks : List.of());

        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid technician ID format");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No enum constant")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid status: " + status + ". Valid statuses are: " +
                                Arrays.toString(MaintenanceTask.Status.values()));
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving tasks: " + e.getMessage());
        }
    }

    /**
     * Endpoint pour récupérer les statistiques des tâches d'un technicien
     */
    @GetMapping("/by-technician/{technicianId}/stats")
    public ResponseEntity<Map<String, Object>> getTechnicianTaskStats(@PathVariable String technicianId) {
        try {
            Long technicianIdLong = Long.parseLong(technicianId);

            List<MaintenanceTask> allTasks = taskService.getTasksByTechnicianStaffId(technicianIdLong);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTasks", allTasks.size());
            stats.put("assignedTasks", allTasks.stream().filter(t -> t.getStatus() == MaintenanceTask.Status.ASSIGNED).count());
            stats.put("inProgressTasks", allTasks.stream().filter(t -> t.getStatus() == MaintenanceTask.Status.IN_PROGRESS).count());
            stats.put("completedTasks", allTasks.stream().filter(t -> t.getStatus() == MaintenanceTask.Status.COMPLETED).count());
            stats.put("onHoldTasks", allTasks.stream().filter(t -> t.getStatus() == MaintenanceTask.Status.ON_HOLD).count());

            return ResponseEntity.ok(stats);

        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid technician ID format");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving technician stats: " + e.getMessage());
        }
    }

    // Méthodes utilitaires pour le tri
    private int comparePriority(MaintenanceTask.Priority p1, MaintenanceTask.Priority p2) {
        if (p1 == null && p2 == null) return 0;
        if (p1 == null) return 1;
        if (p2 == null) return -1;

        // HIGH = 0, MEDIUM = 1, LOW = 2 (ordre inverse pour tri descendant)
        int order1 = p1 == MaintenanceTask.Priority.HIGH ? 0 :
                p1 == MaintenanceTask.Priority.MEDIUM ? 1 : 2;
        int order2 = p2 == MaintenanceTask.Priority.HIGH ? 0 :
                p2 == MaintenanceTask.Priority.MEDIUM ? 1 : 2;

        return Integer.compare(order1, order2);
    }

    private int compareStatus(MaintenanceTask.Status s1, MaintenanceTask.Status s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return 1;
        if (s2 == null) return -1;

        // IN_PROGRESS = 0, ASSIGNED = 1, autres = 2
        int order1 = s1 == MaintenanceTask.Status.IN_PROGRESS ? 0 :
                s1 == MaintenanceTask.Status.ASSIGNED ? 1 : 2;
        int order2 = s2 == MaintenanceTask.Status.IN_PROGRESS ? 0 :
                s2 == MaintenanceTask.Status.ASSIGNED ? 1 : 2;

        return Integer.compare(order1, order2);
    }
    @GetMapping("/by-skill")
    public ResponseEntity<List<MaintenanceTask>> getTasksByRequiredSkill(@RequestParam String skill) {
        try {
            if (skill == null || skill.trim().isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<MaintenanceTask> tasks = taskService.getTasksByRequiredSkill(skill.trim());
            return ResponseEntity.ok(tasks != null ? tasks : List.of());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving tasks by required skill: " + e.getMessage());
        }
    }

    @GetMapping("/by-part")
    public ResponseEntity<List<MaintenanceTask>> getTasksByRequiredPart(@RequestParam String part) {
        try {
            if (part == null || part.trim().isEmpty()) {
                return ResponseEntity.ok(List.of());
            }

            List<MaintenanceTask> tasks = taskService.getTasksByRequiredPart(part.trim());
            return ResponseEntity.ok(tasks != null ? tasks : List.of());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving tasks by required part: " + e.getMessage());
        }
    }


    @PatchMapping("/{taskId}/equipment")
    public ResponseEntity<MaintenanceTask> assignEquipment(@PathVariable String taskId,
                                                           @RequestBody Map<String, Object> requestBody) {
        try {
            // Validate task ID
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            // Validate request body
            if (requestBody == null || !requestBody.containsKey("equipmentId")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment ID is required");
            }

            // Parse equipment ID
            Long equipmentId;
            try {
                equipmentId = Long.parseLong(requestBody.get("equipmentId").toString());
                if (equipmentId <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Equipment ID must be positive");
                }
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid equipment ID format");
            }

            // Verify equipment exists
            Equipment equipment = equipmentRepository.findById(equipmentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Equipment not found with ID: " + equipmentId));

            // Assign equipment
            MaintenanceTask task = taskService.assignEquipment(taskId, equipmentId);

            // Ensure equipment is loaded in the response
            task.setRelatedEquipment(equipment);

            return ResponseEntity.ok(task);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error assigning equipment: " + e.getMessage());
        }
    }



    @PatchMapping("/{taskId}/staff-technician")
    public ResponseEntity<MaintenanceTask> assignStaffTechnician(@PathVariable String taskId,
                                                                 @RequestBody(required = false) Map<String, Object> requestBody) {
        try {
            if (taskId == null || taskId.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task ID cannot be empty");
            }

            // Si le corps de la requête est vide ou null, c'est une désassignation
            if (requestBody == null || requestBody.isEmpty()) {
                MaintenanceTask task = taskService.unassignTechnician(taskId);
                return ResponseEntity.ok(task);
            }

            // Sinon, c'est une assignation normale
            if (!requestBody.containsKey("technicianId")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Technician ID is required for assignment");
            }

            Object technicianIdObj = requestBody.get("technicianId");
            if (technicianIdObj == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Technician ID cannot be null");
            }

            Long technicianId;
            try {
                if (technicianIdObj instanceof String) {
                    String technicianIdStr = (String) technicianIdObj;
                    if (technicianIdStr.trim().isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Technician ID cannot be empty");
                    }
                    technicianId = Long.parseLong(technicianIdStr.trim());
                } else if (technicianIdObj instanceof Number) {
                    technicianId = ((Number) technicianIdObj).longValue();
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid technician ID format: expected number or string");
                }

                if (technicianId <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Technician ID must be positive");
                }

            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid technician ID format: " + technicianIdObj);
            }

            MaintenanceTask task = taskService.assignStaffTechnician(taskId, technicianId);

            // AJOUT: Envoyer email de notification
            Staff technician = staffRepository.findById(technicianId).orElse(null);
            if (technician != null) {
                sendTaskAssignmentEmail(task, technician);
            }

            return ResponseEntity.ok(task);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error assigning technician: " + e.getMessage());
        }
    }




    private void sendTaskStatusChangeEmail(MaintenanceTask task, Staff technician, String action) {
        try {
            logger.info("Starting to send email notification for action: {}", action);

            // Récupérer tous les ADMINS avec validation
            List<Staff> admins = staffRepository.findByRole(StaffRole.ADMIN);
            logger.info("Found {} admins in database", admins.size());

            if (admins.isEmpty()) {
                logger.warn("No admins found to notify about task status change");
                return;
            }

            // Filtrer les admins avec des emails valides
            List<Staff> validAdmins = admins.stream()
                    .filter(admin -> admin.getEmail() != null && !admin.getEmail().trim().isEmpty())
                    .collect(Collectors.toList());

            logger.info("Found {} admins with valid emails", validAdmins.size());

            if (validAdmins.isEmpty()) {
                logger.warn("No admins with valid email addresses found");
                return;
            }

            String subject = String.format("Task %s - %s (%s)",
                    action,
                    task.getTaskId(),
                    technician.getFullName());

            logger.info("Email subject: {}", subject);

            // Envoyer l'email à chaque admin
            int successCount = 0;
            int failureCount = 0;

            for (Staff admin : validAdmins) {
                try {
                    logger.info("Sending email to admin: {} ({})", admin.getFullName(), admin.getEmail());

                    String body = buildAdminNotificationEmailBody(task, technician, admin, action);
                    emailService.sendEmail(admin.getEmail(), subject, body);

                    successCount++;
                    logger.info("Email sent successfully to admin: {}", admin.getEmail());

                } catch (Exception e) {
                    failureCount++;
                    logger.error("Failed to send email to admin {}: {}",
                            admin.getEmail(), e.getMessage(), e);
                }
            }

            logger.info("Email sending completed. Success: {}, Failures: {}", successCount, failureCount);

        } catch (Exception e) {
            logger.error("Critical error in sendTaskStatusChangeEmail: {}", e.getMessage(), e);
        }
    }






    private String buildAdminNotificationEmailBody(MaintenanceTask task, Staff technician,
                                                   Staff admin, String action) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
            StringBuilder body = new StringBuilder();

            // En-tête personnalisé pour admin
            body.append("Dear ").append(admin.getFirstName() != null ? admin.getFirstName() : "Administrator").append(",\n\n");

            // Message principal selon l'action
            switch (action) {
                case "ACCEPTED":
                    body.append("✅ Technician ").append(technician.getFullName())
                            .append(" has ACCEPTED the following maintenance task:\n\n");
                    break;
                case "REJECTED":
                    body.append("❌ Technician ").append(technician.getFullName())
                            .append(" has REJECTED the following maintenance task:\n\n");
                    break;
                case "COMPLETED":
                    body.append("🎉 Technician ").append(technician.getFullName())
                            .append(" has COMPLETED the following maintenance task:\n\n");
                    break;
                default:
                    body.append("Technician ").append(technician.getFullName())
                            .append(" has updated the following maintenance task:\n\n");
            }

            // Détails de la tâche
            body.append("TASK DETAILS:\n");
            body.append("═══════════════\n");
            body.append("• Task ID: ").append(task.getTaskId()).append("\n");
            body.append("• Description: ").append(task.getDescription() != null ? task.getDescription() : "N/A").append("\n");
            body.append("• Priority: ").append(task.getPriority() != null ? task.getPriority() : "N/A").append("\n");
            body.append("• Current Status: ").append(task.getStatus() != null ? task.getStatus() : "N/A").append("\n");
            body.append("• Action Date: ").append(LocalDateTime.now().format(formatter)).append("\n");

            if (task.getDueDate() != null) {
                body.append("• Due Date: ").append(task.getDueDate().format(formatter)).append("\n");
            }

            if (task.getCompletionPercentage() > 0) {
                body.append("• Completion: ").append(String.format("%.1f%%", task.getCompletionPercentage())).append("\n");
            }

            body.append("\n");

            // Information du technicien
            body.append("TECHNICIAN INFORMATION:\n");
            body.append("══════════════════════\n");
            body.append("• Name: ").append(technician.getFullName()).append("\n");
            body.append("• Email: ").append(technician.getEmail() != null ? technician.getEmail() : "N/A").append("\n");
            body.append("• Phone: ").append(technician.getPhoneNumber() != null ? technician.getPhoneNumber() : "N/A").append("\n");
            body.append("• Staff ID: ").append(technician.getId()).append("\n\n");

            // Information sur l'équipement si disponible
            if (task.getRelatedEquipment() != null) {
                body.append("EQUIPMENT INFORMATION:\n");
                body.append("═══════════════════\n");
                body.append("• Name: ").append(task.getRelatedEquipment().getName() != null ?
                        task.getRelatedEquipment().getName() : "N/A").append("\n");
                body.append("• Model: ").append(task.getRelatedEquipment().getModel() != null ?
                        task.getRelatedEquipment().getModel() : "N/A").append("\n");
                body.append("• Location: ").append(task.getRelatedEquipment().getLocation() != null ?
                        task.getRelatedEquipment().getLocation() : "N/A").append("\n");
                body.append("• Serial Number: ").append(task.getRelatedEquipment().getSerialNumber() != null ?
                        task.getRelatedEquipment().getSerialNumber() : "N/A").append("\n\n");
            }

            // Compétences requises si disponibles
            if (task.getRequiredSkills() != null && !task.getRequiredSkills().isEmpty()) {
                body.append("REQUIRED SKILLS:\n");
                body.append("══════════════\n");
                for (String skill : task.getRequiredSkills()) {
                    body.append("• ").append(skill).append("\n");
                }
                body.append("\n");
            }

            // Pièces requises si disponibles
            if (task.getRequiredParts() != null && !task.getRequiredParts().isEmpty()) {
                body.append("REQUIRED PARTS:\n");
                body.append("═════════════\n");
                for (String part : task.getRequiredParts()) {
                    body.append("• ").append(part).append("\n");
                }
                body.append("\n");
            }

            // Actions recommandées selon le contexte
            body.append("ADMINISTRATIVE ACTION REQUIRED:\n");
            body.append("═══════════════════════════\n");
            switch (action) {
                case "ACCEPTED":
                    body.append("• ✅ The technician has started working on this task\n");
                    body.append("• 📊 Monitor progress in the maintenance system\n");
                    body.append("• 📞 Contact the technician if you need updates\n");
                    body.append("• 🔍 Review task progress periodically\n");
                    break;
                case "REJECTED":
                    body.append("• ⚠️  URGENT: This task needs to be reassigned to another technician\n");
                    body.append("• 📞 Contact the technician for rejection details\n");
                    body.append("• 🔄 Review task requirements and reassign immediately\n");
                    body.append("• 📝 Update task priority if necessary\n");
                    break;
                case "COMPLETED":
                    body.append("• ✅ Task has been completed successfully\n");
                    body.append("• 🔍 Review the work and approve task closure\n");
                    body.append("• 📝 Update maintenance records and system\n");
                    body.append("• 📊 Add completion data to analytics\n");
                    break;
                default:
                    body.append("• 🔍 Review the task status in the system\n");
                    body.append("• 📞 Contact the technician if needed\n");
            }

            body.append("\n🌐 You can access the complete maintenance management system to view all task details and take administrative actions.\n\n");

            // Message spécial pour les admins
            body.append("As an administrator, you have full access to:\n");
            body.append("• Task reassignment and priority management\n");
            body.append("• Technician performance monitoring\n");
            body.append("• System analytics and reporting\n");
            body.append("• Equipment and maintenance scheduling\n\n");

            // Signature
            body.append("Best regards,\n");
            body.append("Maintenance Management System\n");
            body.append("📧 Administrative Notification Service\n");
            body.append("🕒 Sent on: ").append(LocalDateTime.now().format(formatter));

            return body.toString();

        } catch (Exception e) {
            logger.error("Error building admin email body: {}", e.getMessage(), e);
            return "Error generating email content. Please check the maintenance system for task details.";
        }
    }




    // Méthode utilitaire pour parser le statut avec validation
    private MaintenanceTask.Status parseStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status cannot be empty");
        }
        try {
            return MaintenanceTask.Status.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid status value: " + statusStr + ". Valid statuses are: " +
                            Arrays.toString(MaintenanceTask.Status.values()));
        }
    }














    private void validateRequestBody(Map<String, Object> requestBody, boolean isCreate) {
        if (requestBody == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        if (isCreate) {
            if (!requestBody.containsKey("taskId") ||
                    requestBody.get("taskId") == null ||
                    requestBody.get("taskId").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Task ID is required and cannot be empty");
            }

            if (!requestBody.containsKey("description") ||
                    requestBody.get("description") == null ||
                    requestBody.get("description").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Description is required and cannot be empty");
            }

            if (!requestBody.containsKey("priority") ||
                    requestBody.get("priority") == null ||
                    requestBody.get("priority").toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Priority is required and cannot be empty");
            }

            if (!requestBody.containsKey("estimatedDurationMinutes") ||
                    requestBody.get("estimatedDurationMinutes") == null) {
                throw new IllegalArgumentException("Estimated duration is required");
            }

            try {
                String priorityStr = requestBody.get("priority").toString();
                MaintenanceTask.Priority.valueOf(priorityStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid priority value. Valid priorities are: " +
                        Arrays.toString(MaintenanceTask.Priority.values()));
            }

            try {
                long duration = Long.parseLong(requestBody.get("estimatedDurationMinutes").toString());
                if (duration <= 0) {
                    throw new IllegalArgumentException("Estimated duration must be greater than 0");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid estimated duration format");
            }
        }
    }
}