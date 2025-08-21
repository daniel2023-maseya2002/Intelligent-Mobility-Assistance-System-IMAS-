package IMAS.ImasProject.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
    private boolean isTeamAssignment;
    private List<Long> technicianIds;
    private Long teamId;
    private String taskDescription;
    private String taskPriority;
    private String taskDeadline;
    private boolean createTaskRecords = true; // Default to true
}