package IMAS.ImasProject.model;

/**
 * Schedule Type Enum - Defines different types of schedules
 * Used to categorize schedules for business logic and fare calculation
 */
public enum ScheduleType {

    /**
     * Regular scheduled service - standard operating schedule
     */
    REGULAR("Regular Service"),

    /**
     * Peak hour service - increased frequency during rush hours
     */
    PEAK_HOUR("Peak Hour Service"),

    /**
     * Off-peak service - reduced frequency during low demand periods
     */
    OFF_PEAK("Off-Peak Service"),

    /**
     * Weekend service - special schedule for weekends
     */
    WEEKEND("Weekend Service"),

    /**
     * Holiday service - special schedule for holidays
     */
    HOLIDAY("Holiday Service"),

    /**
     * Night service - late night or overnight service
     */
    NIGHT("Night Service"),

    /**
     * Express service - limited stops, faster transit
     */
    EXPRESS("Express Service"),

    /**
     * Local service - all stops, regular speed
     */
    LOCAL("Local Service"),

    /**
     * Special event service - temporary service for events
     */
    SPECIAL_EVENT("Special Event Service"),

    /**
     * Seasonal service - operates only during specific seasons
     */
    SEASONAL("Seasonal Service"),

    /**
     * Emergency service - temporary service during disruptions
     */
    EMERGENCY("Emergency Service");

    private final String displayName;

    ScheduleType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the human-readable display name
     * @return the display name of the schedule type
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get formatted name for descriptions (replaces underscores with spaces)
     * @return formatted name
     */
    public String getFormattedName() {
        return this.name().replace("_", " ");
    }

    /**
     * Check if this is a peak time schedule type
     * @return true if peak hour or express service
     */
    public boolean isPeakType() {
        return this == PEAK_HOUR || this == EXPRESS;
    }

    /**
     * Check if this is a reduced service type
     * @return true if off-peak, night, or holiday service
     */
    public boolean isReducedService() {
        return this == OFF_PEAK || this == NIGHT || this == HOLIDAY;
    }

    /**
     * Check if this is a special/temporary service
     * @return true if special event, emergency, or seasonal
     */
    public boolean isSpecialService() {
        return this == SPECIAL_EVENT || this == EMERGENCY || this == SEASONAL;
    }

    /**
     * Get fare multiplier for this schedule type
     * @return multiplier for fare calculation
     */
    public double getFareMultiplier() {
        switch (this) {
            case PEAK_HOUR:
            case EXPRESS:
                return 1.2; // 20% premium
            case NIGHT:
                return 1.1; // 10% premium for night service
            case OFF_PEAK:
            case HOLIDAY:
                return 0.9; // 10% discount
            case WEEKEND:
                return 0.95; // 5% discount
            default:
                return 1.0; // Regular fare
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}