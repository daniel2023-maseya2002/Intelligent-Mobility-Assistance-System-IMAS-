package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.StaffRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

public class StaffDTO {
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 characters")
    private String phoneNumber;

    private String password; // This field will not be serialized in JSON responses

    @NotNull(message = "Role is required")
    private StaffRole role;

    private boolean active = true;

    private byte[] photo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String resetToken;

    private LocalDateTime resetTokenExpiration;

    // Default constructor
    public StaffDTO() {}

    // Constructor with essential fields
    public StaffDTO(String firstName, String lastName, String email, String phoneNumber, StaffRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with ID (for updates)
    public StaffDTO(Long id, String firstName, String lastName, String email, String phoneNumber, StaffRole role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the full name by combining first and last name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null) {
            return lastName.trim();
        }
        if (lastName == null) {
            return firstName.trim();
        }
        return (firstName.trim() + " " + lastName.trim()).trim();
    }

    // Never serialize the password in JSON responses
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    // But allow deserializing from JSON requests and setting for authentication
    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    public StaffRole getRole() {
        return role;
    }

    public void setRole(StaffRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonIgnore
    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    @JsonIgnore
    public LocalDateTime getResetTokenExpiration() {
        return resetTokenExpiration;
    }

    public void setResetTokenExpiration(LocalDateTime resetTokenExpiration) {
        this.resetTokenExpiration = resetTokenExpiration;
    }

    /**
     * Check if the reset token is expired
     */
    @JsonIgnore
    public boolean isResetTokenExpired() {
        return resetTokenExpiration == null || LocalDateTime.now().isAfter(resetTokenExpiration);
    }

    /**
     * Check if the staff member has a valid reset token
     */
    @JsonIgnore
    public boolean hasValidResetToken() {
        return resetToken != null && !resetToken.trim().isEmpty() && !isResetTokenExpired();
    }

    /**
     * Get the role display name
     */
    @JsonIgnore
    public String getRoleDisplayName() {
        return role != null ? role.getDisplayName() : "";
    }

    /**
     * Check if the staff member is an admin
     */
    @JsonIgnore
    public boolean isAdmin() {
        return role == StaffRole.ADMIN;
    }

    /**
     * Check if the staff member is a technician
     */
    @JsonIgnore
    public boolean isTechnician() {
        return role == StaffRole.TECHNICIAN;
    }

    /**
     * Check if the staff member is a driver
     */
    @JsonIgnore
    public boolean isDriver() {
        return role == StaffRole.DRIVER;
    }

    /**
     * Check if the staff member is a passenger
     */
    @JsonIgnore
    public boolean isPassenger() {
        return role == StaffRole.PASSENGER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffDTO staffDTO = (StaffDTO) o;
        return Objects.equals(id, staffDTO.id) &&
                Objects.equals(email, staffDTO.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "StaffDTO{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", role=" + role +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}