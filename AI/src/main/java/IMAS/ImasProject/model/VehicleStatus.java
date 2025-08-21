package IMAS.ImasProject.model;

/**
 * Enumeration for Vehicle Status
 * Defines the different states a vehicle can be in
 */
public enum VehicleStatus {
    /**
     * Vehicle is active and ready for service
     */
    ACTIVE("Active"),

    /**
     * Vehicle is inactive/parked
     */
    INACTIVE("Inactive"),

    /**
     * Vehicle is currently in transit
     */
    IN_TRANSIT("In Transit"),

    /**
     * Vehicle is under maintenance
     */
    MAINTENANCE("Under Maintenance"),

    /**
     * Vehicle has broken down
     */
    BREAKDOWN("Breakdown"),

    /**
     * Vehicle is out of service
     */
    OUT_OF_SERVICE("Out of Service");

    private final String displayName;

    VehicleStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if the vehicle is available for service
     */
    public boolean isAvailableForService() {
        return this == ACTIVE || this == INACTIVE;
    }

    /**
     * Check if the vehicle is operational
     */
    public boolean isOperational() {
        return this == ACTIVE || this == IN_TRANSIT;
    }

    /**
     * Check if the vehicle needs attention
     */
    public boolean needsAttention() {
        return this == MAINTENANCE || this == BREAKDOWN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}