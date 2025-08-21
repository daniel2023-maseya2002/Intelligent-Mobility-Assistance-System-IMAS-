package IMAS.ImasProject.services;


import IMAS.ImasProject.dto.TrafficDataDTO;
import IMAS.ImasProject.dto.TrafficQueryDTO;
import IMAS.ImasProject.model.TrafficData;
import IMAS.ImasProject.repository.TrafficDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrafficDataService {

    @Autowired
    private TrafficDataRepository trafficDataRepository;

    // Créer une nouvelle donnée de trafic
    public TrafficDataDTO createTrafficData(TrafficDataDTO dto) {
        TrafficData trafficData = convertToEntity(dto);
        trafficData.setTimestamp(LocalDateTime.now());
        trafficData.setDayOfWeek(LocalDateTime.now().getDayOfWeek().getValue());
        trafficData.setHourOfDay(LocalDateTime.now().getHour());

        TrafficData saved = trafficDataRepository.save(trafficData);
        return convertToDTO(saved);
    }

    // Mettre à jour une donnée existante
    public TrafficDataDTO updateTrafficData(Long id, TrafficDataDTO dto) {
        Optional<TrafficData> existing = trafficDataRepository.findById(id);
        if (existing.isPresent()) {
            TrafficData trafficData = existing.get();
            updateEntityFromDTO(trafficData, dto);
            TrafficData saved = trafficDataRepository.save(trafficData);
            return convertToDTO(saved);
        }
        throw new RuntimeException("TrafficData not found with id: " + id);
    }

    // Récupérer par ID
    public Optional<TrafficDataDTO> getTrafficDataById(Long id) {
        return trafficDataRepository.findById(id).map(this::convertToDTO);
    }

    // Rechercher par zone géographique
    public List<TrafficDataDTO> findByGeographicArea(TrafficQueryDTO query) {
        LocalDateTime since = query.getStartTime() != null ?
                query.getStartTime() : LocalDateTime.now().minusHours(1);

        Double latMin = query.getLatitude() - query.getRadius();
        Double latMax = query.getLatitude() + query.getRadius();
        Double lonMin = query.getLongitude() - query.getRadius();
        Double lonMax = query.getLongitude() + query.getRadius();

        List<TrafficData> results = trafficDataRepository.findByGeographicAreaAndSince(
                latMin, latMax, lonMin, lonMax, since);

        return results.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Données récentes pour une localisation
    public List<TrafficDataDTO> findRecentByLocation(Double latitude, Double longitude, Double radius) {
        List<TrafficData> results = trafficDataRepository.findRecentByLocation(latitude, longitude, radius);
        return results.stream()
                .limit(50) // Limiter à 50 résultats récents
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Obtenir le niveau de trafic actuel pour une zone
    public Integer getCurrentTrafficLevel(Double latitude, Double longitude, Double radius) {
        List<TrafficData> recentData = trafficDataRepository.findRecentByLocation(latitude, longitude, radius);

        if (recentData.isEmpty()) {
            return 1; // Niveau par défaut si pas de données
        }

        // Calculer la moyenne des 10 dernières données
        return recentData.stream()
                .limit(10)
                .mapToInt(TrafficData::getTrafficLevel)
                .sum() / Math.min(10, recentData.size());
    }

    // Statistiques de trafic par heure
    public List<Object[]> getTrafficStatsByHour(Double latMin, Double latMax,
                                                Double lonMin, Double lonMax,
                                                LocalDateTime since) {
        return trafficDataRepository.getAverageTrafficByHour(latMin, latMax, lonMin, lonMax, since);
    }

    // Points chauds de trafic
    public List<Object[]> getTrafficHotspots(LocalDateTime since, Double minLevel) {
        return trafficDataRepository.findTrafficHotspots(since, minLevel);
    }

    // Données pour l'entraînement ML
    public List<TrafficDataDTO> getTrainingData(LocalDateTime startDate, LocalDateTime endDate) {
        List<TrafficData> data = trafficDataRepository.findTrainingData(startDate, endDate);
        return data.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Données des heures de pointe
    public List<TrafficDataDTO> getRushHourData(LocalDateTime since) {
        List<TrafficData> data = trafficDataRepository.findRushHourData(since);
        return data.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Supprimer une donnée
    public void deleteTrafficData(Long id) {
        trafficDataRepository.deleteById(id);
    }

    // Supprimer les anciennes données (plus de 30 jours)
    @Scheduled(cron = "0 0 2 * * ?") // Tous les jours à 2h du matin
    public void cleanupOldData() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        trafficDataRepository.deleteByTimestampBefore(cutoffDate);
    }

    // Pagination pour toutes les données
    public Page<TrafficDataDTO> getAllTrafficData(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<TrafficData> dataPage = trafficDataRepository.findAll(pageable);
        return dataPage.map(this::convertToDTO);
    }

    // Recherche avancée avec critères multiples
    public List<TrafficDataDTO> advancedSearch(TrafficQueryDTO query) {
        // Cette méthode pourrait utiliser Criteria API pour des requêtes complexes
        // Pour la simplicité, nous utilisons les méthodes du repository
        LocalDateTime start = query.getStartTime() != null ?
                query.getStartTime() : LocalDateTime.now().minusHours(24);
        LocalDateTime end = query.getEndTime() != null ?
                query.getEndTime() : LocalDateTime.now();

        List<TrafficData> results = trafficDataRepository.findTrainingData(start, end);

        // Filtrage en mémoire pour les critères complexes
        return results.stream()
                .filter(td -> query.getMinTrafficLevel() == null || td.getTrafficLevel() >= query.getMinTrafficLevel())
                .filter(td -> query.getMaxTrafficLevel() == null || td.getTrafficLevel() <= query.getMaxTrafficLevel())
                .filter(td -> query.getWeatherCondition() == null || query.getWeatherCondition().equals(td.getWeatherCondition()))
                .filter(td -> query.getIsHoliday() == null || query.getIsHoliday().equals(td.getIsHoliday()))
                .filter(td -> query.getDayOfWeek() == null || query.getDayOfWeek().equals(td.getDayOfWeek()))
                .filter(td -> query.getHourOfDay() == null || query.getHourOfDay().equals(td.getHourOfDay()))
                .filter(td -> query.getRoadType() == null || query.getRoadType().equals(td.getRoadType()))
                .filter(td -> query.getEventType() == null || query.getEventType().equals(td.getEventType()))
                .limit(query.getLimit())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Méthodes de conversion
    private TrafficDataDTO convertToDTO(TrafficData entity) {
        TrafficDataDTO dto = new TrafficDataDTO();
        dto.setId(entity.getId());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        dto.setTimestamp(entity.getTimestamp());
        dto.setTrafficLevel(entity.getTrafficLevel());
        dto.setAverageSpeed(entity.getAverageSpeed());
        dto.setWeatherCondition(entity.getWeatherCondition());
        dto.setIsHoliday(entity.getIsHoliday());
        dto.setDayOfWeek(entity.getDayOfWeek());
        dto.setHourOfDay(entity.getHourOfDay());
        dto.setVehicleCount(entity.getVehicleCount());
        dto.setVisibility(entity.getVisibility());
        dto.setTemperature(entity.getTemperature());
        dto.setHumidity(entity.getHumidity());
        dto.setRoadType(entity.getRoadType());
        dto.setEventType(entity.getEventType());
        dto.setTrafficLevelDescription(entity.getTrafficLevelDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    private TrafficData convertToEntity(TrafficDataDTO dto) {
        TrafficData entity = new TrafficData();
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setTimestamp(dto.getTimestamp());
        entity.setTrafficLevel(dto.getTrafficLevel());
        entity.setAverageSpeed(dto.getAverageSpeed());
        entity.setWeatherCondition(dto.getWeatherCondition());
        entity.setIsHoliday(dto.getIsHoliday());
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setHourOfDay(dto.getHourOfDay());
        entity.setVehicleCount(dto.getVehicleCount());
        entity.setVisibility(dto.getVisibility());
        entity.setTemperature(dto.getTemperature());
        entity.setHumidity(dto.getHumidity());
        entity.setRoadType(dto.getRoadType());
        entity.setEventType(dto.getEventType());
        return entity;
    }

    private void updateEntityFromDTO(TrafficData entity, TrafficDataDTO dto) {
        if (dto.getLatitude() != null) entity.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) entity.setLongitude(dto.getLongitude());
        if (dto.getTrafficLevel() != null) entity.setTrafficLevel(dto.getTrafficLevel());
        if (dto.getAverageSpeed() != null) entity.setAverageSpeed(dto.getAverageSpeed());
        if (dto.getWeatherCondition() != null) entity.setWeatherCondition(dto.getWeatherCondition());
        if (dto.getIsHoliday() != null) entity.setIsHoliday(dto.getIsHoliday());
        if (dto.getVehicleCount() != null) entity.setVehicleCount(dto.getVehicleCount());
        if (dto.getVisibility() != null) entity.setVisibility(dto.getVisibility());
        if (dto.getTemperature() != null) entity.setTemperature(dto.getTemperature());
        if (dto.getHumidity() != null) entity.setHumidity(dto.getHumidity());
        if (dto.getRoadType() != null) entity.setRoadType(dto.getRoadType());
        if (dto.getEventType() != null) entity.setEventType(dto.getEventType());

        // Mettre à jour les champs calculés
        entity.setDayOfWeek(LocalDateTime.now().getDayOfWeek().getValue());
        entity.setHourOfDay(LocalDateTime.now().getHour());
    }
}