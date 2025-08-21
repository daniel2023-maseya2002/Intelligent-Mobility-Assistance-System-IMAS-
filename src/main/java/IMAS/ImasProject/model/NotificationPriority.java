package IMAS.ImasProject.model;

/**
 * NotificationPriority Enum - Defines the priority levels for notifications
 */
public enum NotificationPriority {
    /**
     * Low priority - non-urgent notifications
     */
    LOW(1, "Low", "Non-urgent notification"),

    /**
     * Normal priority - standard notifications
     */
    NORMAL(2, "Normal", "Standard priority notification"),

    /**
     * High priority - important notifications
     */
    HIGH(3, "High", "Important notification requiring attention"),

    /**
     * Critical priority - urgent notifications
     */
    CRITICAL(4, "Critical", "Critical notification requiring immediate attention"),

    /**
     * Emergency priority - emergency notifications
     */
    EMERGENCY(5, "Emergency", "Emergency notification - highest priority");

    private final int level;
    private final String displayName;
    private final String description;

    NotificationPriority(int level, String displayName, String description) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this priority is higher than another priority
     */
    public boolean isHigherThan(NotificationPriority other) {
        return this.level > other.level;
    }

    /**
     * Check if this priority is lower than another priority
     */
    public boolean isLowerThan(NotificationPriority other) {
        return this.level < other.level;
    }

    /**
     * Check if this priority is critical or emergency level
     */
    public boolean isCriticalLevel() {
        return this == CRITICAL || this == EMERGENCY;
    }

    /**
     * Check if this priority is high level or above
     */
    public boolean isHighLevel() {
        return this.level >= HIGH.level;
    }

    @Override
    public String toString() {
        return displayName;
    }
}