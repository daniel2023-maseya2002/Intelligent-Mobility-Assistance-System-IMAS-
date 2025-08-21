package IMAS.ImasProject.model;

/**
 * NotificationType Enum - Defines the different types of notifications in the system
 */
public enum NotificationType {
    /**
     * General informational notifications
     */
    INFO("Information", "General information notification"),

    /**
     * Service alerts and announcements
     */
    SERVICE_ALERT("Service Alert", "Service disruption or important announcement"),

    /**
     * Delay notifications for routes or vehicles
     */
    DELAY_ALERT("Delay Alert", "Transportation delay notification"),

    /**
     * Route change notifications
     */
    ROUTE_CHANGE("Route Change", "Route modification or diversion"),

    /**
     * Vehicle breakdown or maintenance notifications
     */
    VEHICLE_ISSUE("Vehicle Issue", "Vehicle breakdown or maintenance"),

    /**
     * Stop closure or temporary unavailability
     */
    STOP_CLOSURE("Stop Closure", "Bus stop closure or unavailability"),

    /**
     * Emergency alerts
     */
    EMERGENCY("Emergency", "Emergency notification"),

    /**
     * Personal notifications for specific users
     */
    PERSONAL("Personal", "Personal user notification"),

    /**
     * System maintenance notifications
     */
    MAINTENANCE("Maintenance", "System maintenance notification"),

    /**
     * Promotional or marketing notifications
     */
    PROMOTION("Promotion", "Promotional or marketing message"),

    /**
     * Reminder notifications
     */
    REMINDER("Reminder", "Reminder notification"),

    /**
     * Weather-related alerts
     */
    WEATHER_ALERT("Weather Alert", "Weather-related service impact");

    private final String displayName;
    private final String description;

    NotificationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}