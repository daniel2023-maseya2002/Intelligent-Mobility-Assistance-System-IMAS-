package IMAS.ImasProject.services;


import IMAS.ImasProject.model.Incident;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusCollisionService {

    private final IncidentService incidentService;

    /**
     * Records a bus collision incident in the database
     * @param busId1 ID of the first bus involved
     * @param busId2 ID of the second bus involved
     * @param location Location where the collision occurred
     * @param summary Brief description of the collision
     * @return The created incident
     */
    @Transactional
    public Incident recordBusCollision(String busId1, String busId2, String location, String summary) {
        // Create a new incident object
        Incident incident = Incident.builder()
                .incidentType(Incident.IncidentType.COLLISION)
                .location(location)
                .dateTime(LocalDateTime.now())
                .summary(summary)
                .status(Incident.IncidentStatus.REPORTED)
                .build();

        // Add additional details about the buses involved
        Map<String, String> details = new HashMap<>();
        details.put("busId1", busId1);
        details.put("busId2", busId2);
        details.put("severity", "HIGH");
        details.put("emergency_services_dispatched", "true");
        details.put("traffic_impact", "SEVERE");
        details.put("passenger_evacuation_required", "true");

        incident.setAdditionalDetails(details);

        // Save the incident
        return incidentService.createIncident(incident);
    }

    /**
     * Records a bus collision with a vehicle incident
     * @param busId ID of the bus involved
     * @param vehicleType Type of vehicle (car, truck, motorcycle, etc.)
     * @param location Location where the collision occurred
     * @param summary Brief description of the collision
     * @return The created incident
     */
    @Transactional
    public Incident recordBusVehicleCollision(String busId, String vehicleType, String location, String summary) {
        // Create a new incident object
        Incident incident = Incident.builder()
                .incidentType(Incident.IncidentType.COLLISION)
                .location(location)
                .dateTime(LocalDateTime.now())
                .summary(summary)
                .status(Incident.IncidentStatus.REPORTED)
                .build();

        // Add additional details about the collision
        Map<String, String> details = new HashMap<>();
        details.put("busId", busId);
        details.put("vehicleType", vehicleType);
        details.put("severity", "MEDIUM");
        details.put("emergency_services_dispatched", "true");
        details.put("traffic_impact", "MODERATE");
        details.put("passenger_injuries_reported", "false");

        incident.setAdditionalDetails(details);

        // Save the incident
        return incidentService.createIncident(incident);
    }

    /**
     * Records a bus collision with infrastructure (building, pole, etc.)
     * @param busId ID of the bus involved
     * @param infrastructureType Type of infrastructure hit
     * @param location Location where the collision occurred
     * @param summary Brief description of the collision
     * @return The created incident
     */
    @Transactional
    public Incident recordBusInfrastructureCollision(String busId, String infrastructureType, String location, String summary) {
        // Create a new incident object
        Incident incident = Incident.builder()
                .incidentType(Incident.IncidentType.COLLISION)
                .location(location)
                .dateTime(LocalDateTime.now())
                .summary(summary)
                .status(Incident.IncidentStatus.REPORTED)
                .build();

        // Add additional details about the collision
        Map<String, String> details = new HashMap<>();
        details.put("busId", busId);
        details.put("infrastructureType", infrastructureType);
        details.put("severity", "MEDIUM");
        details.put("emergency_services_dispatched", "true");
        details.put("infrastructure_damage", "true");
        details.put("bus_operational", "false");

        incident.setAdditionalDetails(details);

        // Save the incident
        return incidentService.createIncident(incident);
    }
}