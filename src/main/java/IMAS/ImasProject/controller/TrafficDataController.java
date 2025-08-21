package IMAS.ImasProject.controller;

import IMAS.ImasProject.dto.TrafficDataDTO;
import IMAS.ImasProject.dto.TrafficQueryDTO;
import IMAS.ImasProject.services.TrafficDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/traffic-data")
@CrossOrigin(origins = "*")
@Tag(name = "Traffic Data", description = "API pour la gestion des données de trafic")
public class TrafficDataController {

    @Autowired
    private TrafficDataService trafficDataService;

    @PostMapping
    @Operation(summary = "Créer une nouvelle donnée de trafic")
    public ResponseEntity<TrafficDataDTO> createTrafficData(@Valid @RequestBody TrafficDataDTO dto) {
        try {
            TrafficDataDTO created = trafficDataService.createTrafficData(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une donnée de trafic par ID")
    public ResponseEntity<TrafficDataDTO> getTrafficDataById(
            @Parameter(description = "ID de la donnée de trafic") @PathVariable Long id) {
        Optional<TrafficDataDTO> trafficData = trafficDataService.getTrafficDataById(id);
        return trafficData.map(dto -> ResponseEntity.ok(dto))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une donnée de trafic")
    public ResponseEntity<TrafficDataDTO> updateTrafficData(
            @PathVariable Long id, @Valid @RequestBody TrafficDataDTO dto) {
        try {
            TrafficDataDTO updated = trafficDataService.updateTrafficData(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une donnée de trafic")
    public ResponseEntity<Void> deleteTrafficData(@PathVariable Long id) {
        try {
            trafficDataService.deleteTrafficData(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Récupérer toutes les données de trafic avec pagination")
    public ResponseEntity<Page<TrafficDataDTO>> getAllTrafficData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy) {
        Page<TrafficDataDTO> trafficData = trafficDataService.getAllTrafficData(page, size, sortBy);
        return ResponseEntity.ok(trafficData);
    }

    @GetMapping("/nearby")
    @Operation(summary = "Récupérer les données de trafic à proximité")
    public ResponseEntity<List<TrafficDataDTO>> getNearbyTrafficData(
            @Parameter(description = "Latitude") @RequestParam Double latitude,
            @Parameter(description = "Longitude") @RequestParam Double longitude,
            @Parameter(description = "Rayon en degrés") @RequestParam(defaultValue = "0.01") Double radius) {

        List<TrafficDataDTO> nearbyData = trafficDataService.findRecentByLocation(latitude, longitude, radius);
        return ResponseEntity.ok(nearbyData);
    }

    @GetMapping("/current-level")
    @Operation(summary = "Obtenir le niveau de trafic actuel pour une zone")
    public ResponseEntity<Map<String, Object>> getCurrentTrafficLevel(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "0.01") Double radius) {

        Integer currentLevel = trafficDataService.getCurrentTrafficLevel(latitude, longitude, radius);
        String description = getTrafficLevelDescription(currentLevel);

        Map<String, Object> response = Map.of(
                "latitude", latitude,
                "longitude", longitude,
                "radius", radius,
                "trafficLevel", currentLevel,
                "description", description,
                "timestamp", LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    @Operation(summary = "Recherche avancée de données de trafic")
    public ResponseEntity<List<TrafficDataDTO>> searchTrafficData(@RequestBody TrafficQueryDTO query) {
        List<TrafficDataDTO> results = trafficDataService.advancedSearch(query);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/area")
    @Operation(summary = "Récupérer les données de trafic par zone géographique")
    public ResponseEntity<List<TrafficDataDTO>> getTrafficDataByArea(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "0.01") Double radius,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime) {

        TrafficQueryDTO query = new TrafficQueryDTO(latitude, longitude, radius);
        if (startTime != null) {
            query.setStartTime(startTime);
        }

        List<TrafficDataDTO> areaData = trafficDataService.findByGeographicArea(query);
        return ResponseEntity.ok(areaData);
    }

    @GetMapping("/hotspots")
    @Operation(summary = "Récupérer les points chauds de trafic")
    public ResponseEntity<List<Map<String, Object>>> getTrafficHotspots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @RequestParam(defaultValue = "3.0") Double minLevel) {

        LocalDateTime sinceTime = since != null ? since : LocalDateTime.now().minusHours(24);
        List<Object[]> hotspots = trafficDataService.getTrafficHotspots(sinceTime, minLevel);

        List<Map<String, Object>> response = hotspots.stream()
                .map(row -> Map.of(
                        "latitude", row[0],
                        "longitude", row[1],
                        "averageLevel", row[2]
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats/hourly")
    @Operation(summary = "Statistiques de trafic par heure")
    public ResponseEntity<List<Map<String, Object>>> getHourlyTrafficStats(
            @RequestParam Double latMin,
            @RequestParam Double latMax,
            @RequestParam Double lonMin,
            @RequestParam Double lonMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        LocalDateTime sinceTime = since != null ? since : LocalDateTime.now().minusDays(7);
        List<Object[]> stats = trafficDataService.getTrafficStatsByHour(latMin, latMax, lonMin, lonMax, sinceTime);

        List<Map<String, Object>> response = stats.stream()
                .map(row -> Map.of(
                        "hour", row[0],
                        "averageTrafficLevel", row[1],
                        "averageSpeed", row[2]
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/rush-hour")
    @Operation(summary = "Données de trafic des heures de pointe")
    public ResponseEntity<List<TrafficDataDTO>> getRushHourData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        LocalDateTime sinceTime = since != null ? since : LocalDateTime.now().minusDays(7);
        List<TrafficDataDTO> rushHourData = trafficDataService.getRushHourData(sinceTime);
        return ResponseEntity.ok(rushHourData);
    }

    @GetMapping("/training-data")
    @Operation(summary = "Données pour l'entraînement ML")
    public ResponseEntity<List<TrafficDataDTO>> getTrainingData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<TrafficDataDTO> trainingData = trafficDataService.getTrainingData(startDate, endDate);
        return ResponseEntity.ok(trainingData);
    }

    @PostMapping("/batch")
    @Operation(summary = "Créer plusieurs données de trafic en lot")
    public ResponseEntity<List<TrafficDataDTO>> createBatchTrafficData(
            @RequestBody @Valid List<TrafficDataDTO> trafficDataList) {
        try {
            List<TrafficDataDTO> createdData = trafficDataList.stream()
                    .map(trafficDataService::createTrafficData)
                    .toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(createdData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/summary")
    @Operation(summary = "Résumé des données de trafic")
    public ResponseEntity<Map<String, Object>> getTrafficSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {

        LocalDateTime sinceTime = since != null ? since : LocalDateTime.now().minusHours(24);

        // Récupérer les données récentes pour le calcul du résumé
        TrafficQueryDTO query = new TrafficQueryDTO();
        query.setStartTime(sinceTime);
        query.setLimit(1000);

        List<TrafficDataDTO> recentData = trafficDataService.advancedSearch(query);

        // Calculer les statistiques
        double avgTrafficLevel = recentData.stream()
                .mapToInt(TrafficDataDTO::getTrafficLevel)
                .average()
                .orElse(0.0);

        double avgSpeed = recentData.stream()
                .mapToDouble(TrafficDataDTO::getAverageSpeed)
                .average()
                .orElse(0.0);

        long totalRecords = recentData.size();

        // Correction du mapping : conversion Integer -> String pour les clés
        Map<String, Long> levelCounts = recentData.stream()
                .collect(Collectors.groupingBy(
                        data -> String.valueOf(data.getTrafficLevel()), // Conversion Integer -> String
                        Collectors.counting()
                ));

        Map<String, Object> summary = Map.of(
                "period", Map.of("since", sinceTime, "until", LocalDateTime.now()),
                "totalRecords", totalRecords,
                "averageTrafficLevel", Math.round(avgTrafficLevel * 100.0) / 100.0,
                "averageSpeed", Math.round(avgSpeed * 100.0) / 100.0,
                "levelDistribution", levelCounts
        );

        return ResponseEntity.ok(summary);
    }

    // Méthode utilitaire pour la description du niveau de trafic
    private String getTrafficLevelDescription(Integer level) {
        if (level == null) return "Inconnu";
        switch (level) {
            case 1: return "Fluide";
            case 2: return "Modéré";
            case 3: return "Dense";
            case 4: return "Embouteillé";
            case 5: return "Bloqué";
            default: return "Inconnu";
        }
    }
}