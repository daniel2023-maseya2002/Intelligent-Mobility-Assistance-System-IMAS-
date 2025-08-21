package IMAS.ImasProject.dto;



import IMAS.ImasProject.model.Team;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor

public class TeamDTO {

    private Long id;
    private String name;
    private String description;
    private List<StaffDTO> members;
    private List<Long> memberIds;
    private Team team; // Added to store the actual Team entity

    // Constructeurs, getters et setters

    public TeamDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StaffDTO> getMembers() {
        return members;
    }

    public void setMembers(List<StaffDTO> members) {
        this.members = members;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    // Add getters and setters for the Team entity
    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}