package IMAS.ImasProject.services;


import IMAS.ImasProject.dto.StaffDTO;
import IMAS.ImasProject.dto.TeamDTO;
import IMAS.ImasProject.exception.ResourceNotFoundException;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.Team;
import IMAS.ImasProject.repository.StaffRepository;
import IMAS.ImasProject.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private StaffService staffService;

    public List<TeamDTO> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TeamDTO getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        return convertToDTO(team);
    }

    @Transactional
    public TeamDTO createTeam(TeamDTO teamDTO) {
        Team team = new Team();
        team.setName(teamDTO.getName());
        team.setDescription(teamDTO.getDescription());

        if (teamDTO.getMemberIds() != null && !teamDTO.getMemberIds().isEmpty()) {
            Set<Staff> members = new HashSet<>();
            for (Long staffId : teamDTO.getMemberIds()) {
                Staff staff = staffRepository.findById(staffId)
                        .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
                members.add(staff);
            }
            team.setMembers(members);
        }

        Team savedTeam = teamRepository.save(team);
        return convertToDTO(savedTeam);
    }

    @Transactional
    public TeamDTO updateTeam(Long id, TeamDTO teamDTO) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));

        team.setName(teamDTO.getName());
        team.setDescription(teamDTO.getDescription());

        if (teamDTO.getMemberIds() != null) {
            Set<Staff> members = new HashSet<>();
            for (Long staffId : teamDTO.getMemberIds()) {
                Staff staff = staffRepository.findById(staffId)
                        .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + staffId));
                members.add(staff);
            }
            team.setMembers(members);
        }

        Team updatedTeam = teamRepository.save(team);
        return convertToDTO(updatedTeam);
    }

    @Transactional
    public void deleteTeam(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + id));
        teamRepository.delete(team);
    }

    public TeamDTO getTeamByTechnicianId(Long technicianId) {
        Team team = teamRepository.findFirstByMemberId(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("No team found for technician with ID: " + technicianId));
        return convertToDTO(team);
    }

    public List<Long> getTeamMemberIds(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        return team.getMembers().stream()
                .map(Staff::getId)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getTeamPerformance(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));

        List<Long> memberIds = team.getMembers().stream()
                .map(Staff::getId)
                .collect(Collectors.toList());

        Map<String, Object> performance = new HashMap<>();
        performance.put("teamName", team.getName());
        performance.put("memberCount", memberIds.size());
        performance.put("members", memberIds);
        performance.put("performanceMetrics", calculatePerformanceMetrics(teamId, memberIds));

        return performance;
    }

    @Transactional
    public void addMemberToTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        Staff staff = staffRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + memberId));

        team.getMembers().add(staff);
        teamRepository.save(team);
    }

    @Transactional
    public void removeMemberFromTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with id: " + teamId));
        Staff staff = staffRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + memberId));

        team.getMembers().remove(staff);
        teamRepository.save(team);
    }

    private TeamDTO convertToDTO(Team team) {
        TeamDTO dto = new TeamDTO();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());

        List<StaffDTO> memberDTOs = team.getMembers().stream()
                .map(staffService::convertToDTO)
                .collect(Collectors.toList());
        dto.setMembers(memberDTOs);

        List<Long> memberIds = team.getMembers().stream()
                .map(Staff::getId)
                .collect(Collectors.toList());
        dto.setMemberIds(memberIds);

        return dto;
    }

    private Map<String, Object> calculatePerformanceMetrics(Long teamId, List<Long> memberIds) {
        Map<String, Object> metrics = new HashMap<>();

        // Implémentez ici la logique de calcul des métriques de performance
        metrics.put("onTimeRate", 92.5);
        metrics.put("incidentResolutionRate", 88.0);
        metrics.put("averageResponseTime", 2.5);

        return metrics;
    }
}