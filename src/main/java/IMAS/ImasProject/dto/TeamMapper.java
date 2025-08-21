package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {
    public TeamDTO toDTO(Team team) {
        if (team == null) {
            return null;
        }

        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());
        // Note: You might want to map members to StaffDTOs here if needed
        return dto;
    }
}
