package IMAS.ImasProject.model;

/**
 * TargetAudience Enum - Defines the target audience for notifications
 */
public enum TargetAudience {
    /**
     * All users in the system
     */
    ALL_USERS("All Users", "Notification sent to all registered users"),

    /**
     * Users of a specific route
     */
    ROUTE_USERS("Route Users", "Notification sent to users of a specific route"),

    /**
     * Users at a specific stop
     */
    STOP_USERS("Stop Users", "Notification sent to users of a specific stop"),

    /**
     * Users in a specific geographic area
     */
    AREA_USERS("Area Users", "Notification sent to users in a specific area"),

    /**
     * A specific individual user
     */
    SPECIFIC_USER("Specific User", "Notification sent to a specific user"),

    /**
     * Users with active trips
     */
    ACTIVE_TRAVELERS("Active Travelers", "Notification sent to users currently traveling"),

    /**
     * Premium or subscribed users
     */
    PREMIUM_USERS("Premium Users", "Notification sent to premium subscribers"),

    /**
     * New users (within registration period)
     */
    NEW_USERS("New Users", "Notification sent to recently registered users"),

    /**
     * Users who haven't used the app recently
     */
    INACTIVE_USERS("Inactive Users", "Notification sent to inactive users"),

    /**
     * Users with specific preferences
     */
    PREFERENCE_BASED("Preference Based", "Notification sent based on user preferences"),

    /**
     * System administrators
     */
    ADMIN_USERS("Admin Users", "Notification sent to system administrators"),

    /**
     * Driver/operator users
     */
    DRIVER_USERS("Driver Users", "Notification sent to drivers and operators"),

    /**
     * Users with opted-in notification preferences
     */
    OPTED_IN_USERS("Opted-in Users", "Notification sent to users who opted in"),

    /**
     * Custom user segment
     */
    CUSTOM_SEGMENT("Custom Segment", "Notification sent to a custom user segment");

    private final String displayName;
    private final String description;

    TargetAudience(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this audience targets a specific user
     */
    public boolean isUserSpecific() {
        return this == SPECIFIC_USER;
    }

    /**
     * Check if this audience targets a geographic context
     */
    public boolean isLocationBased() {
        return this == ROUTE_USERS || this == STOP_USERS || this == AREA_USERS;
    }

    /**
     * Check if this audience targets all users
     */
    public boolean isGlobalAudience() {
        return this == ALL_USERS;
    }

    /**
     * Check if this audience requires additional context
     */
    public boolean requiresContext() {
        return this == ROUTE_USERS || this == STOP_USERS || this == AREA_USERS ||
                this == SPECIFIC_USER || this == CUSTOM_SEGMENT;
    }

    /**
     * Check if this audience is administrative
     */
    public boolean isAdministrative() {
        return this == ADMIN_USERS || this == DRIVER_USERS;
    }

    @Override
    public String toString() {
        return displayName;
    }
}