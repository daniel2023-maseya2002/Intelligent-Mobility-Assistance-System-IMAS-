package IMAS.ImasProject.controller;


import IMAS.ImasProject.dto.TaskAssignmentDTO;
import IMAS.ImasProject.model.TaskAssignment;
import IMAS.ImasProject.services.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping("/auto-assign/{taskId}")
    public ResponseEntity<TaskAssignment> autoAssignTask(@PathVariable String taskId) {
        TaskAssignment assignment = assignmentService.assignTaskAutomatically(taskId);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/manual-assign")
    public ResponseEntity<TaskAssignment> manualAssignTask(@RequestBody TaskAssignmentDTO assignmentDTO) {
        TaskAssignment assignment = assignmentService.assignTaskManually(assignmentDTO);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/respond/{assignmentId}")
    public ResponseEntity<TaskAssignment> respondToAssignment(
            @PathVariable Long assignmentId,
            @RequestParam boolean accept,
            @RequestParam(required = false) String reason) {
        TaskAssignment assignment = assignmentService.respondToAssignment(assignmentId, accept, reason);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/reassign/{assignmentId}/{newTechnicianId}")
    public ResponseEntity<TaskAssignment> reassignTask(
            @PathVariable Long assignmentId,
            @PathVariable Long newTechnicianId) {
        TaskAssignment assignment = assignmentService.reassignTask(assignmentId, newTechnicianId);
        return ResponseEntity.ok(assignment);
    }

    @PostMapping("/complete/{assignmentId}")
    public ResponseEntity<Void> completeTask(@PathVariable Long assignmentId) {
        assignmentService.completeTask(assignmentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<TaskAssignment>> getTechnicianAssignments(
            @PathVariable Long technicianId,
            @RequestParam(required = false) TaskAssignment.AssignmentStatus status) {
        List<TaskAssignment> assignments = assignmentService.getTechnicianAssignments(technicianId, status);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskAssignment>> getTaskAssignments(@PathVariable String taskId) {
        List<TaskAssignment> assignments = assignmentService.getTaskAssignments(taskId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<TaskAssignment>> getAssignmentsBetweenDates(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<TaskAssignment> assignments = assignmentService.getAssignmentsBetweenDates(start, end);
        return ResponseEntity.ok(assignments);
    }
}