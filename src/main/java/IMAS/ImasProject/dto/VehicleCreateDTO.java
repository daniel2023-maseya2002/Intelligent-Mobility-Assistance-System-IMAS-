package IMAS.ImasProject.dto;

import IMAS.ImasProject.model.FuelType;
import IMAS.ImasProject.model.VehicleStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCreateDTO {

    @NotBlank(message = "Vehicle number is required")
    @Size(max = 50, message = "Vehicle number must not exceed 50 characters")
    private String vehicleNumber;

    @NotBlank(message = "License plate is required")
    @Size(max = 20, message = "License plate must not exceed 20 characters")
    private String licensePlate;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 200, message = "Capacity cannot exceed 200")
    private Integer capacity;

    @NotBlank(message = "Vehicle type is required")
    @Size(max = 50, message = "Vehicle type must not exceed 50 characters")
    private String vehicleType;

    private Boolean isAccessible = false;

    @NotNull(message = "Status is required")
    private VehicleStatus status;

    @Size(max = 50, message = "Manufacturer must not exceed 50 characters")
    private String manufacturer;

    @Size(max = 50, message = "Model must not exceed 50 characters")
    private String model;

    @Min(value = 1900, message = "Year must be valid")
    @Max(value = 2030, message = "Year cannot be in the future")
    private Integer year;

    @DecimalMin(value = "0.0", message = "Fuel capacity must be positive")
    private Double fuelCapacity;

    private FuelType fuelType;
    private Boolean hasAirConditioning = false;
    private Boolean hasWifi = false;
    private Boolean hasGps = false;
    private LocalDateTime lastMaintenance;
    private LocalDateTime nextMaintenance;

    @DecimalMin(value = "0.0", message = "Odometer reading must be positive")
    private Double odometerReading;

    private Long routeId;
}