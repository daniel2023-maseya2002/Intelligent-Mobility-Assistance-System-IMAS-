package IMAS.ImasProject.services;

import IMAS.ImasProject.controller.RouteController;
import IMAS.ImasProject.model.Rerouting;
import IMAS.ImasProject.model.ReroutingStatus;
import IMAS.ImasProject.model.Route;
import IMAS.ImasProject.model.RouteType;
import IMAS.ImasProject.repository.ReroutingRepository;
import IMAS.ImasProject.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class RouteService {

    @Autowired
    private ReroutingRepository reroutingRepository;

    @Autowired
    private RouteRepository routeRepository;

    private Random random = new Random();

    // Liste des destinations de Kinshasa
    private final List<String> kinshasaDestinations = Arrays.asList(
            "Gare Centrale", "Matete", "Limete", "Bandalungwa", "Ndjili",
            "Masina", "Kintambo", "Lingwala", "Barumbu", "Kinshasa",
            "Lemba", "Mont Ngafula", "Ngaliema", "Selembao", "Makala",
            "Bumbu", "Kalamu", "Ngaba", "Kimbanseke", "Maluku"
    );

    // Couleurs disponibles pour les routes
    private final List<String> routeColors = Arrays.asList(
            "#1E40AF", "#3B82F6", "#60A5FA", "#93C5FD", "#DBEAFE",
            "#1D4ED8", "#2563EB", "#3B82F6", "#60A5FA", "#93C5FD",
            "#DC2626", "#EF4444", "#F87171", "#FCA5A5", "#FECACA",
            "#059669", "#10B981", "#34D399", "#6EE7B7", "#A7F3D0",
            "#D97706", "#F59E0B", "#FBBF24", "#FCD34D", "#FDE68A",
            "#7C3AED", "#8B5CF6", "#A78BFA", "#C4B5FD", "#DDD6FE"
    );

    public Route save(Route route) {
        return routeRepository.save(route);
    }

    public List<Route> findAll() {
        return routeRepository.findAll();
    }

    public Route findById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found for ID: " + id));
    }

    public List<Route> findByCity(String city) {
        return routeRepository.findByRouteNameContaining(city);
    }

    public void deleteById(Long id) {
        if (!routeRepository.existsById(id)) {
            throw new IllegalArgumentException("Route not found for ID: " + id);
        }
        routeRepository.deleteById(id);
    }

    public void createKinshasaPredefinedRoutes() {
        createRouteIfNotExists("KIN001", "Gare Centrale - Matete", "Route principale vers Matete", 15.5, 45, RouteType.URBAN, "#1E40AF");
        createRouteIfNotExists("KIN002", "Gare Centrale - Limete", "Route vers Limete", 12.0, 35, RouteType.URBAN, "#3B82F6");
        createRouteIfNotExists("KIN003", "Gare Centrale - Bandalungwa", "Route vers Bandalungwa", 10.2, 30, RouteType.URBAN, "#60A5FA");
        createRouteIfNotExists("KIN004", "Gare Centrale - Ndjili", "Route vers l'aéroport", 25.0, 60, RouteType.SUBURBAN, "#93C5FD");
        createRouteIfNotExists("KIN005", "Gare Centrale - Masina", "Route vers Masina", 28.0, 65, RouteType.SUBURBAN, "#DBEAFE");
        createRouteIfNotExists("KIN006", "Express Gare-Ndjili", "Route express vers l'aéroport", 25.0, 40, RouteType.EXPRESS, "#1D4ED8");
    }

    /**
     * Génère des routes aléatoires en utilisant les destinations existantes
     * et en créant des combinaisons bidirectionnelles
     */
    public List<Route> generateRandomRoutes(int count) {
        List<Route> generatedRoutes = new ArrayList<>();

        // Obtenir toutes les destinations existantes des routes dans la DB
        Set<String> existingDestinations = extractDestinationsFromDatabase();

        // Combiner avec les destinations prédéfinies de Kinshasa
        Set<String> allDestinations = new HashSet<>(existingDestinations);
        allDestinations.addAll(kinshasaDestinations);

        List<String> destinationList = new ArrayList<>(allDestinations);

        if (destinationList.size() < 2) {
            // Si pas assez de destinations, utiliser les destinations par défaut
            destinationList = new ArrayList<>(kinshasaDestinations);
        }

        // Générer les routes demandées
        for (int i = 0; i < count; i++) {
            Route newRoute = generateSingleRandomRoute(destinationList, i);
            if (newRoute != null) {
                generatedRoutes.add(newRoute);
            }
        }

        return generatedRoutes;
    }

    /**
     * Extrait toutes les destinations des routes existantes dans la base de données
     */
    private Set<String> extractDestinationsFromDatabase() {
        List<Route> allRoutes = routeRepository.findAll();
        Set<String> destinations = new HashSet<>();

        for (Route route : allRoutes) {
            String routeName = route.getRouteName();
            if (routeName != null && routeName.contains(" - ")) {
                String[] parts = routeName.split(" - ");
                for (String part : parts) {
                    destinations.add(part.trim());
                }
            }
        }

        return destinations;
    }

    /**
     * Génère une seule route aléatoire
     */
    private Route generateSingleRandomRoute(List<String> destinations, int index) {
        String origin, destination;
        String routeName, routeCode;
        int attempts = 0;
        int maxAttempts = 50;

        do {
            // Sélectionner aléatoirement origine et destination
            origin = destinations.get(random.nextInt(destinations.size()));
            destination = destinations.get(random.nextInt(destinations.size()));

            // S'assurer que l'origine et la destination sont différentes
            while (origin.equals(destination)) {
                destination = destinations.get(random.nextInt(destinations.size()));
            }

            routeName = origin + " - " + destination;
            routeCode = generateRouteCode();

            attempts++;
        } while (routeExists(routeCode, routeName) && attempts < maxAttempts);

        if (attempts >= maxAttempts) {
            return null; // Impossible de générer une route unique
        }

        // Calculer distance et durée aléatoirement mais de façon réaliste
        double distance = generateRealisticDistance(origin, destination);
        int duration = (int) (distance * 2.5) + random.nextInt(15); // Environ 2.5 min par km + variation

        // Déterminer le type de route basé sur la distance
        RouteType routeType = determineRouteType(distance);

        // Sélectionner une couleur aléatoire
        String color = routeColors.get(random.nextInt(routeColors.size()));

        // Créer la route
        Route route = new Route();
        route.setRouteCode(routeCode);
        route.setRouteName(routeName);
        route.setDescription(generateRouteDescription(origin, destination, routeType));
        route.setTotalDistance(distance);
        route.setEstimatedDuration(duration);
        route.setRouteType(routeType);
        route.setColor(color);
        route.setActive(true);
        route.setCreatedAt(LocalDateTime.now());
        route.setCreatedBy("SYSTEM-GENERATOR");

        return routeRepository.save(route);
    }

    /**
     * Génère un code de route unique
     */
    private String generateRouteCode() {
        String prefix = "GEN";
        int number;
        String code;

        do {
            number = random.nextInt(9999) + 1;
            code = String.format("%s%04d", prefix, number);
        } while (routeRepository.findByRouteCode(code) != null);

        return code;
    }

    /**
     * Génère une distance réaliste basée sur les destinations
     */
    private double generateRealisticDistance(String origin, String destination) {
        // Distance de base aléatoire entre 5 et 35 km
        double baseDistance = 5.0 + (random.nextDouble() * 30.0);

        // Ajuster basé sur les destinations connues
        if (isAirportRoute(origin, destination)) {
            baseDistance = Math.max(baseDistance, 20.0); // Minimum 20km pour l'aéroport
        } else if (isCentralRoute(origin, destination)) {
            baseDistance = Math.min(baseDistance, 15.0); // Maximum 15km pour les routes centrales
        }

        return Math.round(baseDistance * 10.0) / 10.0; // Arrondir à 1 décimale
    }

    /**
     * Détermine le type de route basé sur la distance
     */
    private RouteType determineRouteType(double distance) {
        if (distance >= 30.0) {
            return RouteType.SUBURBAN;
        } else if (distance >= 20.0) {
            return random.nextBoolean() ? RouteType.SUBURBAN : RouteType.EXPRESS;
        } else {
            return RouteType.URBAN;
        }
    }

    /**
     * Génère une description pour la route
     */
    private String generateRouteDescription(String origin, String destination, RouteType routeType) {
        String[] descriptions = {
                "Route " + routeType.toString().toLowerCase() + " reliant " + origin + " à " + destination,
                "Liaison " + routeType.toString().toLowerCase() + " entre " + origin + " et " + destination,
                "Service " + routeType.toString().toLowerCase() + " " + origin + " - " + destination,
                "Connexion directe de " + origin + " vers " + destination
        };

        return descriptions[random.nextInt(descriptions.length)];
    }

    /**
     * Vérifie si c'est une route vers l'aéroport
     */
    private boolean isAirportRoute(String origin, String destination) {
        return origin.toLowerCase().contains("ndjili") || destination.toLowerCase().contains("ndjili") ||
                origin.toLowerCase().contains("aéroport") || destination.toLowerCase().contains("aéroport");
    }

    /**
     * Vérifie si c'est une route centrale
     */
    private boolean isCentralRoute(String origin, String destination) {
        List<String> centralAreas = Arrays.asList("gare centrale", "kintambo", "lingwala", "barumbu", "kinshasa");
        String originLower = origin.toLowerCase();
        String destinationLower = destination.toLowerCase();

        return centralAreas.stream().anyMatch(area ->
                originLower.contains(area) || destinationLower.contains(area));
    }

    /**
     * Vérifie si une route existe déjà
     */
    private boolean routeExists(String routeCode, String routeName) {
        return routeRepository.findByRouteCode(routeCode) != null ||
                routeRepository.findByRouteName(routeName) != null;
    }




    public Rerouting createRerouting(RouteController.ReroutingRequest request) {
        Route affectedRoute = findById(request.getAffectedRouteId());
        Route alternativeRoute = findById(request.getAlternativeRouteId());

        Rerouting rerouting = new Rerouting();
        rerouting.setAffectedRoute(affectedRoute);
        rerouting.setAlternativeRoute(alternativeRoute);
        rerouting.setReason(request.getReason());
        rerouting.setEstimatedDuration(request.getEstimatedDuration());
        rerouting.setAdditionalNotes(request.getAdditionalNotes());
        rerouting.setCreatedBy("SYSTEM");

        return reroutingRepository.save(rerouting);
    }

    public List<Rerouting> getActiveReroutings() {
        return reroutingRepository.findByStatus(ReroutingStatus.ACTIVE);
    }

    public List<Rerouting> getAllReroutings() {
        return reroutingRepository.findAllByOrderByCreatedAtDesc();
    }

    public void endRerouting(Long reroutingId) {
        Rerouting rerouting = reroutingRepository.findById(reroutingId)
                .orElseThrow(() -> new IllegalArgumentException("Rerouting not found"));

        rerouting.setStatus(ReroutingStatus.ENDED);
        rerouting.setEndTime(LocalDateTime.now());
        reroutingRepository.save(rerouting);
    }
    private void createRouteIfNotExists(String code, String name, String description, double distance, int duration, RouteType type, String color) {
        if (routeRepository.findByRouteCode(code) == null) {
            Route route = new Route();
            route.setRouteCode(code);
            route.setRouteName(name);
            route.setDescription(description);
            route.setTotalDistance(distance);
            route.setEstimatedDuration(duration);
            route.setRouteType(type);
            route.setColor(color);
            route.setActive(true);
            route.setCreatedAt(LocalDateTime.now());
            route.setCreatedBy("SYSTEM");
            routeRepository.save(route);
        }
    }
}