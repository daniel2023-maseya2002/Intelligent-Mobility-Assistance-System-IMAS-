package IMAS.ImasProject.model;

public enum RatingType {
    SERVICE("Service Quality"),
    DRIVER("Driver Performance"),
    BUS("Bus Condition"),
    ROUTE("Route Experience"),
    OVERALL("Overall Experience");

    private final String displayName;

    RatingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return this.name();
    }
}