package IMAS.ImasProject.services;


import IMAS.ImasProject.model.Equipment;
import IMAS.ImasProject.model.MaintenanceTask;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.StaffRole;
import IMAS.ImasProject.repository.EquipmentRepository;
import IMAS.ImasProject.repository.MaintenanceTaskRepository;
import IMAS.ImasProject.repository.StaffRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MaintenanceTaskService {

    @Autowired
    private MaintenanceTaskRepository taskRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private StaffRepository staffRepository;

    public MaintenanceTask createTask(String taskId, String description,
                                      MaintenanceTask.Priority priority, Duration estimatedDuration) {
        if (taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("Task with ID " + taskId + " already exists");
        }

        MaintenanceTask task = new MaintenanceTask();
        task.setTaskId(taskId);
        task.setDescription(description);
        task.setPriority(priority);
        task.setEstimatedDurationMinutes(estimatedDuration.toMinutes());
        task.setStatus(MaintenanceTask.Status.PLANNED);
        task.setCompletionPercentage(0.0);
        task.setCreationDate(LocalDateTime.now());
        task.setLastUpdated(LocalDateTime.now());

        // Initialize lists to avoid NullPointerException
        if (task.getRequiredSkills() == null) {
            task.setRequiredSkills(new ArrayList<>());
        }
        if (task.getRequiredParts() == null) {
            task.setRequiredParts(new ArrayList<>());
        }

        return taskRepository.save(task);
    }

    public Optional<MaintenanceTask> getTaskById(String taskId) {
        return taskRepository.findById(taskId)
                .map(task -> {
                    // Force le chargement des relations si elles sont nécessaires
                    if (task.getRelatedEquipment() != null) {
                        task.getRelatedEquipment().getEquipmentId(); // Force le chargement
                    }
                    if (task.getAssignedTechnicianStaff() != null) {
                        task.getAssignedTechnicianStaff().getId(); // Force le chargement
                    }
                    return task;
                });
    }

    public List<MaintenanceTask> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<MaintenanceTask> getTasksByStatus(MaintenanceTask.Status status) {
        return taskRepository.findByStatus(status);
    }

    public List<MaintenanceTask> getTasksByStatuses(List<MaintenanceTask.Status> statuses) {
        return taskRepository.findByStatusIn(statuses);
    }

    public List<MaintenanceTask> getTasksByPriority(MaintenanceTask.Priority priority) {
        return taskRepository.findByPriority(priority);
    }

    // FIXED: Changed parameter type to Long and updated method name
    public List<MaintenanceTask> getTasksByTechnician(String technicianId) {
        try {
            Long id = Long.parseLong(technicianId);
            return taskRepository.findByAssignedTechnician(id);
        } catch (NumberFormatException e) {
            // Handle invalid ID format
            return new ArrayList<>();
        }
    }



    // TaskService.java - Ajoutez ces méthodes à votre TaskService

    /**
     * Récupère toutes les tâches assignées à un technicien spécifique par son ID de staff
     */
    public List<MaintenanceTask> getTasksByTechnicianStaffId(Long technicianId) {
        try {
            // Vérifier que le technicien existe
            Staff technician = staffRepository.findById(technicianId)
                    .orElseThrow(() -> new IllegalArgumentException("Technician not found with ID: " + technicianId));

            // Vérifier que c'est bien un technicien
            if (technician.getRole() != StaffRole.TECHNICIAN) {
                throw new IllegalArgumentException("Staff member is not a technician: " + technicianId);
            }

            // Récupérer les tâches assignées à ce technicien
            List<MaintenanceTask> tasks = taskRepository.findByAssignedTechnicianId(technicianId);

            // Charger les relations nécessaires
            for (MaintenanceTask task : tasks) {
                loadTaskRelations(task);
            }

            return tasks;
        } catch (Exception e) {
            System.err.println("Error getting tasks for technician " + technicianId + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Méthode helper pour charger les relations d'une tâche
     */
    private void loadTaskRelations(MaintenanceTask task) {
        try {
            // Charger l'équipement associé
            if (task.getRelatedEquipment() != null) {
                Long equipmentId = task.getRelatedEquipment().getEquipmentId();
                Equipment equipment = equipmentRepository.findById(equipmentId).orElse(null);
                task.setRelatedEquipment(equipment);
            }

            // Charger le technicien assigné
            if (task.getAssignedTechnician() != null) {
                Long technicianId = task.getAssignedTechnician(); // This returns Long
                Staff technician = staffRepository.findById(technicianId).orElse(null);
                // Use the correct setter method for the Staff object
                task.setAssignedTechnicianStaff(technician); // ✅ Correct method
            }
        } catch (Exception e) {
            System.err.println("Error loading task relations: " + e.getMessage());
        }
    }

    /**
     * Récupère les tâches par statut pour un technicien spécifique
     */
    public List<MaintenanceTask> getTasksByTechnicianAndStatus(Long technicianId, MaintenanceTask.Status status) {
        try {
            List<MaintenanceTask> allTasks = getTasksByTechnicianStaffId(technicianId);
            return allTasks.stream()
                    .filter(task -> task.getStatus() == status)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting tasks by technician and status: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Récupère les tâches par plusieurs statuts pour un technicien spécifique
     */
    public List<MaintenanceTask> getTasksByTechnicianAndStatuses(Long technicianId, List<MaintenanceTask.Status> statuses) {
        try {
            List<MaintenanceTask> allTasks = getTasksByTechnicianStaffId(technicianId);
            return allTasks.stream()
                    .filter(task -> statuses.contains(task.getStatus()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting tasks by technician and statuses: " + e.getMessage());
            return new ArrayList<>();
        }
    }

/*    public List<MaintenanceTask> getTasksByTechnicianStaffId(Long technicianStaffId) {
        return taskRepository.findByAssignedTechnicianStaff_Id(technicianStaffId);
    }*/

    public List<MaintenanceTask> getTasksByRequiredSkill(String skill) {
        return taskRepository.findByRequiredSkillsContaining(skill);
    }

    public List<MaintenanceTask> getTasksByRequiredPart(String part) {
        return taskRepository.findByRequiredPartsContaining(part);
    }

    public MaintenanceTask updateTask(String taskId, String description,
                                      MaintenanceTask.Priority priority, Duration estimatedDuration) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (description != null && !description.trim().isEmpty()) {
            task.setDescription(description);
        }
        if (priority != null) {
            task.setPriority(priority);
        }
        if (estimatedDuration != null) {
            task.setEstimatedDurationMinutes(estimatedDuration.toMinutes());
        }

        task.setLastUpdated(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public MaintenanceTask updateTaskStatus(String taskId, MaintenanceTask.Status status) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        task.setStatus(status);
        task.setLastUpdated(LocalDateTime.now());

        // If task is completed, update completion date
        if (status == MaintenanceTask.Status.COMPLETED) {
            task.setCompletionDate(LocalDateTime.now());
            if (task.getCompletionPercentage() < 100) {
                task.setCompletionPercentage(100.0);
            }
        }

        return taskRepository.save(task);
    }

    /**
     * DEPRECATED: Assign technician by String ID (legacy method)
     * Use assignStaffTechnician(String, Long) instead
     */
    @Deprecated
    public MaintenanceTask assignTechnician(String taskId, String technicianIdStr) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (technicianIdStr != null && !technicianIdStr.trim().isEmpty()) {
            try {
                Long technicianId = Long.parseLong(technicianIdStr.trim());
                task.setAssignedTechnician(technicianId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid technician ID format: " + technicianIdStr);
            }
        } else {
            task.setAssignedTechnician(null);
        }

        task.setLastUpdated(LocalDateTime.now());
        return taskRepository.save(task);
    }

    /**
     * Assign Staff technician by Staff ID (recommended method)
     */


// In MaintenanceTaskService.java
    public MaintenanceTask assignStaffTechnician(String taskId, Long technicianStaffId) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (technicianStaffId != null) {
            Staff technician = staffRepository.findById(technicianStaffId)
                    .orElseThrow(() -> new IllegalArgumentException("Staff not found with ID: " + technicianStaffId));

            task.setAssignedTechnicianStaff(technician);
            task.setAssignedTechnician(technician.getId()); // Make sure this is set
        } else {
            task.setAssignedTechnicianStaff(null);
            task.setAssignedTechnician(null);
        }

        task.setLastUpdated(LocalDateTime.now());
        return taskRepository.save(task);
    }

    /**
     * Remove technician assignment
     */
    public MaintenanceTask unassignTechnician(String taskId) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        task.setAssignedTechnicianStaff(null);
        task.setAssignedTechnician(null);
        task.setStatus(MaintenanceTask.Status.PLANNED); // Optionnel: remettre le statut à PLANNED
        task.setLastUpdated(LocalDateTime.now());

        return taskRepository.save(task);
    }



    @Transactional
    public MaintenanceTask assignEquipment(String taskId, Long equipmentId) {
        // Validate inputs
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be empty");
        }
        if (equipmentId == null || equipmentId <= 0) {
            throw new IllegalArgumentException("Invalid equipment ID");
        }

        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found with ID: " + equipmentId));

        // Ensure equipment is loaded
        Hibernate.initialize(equipment);

        task.setRelatedEquipment(equipment);
        task.setLastUpdated(LocalDateTime.now());

        return taskRepository.save(task);
    }

    public MaintenanceTask updateCompletionPercentage(String taskId, double completionPercentage) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (completionPercentage < 0 || completionPercentage > 100) {
            throw new IllegalArgumentException("Completion percentage must be between 0 and 100");
        }

        task.setCompletionPercentage(completionPercentage);
        task.setLastUpdated(LocalDateTime.now());

        // If task reaches 100%, mark as completed
        if (completionPercentage == 100 && task.getStatus() != MaintenanceTask.Status.COMPLETED) {
            task.setStatus(MaintenanceTask.Status.COMPLETED);
            task.setCompletionDate(LocalDateTime.now());
        }

        return taskRepository.save(task);
    }

    public MaintenanceTask addRequiredSkill(String taskId, String skill) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (skill == null || skill.trim().isEmpty()) {
            throw new IllegalArgumentException("Skill cannot be empty");
        }

        if (task.getRequiredSkills() == null) {
            task.setRequiredSkills(new ArrayList<>());
        }

        String trimmedSkill = skill.trim();
        if (!task.getRequiredSkills().contains(trimmedSkill)) {
            task.getRequiredSkills().add(trimmedSkill);
            task.setLastUpdated(LocalDateTime.now());
            return taskRepository.save(task);
        }

        return task;
    }

    public MaintenanceTask removeRequiredSkill(String taskId, String skill) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (skill != null && task.getRequiredSkills() != null) {
            task.getRequiredSkills().remove(skill.trim());
            task.setLastUpdated(LocalDateTime.now());
            return taskRepository.save(task);
        }

        return task;
    }

    public MaintenanceTask addRequiredPart(String taskId, String part) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (part == null || part.trim().isEmpty()) {
            throw new IllegalArgumentException("Part cannot be empty");
        }

        if (task.getRequiredParts() == null) {
            task.setRequiredParts(new ArrayList<>());
        }

        String trimmedPart = part.trim();
        if (!task.getRequiredParts().contains(trimmedPart)) {
            task.getRequiredParts().add(trimmedPart);
            task.setLastUpdated(LocalDateTime.now());
            return taskRepository.save(task);
        }

        return task;
    }

    public MaintenanceTask removeRequiredPart(String taskId, String part) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        if (part != null && task.getRequiredParts() != null) {
            task.getRequiredParts().remove(part.trim());
            task.setLastUpdated(LocalDateTime.now());
            return taskRepository.save(task);
        }

        return task;
    }

    public MaintenanceTask setDueDate(String taskId, LocalDateTime dueDate) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        task.setDueDate(dueDate);
        task.setLastUpdated(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public List<MaintenanceTask> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return taskRepository.findByDueDateBeforeAndStatusNot(now, MaintenanceTask.Status.COMPLETED);
    }

    public List<MaintenanceTask> getTasksDueWithin(Duration duration) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plus(duration);
        return taskRepository.findByDueDateBetweenAndStatusNot(now, deadline, MaintenanceTask.Status.COMPLETED);
    }

    public void deleteTask(String taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new IllegalArgumentException("Task not found with ID: " + taskId);
        }
        taskRepository.deleteById(taskId);
    }

    // Utility methods
    public boolean isTaskOverdue(String taskId) {
        MaintenanceTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        return task.getDueDate() != null &&
                task.getDueDate().isBefore(LocalDateTime.now()) &&
                task.getStatus() != MaintenanceTask.Status.COMPLETED;
    }

    public long countTasksByStatus(MaintenanceTask.Status status) {
        return taskRepository.countByStatus(status);
    }

    // FIXED: Changed parameter type to Long
    public long countTasksByTechnician(Long technicianId) {
        return taskRepository.countByAssignedTechnician(technicianId);
    }

    public long countTasksByTechnicianStaff(Long technicianStaffId) {
        return taskRepository.findByAssignedTechnicianStaff_Id(technicianStaffId).size();
    }

    // Additional utility methods for better task management

    public List<MaintenanceTask> getTasksByEquipment(Long equipmentId) {
        return taskRepository.findByRelatedEquipment_EquipmentId(equipmentId);
    }

    /**
     * Get unassigned tasks that require a specific skill
     */
    public List<MaintenanceTask> getUnassignedTasksRequiringSkill(String skill) {
        return taskRepository.findUnassignedTasksRequiringSkill(skill);
    }

    /**
     * Get tasks by multiple statuses and priority
     */
    public List<MaintenanceTask> getTasksByStatusesAndPriority(List<MaintenanceTask.Status> statuses,
                                                               MaintenanceTask.Priority priority) {
        return taskRepository.findByStatusInAndPriority(statuses, priority);
    }

    /**
     * Get tasks by completion percentage range
     */
    public List<MaintenanceTask> getTasksByCompletionRange(double minPercentage, double maxPercentage) {
        return taskRepository.findByCompletionPercentageBetween(minPercentage, maxPercentage);
    }

    /**
     * Get partially completed tasks (between 1% and 99%)
     */
    public List<MaintenanceTask> getPartiallyCompletedTasks() {
        return taskRepository.findByCompletionPercentageBetween(1.0, 99.0);
    }

    /**
     * Get not started tasks (0% completion)
     */
    public List<MaintenanceTask> getNotStartedTasks() {
        return taskRepository.findByCompletionPercentageBetween(0.0, 0.0);
    }

    /**
     * Bulk assign technician to multiple tasks
     */
    @Transactional
    public List<MaintenanceTask> bulkAssignStaffTechnician(List<String> taskIds, Long technicianStaffId) {
        List<MaintenanceTask> updatedTasks = new ArrayList<>();

        for (String taskId : taskIds) {
            try {
                MaintenanceTask task = assignStaffTechnician(taskId, technicianStaffId);
                updatedTasks.add(task);
            } catch (IllegalArgumentException e) {
                // Log the error but continue with other tasks
                System.err.println("Failed to assign technician to task " + taskId + ": " + e.getMessage());
            }
        }

        return updatedTasks;
    }

    /**
     * Bulk update task status
     */
    @Transactional
    public List<MaintenanceTask> bulkUpdateTaskStatus(List<String> taskIds, MaintenanceTask.Status status) {
        List<MaintenanceTask> updatedTasks = new ArrayList<>();

        for (String taskId : taskIds) {
            try {
                MaintenanceTask task = updateTaskStatus(taskId, status);
                updatedTasks.add(task);
            } catch (IllegalArgumentException e) {
                // Log the error but continue with other tasks
                System.err.println("Failed to update status for task " + taskId + ": " + e.getMessage());
            }
        }

        return updatedTasks;
    }
}