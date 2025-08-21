package IMAS.ImasProject.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StatsDTO {
    private Long operatorId;
    private Long teamId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Constructeur
    public StatsDTO(Long operatorId, Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        this.operatorId = operatorId;
        this.teamId = teamId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
