package IMAS.ImasProject.services;


import IMAS.ImasProject.dto.TaskAssignmentDTO;
import IMAS.ImasProject.model.MaintenanceTask;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.TaskAssignment;
import IMAS.ImasProject.repository.MaintenanceTaskRepository;
import IMAS.ImasProject.repository.StaffRepository;
import IMAS.ImasProject.repository.TaskAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AssignmentService {

    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private MaintenanceTaskRepository maintenanceTaskRepository;

    @Autowired
    private StaffRepository staffRepository;

    /**
     * Automatically assigns a task to the most suitable technician
     */
    public TaskAssignment assignTaskAutomatically(String taskId) {
        MaintenanceTask task = maintenanceTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        // Find available technician with matching skills and lowest workload
        Staff technician = findBestAvailableTechnician(task);

        if (technician == null) {
            throw new RuntimeException("No available technician found for task: " + taskId);
        }

        TaskAssignment assignment = new TaskAssignment(task, technician, TaskAssignment.AssignmentMethod.AUTOMATIC);
        return taskAssignmentRepository.save(assignment);
    }

    /**
     * Manually assigns a task to a specific technician
     */
    public TaskAssignment assignTaskManually(TaskAssignmentDTO assignmentDTO) {
        MaintenanceTask task = maintenanceTaskRepository.findById(assignmentDTO.getTaskId())
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + assignmentDTO.getTaskId()));

        Staff technician = staffRepository.findById(assignmentDTO.getTechnicianId())
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + assignmentDTO.getTechnicianId()));

        // Check if technician is available
        if (!isTechnicianAvailable(technician.getId())) {
            throw new RuntimeException("Technician is not available for assignment");
        }

        TaskAssignment assignment = new TaskAssignment(task, technician, TaskAssignment.AssignmentMethod.MANUAL);

        // Set additional fields from DTO if provided
        if (assignmentDTO.getAssignmentMethod() != null) {
            assignment.setAssignmentMethod(assignmentDTO.getAssignmentMethod());
        }

        return taskAssignmentRepository.save(assignment);
    }

    /**
     * Technician responds to an assignment (accept/reject)
     */
    public TaskAssignment respondToAssignment(Long assignmentId, boolean accept, String reason) {
        TaskAssignment assignment = taskAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        if (assignment.getStatus() != TaskAssignment.AssignmentStatus.PENDING_ACCEPTANCE) {
            throw new RuntimeException("Assignment is not in pending state");
        }

        assignment.setRespondedAt(LocalDateTime.now());

        if (accept) {
            assignment.setStatus(TaskAssignment.AssignmentStatus.ACCEPTED);
        } else {
            assignment.setStatus(TaskAssignment.AssignmentStatus.REJECTED);
            assignment.setRejectionReason(reason);
        }

        return taskAssignmentRepository.save(assignment);
    }

    /**
     * Reassigns a task to a different technician
     */
    public TaskAssignment reassignTask(Long assignmentId, Long newTechnicianId) {
        TaskAssignment currentAssignment = taskAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        Staff newTechnician = staffRepository.findById(newTechnicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found with id: " + newTechnicianId));

        if (!isTechnicianAvailable(newTechnicianId)) {
            throw new RuntimeException("New technician is not available for assignment");
        }

        // Mark current assignment as rejected if it was pending or accepted
        if (currentAssignment.getStatus() == TaskAssignment.AssignmentStatus.PENDING_ACCEPTANCE ||
                currentAssignment.getStatus() == TaskAssignment.AssignmentStatus.ACCEPTED) {
            currentAssignment.setStatus(TaskAssignment.AssignmentStatus.REJECTED);
            currentAssignment.setRejectionReason("Reassigned to another technician");
            taskAssignmentRepository.save(currentAssignment);
        }

        // Create new assignment
        TaskAssignment newAssignment = new TaskAssignment(
                currentAssignment.getTask(),
                newTechnician,
                TaskAssignment.AssignmentMethod.MANUAL
        );

        return taskAssignmentRepository.save(newAssignment);
    }

    /**
     * Marks a task as started
     */
    public TaskAssignment startTask(Long assignmentId) {
        TaskAssignment assignment = taskAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        if (assignment.getStatus() != TaskAssignment.AssignmentStatus.ACCEPTED) {
            throw new RuntimeException("Task must be accepted before it can be started");
        }

        assignment.setStatus(TaskAssignment.AssignmentStatus.IN_PROGRESS);
        assignment.setStartedAt(LocalDateTime.now());

        return taskAssignmentRepository.save(assignment);
    }

    /**
     * Marks a task as completed
     */
    public void completeTask(Long assignmentId) {
        TaskAssignment assignment = taskAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));

        if (assignment.getStatus() != TaskAssignment.AssignmentStatus.IN_PROGRESS) {
            throw new RuntimeException("Task must be in progress to be completed");
        }

        assignment.setStatus(TaskAssignment.AssignmentStatus.COMPLETED);
        assignment.setCompletedAt(LocalDateTime.now());

        taskAssignmentRepository.save(assignment);
    }

    /**
     * Gets all assignments for a specific technician, optionally filtered by status
     */
    public List<TaskAssignment> getTechnicianAssignments(Long technicianId, TaskAssignment.AssignmentStatus status) {
        if (status != null) {
            return taskAssignmentRepository.findByTechnicianIdAndStatus(technicianId, status);
        } else {
            return taskAssignmentRepository.findByTechnicianId(technicianId);
        }
    }

    /**
     * Gets all assignments for a specific task
     */
    public List<TaskAssignment> getTaskAssignments(String taskId) {
        return taskAssignmentRepository.findByTaskId(taskId);
    }

    /**
     * Gets assignments between date range
     */
    public List<TaskAssignment> getAssignmentsBetweenDates(LocalDateTime start, LocalDateTime end) {
        return taskAssignmentRepository.findByAssignedAtBetween(start, end);
    }

    /**
     * Gets pending assignments for a technician
     */
    public List<TaskAssignment> getPendingAssignments(Long technicianId) {
        return taskAssignmentRepository.findByTechnicianIdAndStatus(
                technicianId,
                TaskAssignment.AssignmentStatus.PENDING_ACCEPTANCE
        );
    }

    /**
     * Gets active assignments for a technician (accepted or in progress)
     */
    public List<TaskAssignment> getActiveAssignments(Long technicianId) {
        return taskAssignmentRepository.findByTechnicianIdAndStatusIn(
                technicianId,
                List.of(TaskAssignment.AssignmentStatus.ACCEPTED, TaskAssignment.AssignmentStatus.IN_PROGRESS)
        );
    }

    // Helper methods

    /**
     * Finds the best available technician for a task based on skills and workload
     */
    private Staff findBestAvailableTechnician(MaintenanceTask task) {
        List<Staff> availableTechnicians = staffRepository.findAvailableTechnicians();

        // Simple algorithm: find technician with lowest current workload
        Staff bestTechnician = null;
        int lowestWorkload = Integer.MAX_VALUE;

        for (Staff technician : availableTechnicians) {
            int currentWorkload = getCurrentWorkload(technician.getId());
            if (currentWorkload < lowestWorkload) {
                lowestWorkload = currentWorkload;
                bestTechnician = technician;
            }
        }

        return bestTechnician;
    }

    /**
     * Checks if a technician is available for new assignments
     */
    private boolean isTechnicianAvailable(Long technicianId) {
        int activeAssignments = taskAssignmentRepository.countActiveAssignmentsByTechnician(technicianId);
        // Assuming max 5 active assignments per technician
        return activeAssignments < 5;
    }

    /**
     * Gets current workload for a technician
     */
    private int getCurrentWorkload(Long technicianId) {
        return taskAssignmentRepository.countActiveAssignmentsByTechnician(technicianId);
    }

    /**
     * Gets assignment statistics for reporting
     */
    public AssignmentStatistics getAssignmentStatistics(LocalDateTime start, LocalDateTime end) {
        List<TaskAssignment> assignments = getAssignmentsBetweenDates(start, end);

        long totalAssignments = assignments.size();
        long completedAssignments = assignments.stream()
                .filter(a -> a.getStatus() == TaskAssignment.AssignmentStatus.COMPLETED)
                .count();
        long pendingAssignments = assignments.stream()
                .filter(a -> a.getStatus() == TaskAssignment.AssignmentStatus.PENDING_ACCEPTANCE)
                .count();
        long rejectedAssignments = assignments.stream()
                .filter(a -> a.getStatus() == TaskAssignment.AssignmentStatus.REJECTED)
                .count();

        return new AssignmentStatistics(totalAssignments, completedAssignments, pendingAssignments, rejectedAssignments);
    }

    // Inner class for statistics
    public static class AssignmentStatistics {
        private final long totalAssignments;
        private final long completedAssignments;
        private final long pendingAssignments;
        private final long rejectedAssignments;

        public AssignmentStatistics(long totalAssignments, long completedAssignments,
                                    long pendingAssignments, long rejectedAssignments) {
            this.totalAssignments = totalAssignments;
            this.completedAssignments = completedAssignments;
            this.pendingAssignments = pendingAssignments;
            this.rejectedAssignments = rejectedAssignments;
        }

        // Getters
        public long getTotalAssignments() { return totalAssignments; }
        public long getCompletedAssignments() { return completedAssignments; }
        public long getPendingAssignments() { return pendingAssignments; }
        public long getRejectedAssignments() { return rejectedAssignments; }
        public double getCompletionRate() {
            return totalAssignments > 0 ? (double) completedAssignments / totalAssignments * 100 : 0;
        }
    }
}