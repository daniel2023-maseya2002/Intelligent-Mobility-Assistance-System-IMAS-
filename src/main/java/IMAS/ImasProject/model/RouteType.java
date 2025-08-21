package IMAS.ImasProject.model;

/**
 * Enumeration for Route Types
 * Defines the different types of routes available in the system
 */
public enum RouteType {
    /**
     * Urban routes within city limits
     */
    URBAN("Urban"),

    /**
     * Suburban routes connecting suburbs to city
     */
    SUBURBAN("Suburban"),

    /**
     * Intercity routes between different cities
     */
    INTERCITY("Intercity"),

    /**
     * Express routes with limited stops
     */
    EXPRESS("Express"),

    /**
     * Local routes with frequent stops
     */
    LOCAL("Local"),

    /**
     * Shuttle services for specific areas
     */
    SHUTTLE("Shuttle");

    private final String displayName;

    RouteType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if the route type is for long distance travel
     */
    public boolean isLongDistance() {
        return this == INTERCITY || this == EXPRESS;
    }

    /**
     * Check if the route type is for short distance travel
     */
    public boolean isShortDistance() {
        return this == LOCAL || this == SHUTTLE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}