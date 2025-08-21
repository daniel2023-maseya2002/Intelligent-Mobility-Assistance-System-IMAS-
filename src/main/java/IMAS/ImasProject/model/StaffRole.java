package IMAS.ImasProject.model;

public enum StaffRole {
    ADMIN("Admin"),
    TECHNICIAN("Technician"),
    DRIVER("Driver"),
    ANALYST("Analyst"),  // DÉJÀ PRÉSENT
    PASSENGER("Passenger");

    private final String displayName;

    StaffRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }


    public boolean isAnalyst() {
        return this == ANALYST;
    }



    // Utility methods for role checking
    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isTechnician() {
        return this == TECHNICIAN;
    }

    public boolean isDriver() {
        return this == DRIVER;
    }

    public boolean isPassenger() {
        return this == PASSENGER;
    }

    // Method to get role from string (case-insensitive)
    public static StaffRole fromString(String role) {
        if (role == null) {
            return null;
        }

        for (StaffRole staffRole : StaffRole.values()) {
            if (staffRole.name().equalsIgnoreCase(role) ||
                    staffRole.displayName.equalsIgnoreCase(role)) {
                return staffRole;
            }
        }

        throw new IllegalArgumentException("Unknown role: " + role);
    }

    // Method to check if a role has administrative privileges
    public boolean hasAdministrativePrivileges() {
        return this == ADMIN || this == TECHNICIAN || this == ANALYST;
    }





    // AJOUTEZ cette méthode pour définir les permissions d'ANALYST
    public boolean canManageReports() {
        return this == ADMIN || this == ANALYST;
    }
    // Method to check if a role can manage drivers
    public boolean canManageDrivers() {
        return this == ADMIN;
    }

    // Method to check if a role can manage passengers
    public boolean canManagePassengers() {
        return this == ADMIN || this == TECHNICIAN;
    }

    // Method to check if a role can view system analytics
    // AJOUTEZ cette méthode spécifique pour ANALYST
    public boolean canViewAnalytics() {
        return this == ADMIN || this == TECHNICIAN || this == ANALYST;
    }

    @Override
    public String toString() {
        return displayName;
    }
}