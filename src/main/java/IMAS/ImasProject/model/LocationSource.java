package IMAS.ImasProject.model;

/**
 * Enumeration for Location Source
 * Defines the different sources from which vehicle location data can be obtained
 */
public enum LocationSource {
    /**
     * Global Positioning System - Most accurate
     */
    GPS("GPS", 1),

    /**
     * Cellular tower triangulation
     */
    CELLULAR("Cellular", 2),

    /**
     * WiFi positioning system
     */
    WIFI("WiFi", 3),

    /**
     * Bluetooth beacons
     */
    BLUETOOTH("Bluetooth", 4),

    /**
     * Manual entry by driver or operator
     */
    MANUAL("Manual", 5),

    /**
     * Estimated based on route and schedule
     */
    ESTIMATED("Estimated", 6),

    /**
     * Network-based location (carrier networks)
     */
    NETWORK("Network", 7),

    /**
     * Last known location (when real-time data unavailable)
     */
    LAST_KNOWN("Last Known", 8);

    private final String displayName;
    private final int accuracyRank; // 1 = most accurate, higher numbers = less accurate

    LocationSource(String displayName, int accuracyRank) {
        this.displayName = displayName;
        this.accuracyRank = accuracyRank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAccuracyRank() {
        return accuracyRank;
    }

    /**
     * Check if this location source is considered highly accurate
     */
    public boolean isHighAccuracy() {
        return accuracyRank <= 2; // GPS and Cellular
    }

    /**
     * Check if this location source is real-time
     */
    public boolean isRealTime() {
        return this != ESTIMATED && this != LAST_KNOWN && this != MANUAL;
    }

    /**
     * Check if this location source is automatically generated
     */
    public boolean isAutomatic() {
        return this != MANUAL;
    }

    /**
     * Get accuracy description based on the source
     */
    public String getAccuracyDescription() {
        return switch (this) {
            case GPS -> "High accuracy (1-5m)";
            case CELLULAR -> "Medium accuracy (100-1000m)";
            case WIFI -> "Medium accuracy (50-500m)";
            case BLUETOOTH -> "Low accuracy (10-100m)";
            case NETWORK -> "Low accuracy (500-5000m)";
            case MANUAL -> "Variable accuracy";
            case ESTIMATED -> "Route-based estimate";
            case LAST_KNOWN -> "Historical data";
        };
    }

    /**
     * Compare accuracy with another location source
     */
    public boolean isMoreAccurateThan(LocationSource other) {
        return this.accuracyRank < other.accuracyRank;
    }

    @Override
    public String toString() {
        return displayName;
    }
}