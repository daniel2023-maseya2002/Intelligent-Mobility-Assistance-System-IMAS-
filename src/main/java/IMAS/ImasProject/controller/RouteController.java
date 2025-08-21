package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.*;
import IMAS.ImasProject.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @PostMapping
    public ResponseEntity<?> createRoute(@RequestBody RouteCreateRequest request) {
        try {
            Route route = new Route();
            route.setRouteName(request.getRouteName());
            route.setRouteCode(request.getRouteCode());
            route.setDescription(request.getDescription());
            route.setTotalDistance(request.getTotalDistance());
            route.setEstimatedDuration(request.getEstimatedDuration());
            route.setColor(request.getColor());
            route.setRouteType(request.getRouteType());
            route.setActive(true);

            Route savedRoute = routeService.save(route);
            return ResponseEntity.ok(savedRoute);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating route: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(routeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getRouteById(@PathVariable Long id) {
        Route route = routeService.findById(id);
        if (route != null) {
            return ResponseEntity.ok(route);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoute(@PathVariable Long id, @RequestBody RouteCreateRequest request) {
        try {
            Route route = routeService.findById(id);
            if (route == null) {
                return ResponseEntity.notFound().build();
            }

            route.setRouteName(request.getRouteName());
            route.setRouteCode(request.getRouteCode());
            route.setDescription(request.getDescription());
            route.setTotalDistance(request.getTotalDistance());
            route.setEstimatedDuration(request.getEstimatedDuration());
            route.setColor(request.getColor());
            route.setRouteType(request.getRouteType());

            Route updatedRoute = routeService.save(route);
            return ResponseEntity.ok(updatedRoute);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating route: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoute(@PathVariable Long id) {
        try {
            routeService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting route: " + e.getMessage());
        }
    }

    @GetMapping("/kinshasa")
    public ResponseEntity<List<Route>> getKinshasaRoutes() {
        List<Route> routes = routeService.findByCity("Kinshasa");
        return ResponseEntity.ok(routes);
    }

    @PostMapping("/kinshasa/predefined")
    public ResponseEntity<?> createPredefinedKinshasaRoutes() {
        try {
            // Créer les routes de base si elles n'existent pas
            routeService.createKinshasaPredefinedRoutes();

            // Générer 4 nouvelles routes aléatoirement
            List<Route> generatedRoutes = routeService.generateRandomRoutes(4);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Kinshasa routes created successfully");
            response.put("status", "success");
            response.put("generatedRoutes", generatedRoutes.size());
            response.put("routes", generatedRoutes);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error creating predefined routes: " + e.getMessage());
            errorResponse.put("status", "error");

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateRandomRoutes(@RequestParam(defaultValue = "4") int count) {
        try {
            List<Route> generatedRoutes = routeService.generateRandomRoutes(count);

            Map<String, Object> response = new HashMap<>();
            response.put("message", count + " new routes generated successfully");
            response.put("status", "success");
            response.put("generatedRoutes", generatedRoutes.size());
            response.put("routes", generatedRoutes);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error generating routes: " + e.getMessage());
            errorResponse.put("status", "error");

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }




    @PostMapping("/rerouting")
    public ResponseEntity<?> implementRerouting(@RequestBody ReroutingRequest request) {
        try {
            // Vérifier que les routes existent
            Route affectedRoute = routeService.findById(request.getAffectedRouteId());
            Route alternativeRoute = routeService.findById(request.getAlternativeRouteId());

            if (affectedRoute == null || alternativeRoute == null) {
                return ResponseEntity.badRequest().body("One or both routes not found");
            }

            // Créer le rerouting
            Rerouting rerouting = routeService.createRerouting(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rerouting implemented successfully");
            response.put("status", "success");
            response.put("reroutingId", rerouting.getId());
            response.put("affectedRoute", affectedRoute.getRouteName());
            response.put("alternativeRoute", alternativeRoute.getRouteName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error implementing rerouting: " + e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/rerouting/active")
    public ResponseEntity<List<Rerouting>> getActiveReroutings() {
        return ResponseEntity.ok(routeService.getActiveReroutings());
    }

    @GetMapping("/rerouting/history")
    public ResponseEntity<List<Rerouting>> getReroutingHistory() {
        return ResponseEntity.ok(routeService.getAllReroutings());
    }

    @PutMapping("/rerouting/{id}/end")
    public ResponseEntity<?> endRerouting(@PathVariable Long id) {
        try {
            routeService.endRerouting(id);
            return ResponseEntity.ok().body("Rerouting ended successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error ending rerouting: " + e.getMessage());
        }
    }


    // DTO pour ReroutingRequest
    public static class ReroutingRequest {
        private Long affectedRouteId;
        private Long alternativeRouteId;
        private String reason;
        private Integer estimatedDuration;
        private String additionalNotes;

        // Getters et setters
        public Long getAffectedRouteId() { return affectedRouteId; }
        public void setAffectedRouteId(Long affectedRouteId) { this.affectedRouteId = affectedRouteId; }

        public Long getAlternativeRouteId() { return alternativeRouteId; }
        public void setAlternativeRouteId(Long alternativeRouteId) { this.alternativeRouteId = alternativeRouteId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public Integer getEstimatedDuration() { return estimatedDuration; }
        public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

        public String getAdditionalNotes() { return additionalNotes; }
        public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
    }


    // DTO
    public static class RouteCreateRequest {
        private String routeName;
        private String routeCode;
        private String description;
        private Double totalDistance;
        private Integer estimatedDuration;
        private String color;
        private RouteType routeType;

        // Getters and setters
        public String getRouteName() { return routeName; }
        public void setRouteName(String routeName) { this.routeName = routeName; }

        public String getRouteCode() { return routeCode; }
        public void setRouteCode(String routeCode) { this.routeCode = routeCode; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Double getTotalDistance() { return totalDistance; }
        public void setTotalDistance(Double totalDistance) { this.totalDistance = totalDistance; }

        public Integer getEstimatedDuration() { return estimatedDuration; }
        public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public RouteType getRouteType() { return routeType; }
        public void setRouteType(RouteType routeType) { this.routeType = routeType; }
    }
}