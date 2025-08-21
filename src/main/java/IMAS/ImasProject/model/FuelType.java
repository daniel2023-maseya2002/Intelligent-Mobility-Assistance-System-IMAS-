package IMAS.ImasProject.model;


/**
 * Enumeration for Fuel Types
 * Defines the different fuel types that vehicles can use
 */
public enum FuelType {
    /**
     * Gasoline/Petrol fuel
     */
    GASOLINE("Gasoline"),

    /**
     * Diesel fuel
     */
    DIESEL("Diesel"),

    /**
     * Electric vehicle
     */
    ELECTRIC("Electric"),

    /**
     * Hybrid vehicle (combination of fuel and electric)
     */
    HYBRID("Hybrid"),

    /**
     * Compressed Natural Gas
     */
    CNG("CNG"),

    /**
     * Liquefied Petroleum Gas
     */
    LPG("LPG"),

    /**
     * Hydrogen fuel cell
     */
    HYDROGEN("Hydrogen");

    private final String displayName;

    FuelType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if the fuel type is eco-friendly
     */
    public boolean isEcoFriendly() {
        return this == ELECTRIC || this == HYDROGEN || this == HYBRID;
    }

    /**
     * Check if the fuel type requires liquid fuel
     */
    public boolean requiresLiquidFuel() {
        return this == GASOLINE || this == DIESEL || this == LPG;
    }

    @Override
    public String toString() {
        return displayName;
    }
}