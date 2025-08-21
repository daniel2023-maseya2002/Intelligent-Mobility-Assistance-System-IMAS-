package IMAS.ImasProject.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
@Data
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "id")
    )
    @JsonIgnoreProperties("teams")
    private Set<Staff> members = new HashSet<>();

    // Constructors
    public Team() {}

    // Helper methods
    public void addMember(Staff staff) {
        this.members.add(staff);
        staff.getTeams().add(this);
    }

    public void removeMember(Staff staff) {
        this.members.remove(staff);
        staff.getTeams().remove(this);
    }
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

    public Set<Staff> getMembers() {
        return members;
    }

    public void setMembers(Set<Staff> members) {
        this.members = members;
    }

    // Helper methods

}