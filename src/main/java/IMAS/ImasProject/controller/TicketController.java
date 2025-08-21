package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.*;
import IMAS.ImasProject.services.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    private BusService busService;

    @Autowired
    private TripService tripService;

    @Autowired
    private StaffService staffService;

    // NOUVEAU: Injection des services de notification
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TicketNotificationService ticketNotificationService;

    /**
     * Valider la disponibilité d'un siège pour un voyage spécifique
     */
    @PostMapping("/validate-seat")
    public ResponseEntity<?> validateSeatAvailability(@RequestBody SeatValidationRequest request) {
        try {
            log.info("Validating seat availability: {}", request);

            // Validation des paramètres requis
            if (request.getBusId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "available", false,
                        "message", "Bus ID is required"
                ));
            }

            if (request.getSeatNumber() == null || request.getSeatNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "available", false,
                        "message", "Seat number is required"
                ));
            }

            if (request.getDepartureTime() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "available", false,
                        "message", "Departure time is required"
                ));
            }

            // Vérifier que le bus existe
            Bus bus = busService.findById(request.getBusId());
            if (bus == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "available", false,
                        "message", "Bus not found with ID: " + request.getBusId()
                ));
            }

            // Parser l'heure de départ
            LocalDateTime departureTime;
            try {
                departureTime = LocalDateTime.parse(request.getDepartureTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "available", false,
                        "message", "Invalid departure time format"
                ));
            }

            // Chercher les tickets existants pour ce voyage spécifique
            List<Ticket> existingTickets = ticketService.findByBusId(request.getBusId());

            boolean seatTaken = existingTickets.stream().anyMatch(ticket -> {
                // Vérifier si c'est le même voyage (même heure, origine et destination)
                boolean sameTime = Math.abs(ChronoUnit.MINUTES.between(ticket.getDepartureTime(), departureTime)) < 1;
                boolean sameRoute = (request.getOrigin() == null || request.getOrigin().equals(ticket.getOrigin())) &&
                        (request.getDestination() == null || request.getDestination().equals(ticket.getDestination()));
                boolean sameSeat = request.getSeatNumber().equals(ticket.getSeatNumber());
                boolean validStatus = "PAID".equals(ticket.getStatus()) || "BOARDED".equals(ticket.getStatus());

                boolean isConflict = sameTime && sameRoute && sameSeat && validStatus;

                if (isConflict) {
                    log.warn("Seat conflict found - Ticket ID: {}, Seat: {}, Status: {}, Time: {}",
                            ticket.getId(), ticket.getSeatNumber(), ticket.getStatus(), ticket.getDepartureTime());
                }

                return isConflict;
            });

            if (seatTaken) {
                return ResponseEntity.ok(Map.of(
                        "available", false,
                        "message", "Seat " + request.getSeatNumber() + " is already taken for this trip"
                ));
            }

            // Vérifier que le numéro de siège est valide pour ce bus
            String seatNumber = request.getSeatNumber();
            try {
                int seatNum = Integer.parseInt(seatNumber);
                if (seatNum < 1 || seatNum > bus.getCapacity()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "available", false,
                            "message", "Seat number " + seatNumber + " is invalid for this bus (capacity: " + bus.getCapacity() + ")"
                    ));
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "available", false,
                        "message", "Invalid seat number format: " + seatNumber
                ));
            }

            // Le siège est disponible
            return ResponseEntity.ok(Map.of(
                    "available", true,
                    "message", "Seat " + request.getSeatNumber() + " is available for this trip",
                    "busId", request.getBusId(),
                    "seatNumber", request.getSeatNumber(),
                    "departureTime", request.getDepartureTime()
            ));

        } catch (Exception e) {
            log.error("Error validating seat availability: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "available", false,
                    "message", "Error validating seat: " + e.getMessage()
            ));
        }
    }

    /**
     * Obtenir les tickets pour un bus et un horaire spécifiques
     */
    @PostMapping("/bus/{busId}/departure/specific")
    public ResponseEntity<?> getTicketsForSpecificTrip(
            @PathVariable Long busId,
            @RequestBody TripFilterRequest request) {
        try {
            log.info("Getting tickets for specific trip - Bus: {}, Request: {}", busId, request);

            // Valider les paramètres
            if (request.getDepartureTime() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Departure time is required"
                ));
            }

            // Parser l'heure de départ
            LocalDateTime departureTime;
            try {
                departureTime = LocalDateTime.parse(request.getDepartureTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Invalid departure time format"
                ));
            }

            // Récupérer tous les tickets du bus
            List<Ticket> allBusTickets = ticketService.findByBusId(busId);

            // Filtrer pour le voyage spécifique
            List<Ticket> specificTripTickets = allBusTickets.stream()
                    .filter(ticket -> {
                        // Vérifier l'heure (tolérance de 1 minute)
                        boolean sameTime = Math.abs(ChronoUnit.MINUTES.between(ticket.getDepartureTime(), departureTime)) < 1;

                        // Vérifier le trajet si spécifié
                        boolean sameRoute = true;
                        if (request.getOrigin() != null && ticket.getOrigin() != null) {
                            sameRoute = sameRoute && request.getOrigin().equals(ticket.getOrigin());
                        }
                        if (request.getDestination() != null && ticket.getDestination() != null) {
                            sameRoute = sameRoute && request.getDestination().equals(ticket.getDestination());
                        }

                        // Vérifier le statut
                        boolean validStatus = "PAID".equals(ticket.getStatus()) || "BOARDED".equals(ticket.getStatus());

                        return sameTime && sameRoute && validStatus;
                    })
                    .toList();

            log.info("Found {} tickets for specific trip", specificTripTickets.size());
            return ResponseEntity.ok(specificTripTickets);

        } catch (Exception e) {
            log.error("Error getting tickets for specific trip: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error retrieving tickets: " + e.getMessage()
            ));
        }
    }

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody TicketCreateRequest request) {
        try {
            log.info("Received ticket creation request: {}", request);

            // Validate required fields
            if (request.getBusId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bus ID is required"));
            }

            if (request.getPassengerId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Passenger ID is required"));
            }

            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "First name is required"));
            }

            if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Last name is required"));
            }

            if (request.getSeatNumber() == null || request.getSeatNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Seat number is required"));
            }

            // Validate bus exists and has capacity
            Bus bus = busService.findById(request.getBusId());
            if (bus == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Bus not found with ID: " + request.getBusId()));
            }

            log.info("Found bus: {} (ID: {})", bus.getName(), bus.getId());

            // Check bus status
            if (bus.getHasAccident()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Cannot create ticket for a bus that has an accident"));
            }

            // Parse departure time
            LocalDateTime departureTime;
            if (request.getDepartureTime() != null) {
                if (request.getDepartureTime() instanceof String) {
                    departureTime = LocalDateTime.parse((String) request.getDepartureTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } else {
                    departureTime = (LocalDateTime) request.getDepartureTime();
                }
            } else {
                // Default to bus departure time or current time + 1 hour
                departureTime = bus.getDepartureTime() != null ? bus.getDepartureTime() : LocalDateTime.now().plusHours(1);
            }

            // VALIDATION CRITIQUE : Vérifier que le siège n'est pas déjà pris pour ce voyage spécifique
            List<Ticket> existingTickets = ticketService.findByBusId(request.getBusId());
            boolean seatAlreadyTaken = existingTickets.stream().anyMatch(ticket -> {
                boolean sameTime = Math.abs(ChronoUnit.MINUTES.between(ticket.getDepartureTime(), departureTime)) < 1;
                boolean sameRoute = (request.getOrigin() == null || request.getOrigin().equals(ticket.getOrigin())) &&
                        (request.getDestination() == null || request.getDestination().equals(ticket.getDestination()));
                boolean sameSeat = request.getSeatNumber().equals(ticket.getSeatNumber());
                boolean validStatus = "PAID".equals(ticket.getStatus()) || "BOARDED".equals(ticket.getStatus());

                return sameTime && sameRoute && sameSeat && validStatus;
            });

            if (seatAlreadyTaken) {
                log.warn("Attempt to book already taken seat: {} for bus {} at {}",
                        request.getSeatNumber(), request.getBusId(), departureTime);
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Seat " + request.getSeatNumber() + " is already taken for this trip. Please select another seat.",
                        "errorCode", "SEAT_ALREADY_TAKEN",
                        "seatNumber", request.getSeatNumber()
                ));
            }

            // Check bus capacity
            long currentPassengersForTrip = existingTickets.stream()
                    .filter(ticket -> {
                        boolean sameTime = Math.abs(ChronoUnit.MINUTES.between(ticket.getDepartureTime(), departureTime)) < 1;
                        boolean sameRoute = (request.getOrigin() == null || request.getOrigin().equals(ticket.getOrigin())) &&
                                (request.getDestination() == null || request.getDestination().equals(ticket.getDestination()));
                        boolean validStatus = "PAID".equals(ticket.getStatus()) || "BOARDED".equals(ticket.getStatus());
                        return sameTime && sameRoute && validStatus;
                    })
                    .count();

            if (currentPassengersForTrip >= bus.getCapacity()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Bus is full for this trip. Current passengers: " + currentPassengersForTrip + "/" + bus.getCapacity(),
                        "errorCode", "BUS_FULL"
                ));
            }

            // Create ticket
            Ticket ticket = new Ticket();
            ticket.setPassengerId(request.getPassengerId());
            ticket.setFirstName(request.getFirstName().trim());
            ticket.setLastName(request.getLastName().trim());
            ticket.setTicketNumber(generateTicketNumber());
            ticket.setReservationCode(generateReservationCode());
            ticket.setOrigin(request.getOrigin());
            ticket.setDestination(request.getDestination());
            ticket.setDepartureTime(departureTime);
            ticket.setBoardingTime(departureTime.minusMinutes(30));
            ticket.setBus(bus);
            ticket.setDriver(bus.getDriver());
            ticket.setSeatNumber(request.getSeatNumber().trim());
            ticket.setLuggageWeight(request.getLuggageWeight() != null ? request.getLuggageWeight() : 0.0);
            ticket.setHasLuggage(request.getHasLuggage() != null ? request.getHasLuggage() : false);
            ticket.setStatus("PAID");
            ticket.setIssuedAt(LocalDateTime.now());

            // Use the new service method that handles ID validation and QR code generation
            Ticket savedTicket = ticketService.saveWithQrCode(ticket);
            log.info("Ticket created successfully with ID: {} for seat {}", savedTicket.getId(), savedTicket.getSeatNumber());

            // Update bus passenger count (global count, not trip-specific)
            bus.setPassengers(bus.getPassengers() + 1);
            busService.save(bus);

            // NOUVEAU: Créer des notifications après l'achat du ticket
            try {
                // Notification pour le passager
                ticketNotificationService.notifyTicketPurchase(savedTicket);

                // Notification pour le conducteur (optionnel)
                ticketNotificationService.notifyDriverNewPassenger(savedTicket);

            } catch (Exception notificationError) {
                // Ne pas faire échouer la création du ticket à cause de l'erreur de notification
                log.error("Error creating notifications after ticket purchase for passenger {}: {}",
                        request.getPassengerId(), notificationError.getMessage(), notificationError);
            }

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("ticket", savedTicket);
            response.put("paymentAmount", calculatePaymentAmount(request));
            response.put("qrData", ticketService.getQrData(savedTicket));
            response.put("message", "Ticket created successfully for seat " + savedTicket.getSeatNumber());

            // Include QR code information in response
            if (savedTicket.hasQrCode()) {
                response.put("qrCode", savedTicket.getQrCode());
                response.put("hasQrCode", true);
                response.put("qrCodeSize", savedTicket.getQrCodeSize());
            } else {
                response.put("hasQrCode", false);
                response.put("qrCodeGenerationFailed", true);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating ticket: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Error creating ticket: " + e.getMessage(),
                    "errorCode", "CREATION_FAILED"
            ));
        }
    }

    // [Le reste des méthodes reste inchangé - je les garde pour la complétude]

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        return ResponseEntity.ok(ticketService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.findById(id);
        if (ticket != null) {
            return ResponseEntity.ok(ticket);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/generate-qr")
    public ResponseEntity<?> generateQrCodeForTicket(@PathVariable Long id) {
        try {
            log.info("Generating QR code for ticket ID: {}", id);

            // Validate ticket exists
            Ticket ticket = ticketService.findById(id);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if ticket ID is valid
            if (ticket.getId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Cannot generate QR code: Ticket ID is null",
                        "ticketId", id
                ));
            }

            // Check if ticket number exists
            if (ticket.getTicketNumber() == null || ticket.getTicketNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Cannot generate QR code: Ticket number is missing",
                        "ticketId", id
                ));
            }

            // Generate QR code using the service method
            Ticket updatedTicket = ticketService.generateQrCodeForExistingTicket(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "QR code generated successfully");
            response.put("ticketId", updatedTicket.getId());
            response.put("ticketNumber", updatedTicket.getTicketNumber());
            response.put("qrData", ticketService.getQrData(updatedTicket));
            response.put("qrCodeSize", updatedTicket.getQrCodeSize());
            response.put("hasQrCode", updatedTicket.hasQrCode());

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            log.error("Ticket not found for ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Invalid state for QR generation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "message", e.getMessage(),
                    "ticketId", id
            ));
        } catch (Exception e) {
            log.error("Error generating QR code for ticket {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error generating QR code: " + e.getMessage(),
                    "ticketId", id
            ));
        }
    }

    @GetMapping("/{id}/qr-code")
    public ResponseEntity<byte[]> getTicketQrCode(@PathVariable Long id) {
        try {
            Ticket ticket = ticketService.findById(id);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            // Generate QR code if it doesn't exist
            if (!ticket.hasQrCode()) {
                log.info("QR code not found for ticket {}, generating new one", id);
                ticket = ticketService.generateQrCodeForExistingTicket(id);
            }

            // Validate QR code exists after generation
            if (!ticket.hasQrCode()) {
                log.error("Failed to generate QR code for ticket {}", id);
                return ResponseEntity.internalServerError().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("ticket-" + ticket.getTicketNumber() + ".png")
                    .build());

            return new ResponseEntity<>(ticket.getQrCode(), headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error retrieving QR code for ticket {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/scan-qr")
    public ResponseEntity<?> scanQrCode(@RequestBody Map<String, String> request) {
        try {
            log.info("Received QR scan request: {}", request);

            // Basic validation
            if (request == null || !request.containsKey("qrData")) {
                log.warn("QR scan request missing qrData");
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", "QR data is required"
                ));
            }

            String qrData = request.get("qrData");
            if (qrData == null || qrData.trim().isEmpty()) {
                log.warn("QR data is empty");
                return ResponseEntity.badRequest().body(Map.of(
                        "valid", false,
                        "message", "QR data cannot be empty"
                ));
            }

            log.info("Processing QR data: {}", qrData);

            // Use the service method to verify QR code
            Ticket ticket = ticketService.verifyQrCode(qrData);

            // Check ticket status
            if ("BOARDED".equals(ticket.getStatus())) {
                log.info("Ticket already used: {}", ticket.getTicketNumber());
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Ticket already used",
                        "ticketNumber", ticket.getTicketNumber(),
                        "passenger", ticket.getFullName(),
                        "status", ticket.getStatus()
                ));
            }

            if (!"PAID".equals(ticket.getStatus())) {
                log.info("Ticket not paid: {}", ticket.getTicketNumber());
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Ticket not paid",
                        "ticketNumber", ticket.getTicketNumber(),
                        "passenger", ticket.getFullName(),
                        "status", ticket.getStatus()
                ));
            }

            // Check departure time
            if (ticket.getDepartureTime().plusHours(1).isBefore(LocalDateTime.now())) {
                log.info("Departure time has passed for ticket: {}", ticket.getTicketNumber());
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "message", "Departure time has passed",
                        "ticketNumber", ticket.getTicketNumber(),
                        "passenger", ticket.getFullName(),
                        "departureTime", ticket.getDepartureTime().toString()
                ));
            }

            // Update ticket status
            ticket.setStatus("BOARDED");
            ticket.setBoardingTime(LocalDateTime.now());
            ticketService.save(ticket);

            // NOUVEAU: Créer une notification pour l'embarquement réussi
            try {
                ticketNotificationService.notifySuccessfulBoarding(ticket);
            } catch (Exception notificationError) {
                log.error("Error creating boarding notification for passenger {}: {}",
                        ticket.getPassengerId(), notificationError.getMessage());
            }

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("message", "Ticket scanned successfully");
            response.put("ticketId", ticket.getId());
            response.put("ticketNumber", ticket.getTicketNumber());
            response.put("passenger", ticket.getFullName());
            response.put("bus", ticket.getBus() != null ? ticket.getBus().getName() : "N/A");
            response.put("destination", ticket.getDestination());
            response.put("seatNumber", ticket.getSeatNumber() != null ? ticket.getSeatNumber() : "Not assigned");
            response.put("boardingTime", ticket.getBoardingTime().toString());

            log.info("Ticket scanned successfully: {}", ticket.getTicketNumber());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid QR code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", "Invalid QR code: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error scanning QR code: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "valid", false,
                    "message", "Internal server error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<?> getTicketByNumber(@PathVariable String ticketNumber) {
        try {
            log.info("Fetching ticket by number: {}", ticketNumber);
            Ticket ticket = ticketService.findByTicketNumber(ticketNumber);
            if (ticket == null) {
                log.warn("Ticket not found for ticket number: {}", ticketNumber);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "valid", false,
                        "message", "Ticket not found for ticket number: " + ticketNumber
                ));
            }
            log.info("Ticket found: ID {}, Number {}", ticket.getId(), ticket.getTicketNumber());
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            log.error("Error fetching ticket by number {}: {}", ticketNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "valid", false,
                    "message", "Error fetching ticket: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<Ticket>> getTicketsByPassenger(@PathVariable Long passengerId) {
        List<Ticket> tickets = ticketService.findByPassengerId(passengerId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/bus/{busId}")
    public ResponseEntity<List<Ticket>> getTicketsByBus(@PathVariable Long busId) {
        List<Ticket> tickets = ticketService.findByBusId(busId);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/calculate-price")
    public ResponseEntity<Map<String, Object>> calculatePrice(@RequestBody PriceCalculationRequest request) {
        double basePrice = 2000.0; // Base price in Congolese Francs
        double distanceMultiplier = calculateDistanceMultiplier(request.getOrigin(), request.getDestination());
        double luggagePrice = request.getHasLuggage() ? 500.0 : 0.0;

        double totalPrice = (basePrice * distanceMultiplier) + luggagePrice;

        Map<String, Object> response = new HashMap<>();
        response.put("basePrice", basePrice);
        response.put("distanceMultiplier", distanceMultiplier);
        response.put("luggagePrice", luggagePrice);
        response.put("totalPrice", totalPrice);
        response.put("currency", "FC"); // Franc Congolais

        return ResponseEntity.ok(response);
    }

    // Helper methods
    private String generateTicketNumber() {
        return "TKT" + System.currentTimeMillis();
    }

    private String generateReservationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private double calculatePaymentAmount(TicketCreateRequest request) {
        double basePrice = 2000.0;
        double distanceMultiplier = calculateDistanceMultiplier(request.getOrigin(), request.getDestination());
        double luggagePrice = request.getHasLuggage() ? 500.0 : 0.0;
        return (basePrice * distanceMultiplier) + luggagePrice;
    }

    private double calculateDistanceMultiplier(String origin, String destination) {
        // Simple distance calculation based on known Kinshasa locations
        Map<String, Double> locationMultipliers = new HashMap<>();
        locationMultipliers.put("GARE_CENTRALE-MATETE", 1.2);
        locationMultipliers.put("GARE_CENTRALE-LIMETE", 1.0);
        locationMultipliers.put("GARE_CENTRALE-BANDALUNGWA", 1.1);
        locationMultipliers.put("GARE_CENTRALE-NDJILI", 1.5);
        locationMultipliers.put("GARE_CENTRALE-MASINA", 1.6);
        locationMultipliers.put("GARE_CENTRALE-KIMBANSEKE", 2.0);

        String route = origin + "-" + destination;
        return locationMultipliers.getOrDefault(route, 1.0);
    }

    /**
     * Envoyer des rappels de départ pour tous les tickets d'un voyage
     */
    @PostMapping("/send-departure-reminders")
    public ResponseEntity<?> sendDepartureReminders(@RequestBody DepartureReminderRequest request) {
        try {
            log.info("Sending departure reminders for bus {} at {}", request.getBusId(), request.getDepartureTime());

            if (request.getBusId() == null || request.getDepartureTime() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Bus ID and departure time are required"
                ));
            }

            // Parser l'heure de départ
            LocalDateTime departureTime;
            try {
                departureTime = LocalDateTime.parse(request.getDepartureTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Invalid departure time format"
                ));
            }

            // Récupérer tous les tickets pour ce voyage
            List<Ticket> tickets = ticketService.findByBusId(request.getBusId());

            List<Ticket> tripTickets = tickets.stream()
                    .filter(ticket -> {
                        boolean sameTime = Math.abs(ChronoUnit.MINUTES.between(ticket.getDepartureTime(), departureTime)) < 1;
                        boolean validStatus = "PAID".equals(ticket.getStatus());
                        return sameTime && validStatus;
                    })
                    .toList();

            int remindersSent = 0;
            int minutesBefore = request.getMinutesBefore() != null ? request.getMinutesBefore() : 30;

            for (Ticket ticket : tripTickets) {
                try {
                    ticketNotificationService.notifyDepartureReminder(ticket, minutesBefore);
                    remindersSent++;
                } catch (Exception e) {
                    log.error("Error sending reminder to passenger {} for ticket {}: {}",
                            ticket.getPassengerId(), ticket.getTicketNumber(), e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Departure reminders sent successfully");
            response.put("totalTickets", tripTickets.size());
            response.put("remindersSent", remindersSent);
            response.put("minutesBefore", minutesBefore);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending departure reminders: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error sending departure reminders: " + e.getMessage()
            ));
        }
    }

    /**
     * Get trip count for a driver
     */
    @GetMapping("/trips/driver/{driverId}/count")
    public ResponseEntity<Long> getDriverTripCount(@PathVariable Long driverId) {
        try {
            long tripCount = tripService.getDriverTripCount(driverId);
            return ResponseEntity.ok(tripCount);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching trip count for driver ID {}: {}", driverId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            log.error("Unexpected error fetching trip count for driver ID {}: {}", driverId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // DTOs
    public static class TicketCreateRequest {
        private Long passengerId;
        private String firstName;
        private String lastName;
        private String origin;
        private String destination;
        private Object departureTime; // Can be String or LocalDateTime
        private Long busId;
        private String seatNumber;
        private Double luggageWeight;
        private Boolean hasLuggage;

        // Getters and setters
        public Long getPassengerId() { return passengerId; }
        public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        public Object getDepartureTime() { return departureTime; }
        public void setDepartureTime(Object departureTime) { this.departureTime = departureTime; }

        public Long getBusId() { return busId; }
        public void setBusId(Long busId) { this.busId = busId; }

        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

        public Double getLuggageWeight() { return luggageWeight; }
        public void setLuggageWeight(Double luggageWeight) { this.luggageWeight = luggageWeight; }

        public Boolean getHasLuggage() { return hasLuggage; }
        public void setHasLuggage(Boolean hasLuggage) { this.hasLuggage = hasLuggage; }

        @Override
        public String toString() {
            return "TicketCreateRequest{" +
                    "passengerId=" + passengerId +
                    ", firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", origin='" + origin + '\'' +
                    ", destination='" + destination + '\'' +
                    ", departureTime=" + departureTime +
                    ", busId=" + busId +
                    ", seatNumber='" + seatNumber + '\'' +
                    ", luggageWeight=" + luggageWeight +
                    ", hasLuggage=" + hasLuggage +
                    '}';
        }
    }

    public static class PriceCalculationRequest {
        private String origin;
        private String destination;
        private Boolean hasLuggage;

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        public Boolean getHasLuggage() { return hasLuggage; }
        public void setHasLuggage(Boolean hasLuggage) { this.hasLuggage = hasLuggage; }
    }

    public static class SeatValidationRequest {
        private Long busId;
        private String seatNumber;
        private String departureTime;
        private String origin;
        private String destination;

        public Long getBusId() { return busId; }
        public void setBusId(Long busId) { this.busId = busId; }

        public String getSeatNumber() { return seatNumber; }
        public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        @Override
        public String toString() {
            return "SeatValidationRequest{" +
                    "busId=" + busId +
                    ", seatNumber='" + seatNumber + '\'' +
                    ", departureTime='" + departureTime + '\'' +
                    ", origin='" + origin + '\'' +
                    ", destination='" + destination + '\'' +
                    '}';
        }
    }

    public static class TripFilterRequest {
        private String departureTime;
        private String origin;
        private String destination;

        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        @Override
        public String toString() {
            return "TripFilterRequest{" +
                    "departureTime='" + departureTime + '\'' +
                    ", origin='" + origin + '\'' +
                    ", destination='" + destination + '\'' +
                    '}';
        }
    }

    public static class DepartureReminderRequest {
        private Long busId;
        private String departureTime;
        private Integer minutesBefore;

        public Long getBusId() { return busId; }
        public void setBusId(Long busId) { this.busId = busId; }

        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

        public Integer getMinutesBefore() { return minutesBefore; }
        public void setMinutesBefore(Integer minutesBefore) { this.minutesBefore = minutesBefore; }

        @Override
        public String toString() {
            return "DepartureReminderRequest{" +
                    "busId=" + busId +
                    ", departureTime='" + departureTime + '\'' +
                    ", minutesBefore=" + minutesBefore +
                    '}';
        }
    }




    /**
     * Obtenir les sièges pris pour un bus et un voyage spécifique
     */
    @PostMapping("/bus/{busId}/taken-seats")
    public ResponseEntity<?> getTakenSeatsForTrip(
            @PathVariable Long busId,
            @RequestBody TakenSeatsRequest request) {
        try {
            log.info("Getting taken seats for bus {} - Request: {}", busId, request);

            // Validation des paramètres requis
            if (request.getDepartureTime() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Departure time is required"
                ));
            }

            // Vérifier que le bus existe
            Bus bus = busService.findById(busId);
            if (bus == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Bus not found with ID: " + busId
                ));
            }

            // Parser l'heure de départ
            LocalDateTime departureTime;
            try {
                departureTime = LocalDateTime.parse(request.getDepartureTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Invalid departure time format. Expected ISO format: yyyy-MM-ddTHH:mm:ss"
                ));
            }

            // Récupérer tous les tickets du bus
            List<Ticket> allBusTickets = ticketService.findByBusId(busId);
            log.info("Found {} total tickets for bus {}", allBusTickets.size(), busId);

            // Filtrer pour le voyage spécifique
            List<String> takenSeats = allBusTickets.stream()
                    .filter(ticket -> {
                        // Vérifier l'heure (tolérance de 1 minute)
                        boolean sameTime = Math.abs(ChronoUnit.MINUTES.between(ticket.getDepartureTime(), departureTime)) < 1;

                        // Vérifier le trajet si spécifié
                        boolean sameRoute = true;
                        if (request.getOrigin() != null && ticket.getOrigin() != null) {
                            sameRoute = sameRoute && request.getOrigin().equals(ticket.getOrigin());
                        }
                        if (request.getDestination() != null && ticket.getDestination() != null) {
                            sameRoute = sameRoute && request.getDestination().equals(ticket.getDestination());
                        }

                        // Vérifier le statut (seulement les tickets payés ou embarqués)
                        boolean validStatus = "PAID".equals(ticket.getStatus()) || "BOARDED".equals(ticket.getStatus());

                        // Vérifier que le siège existe
                        boolean hasSeat = ticket.getSeatNumber() != null && !ticket.getSeatNumber().trim().isEmpty();

                        boolean matches = sameTime && sameRoute && validStatus && hasSeat;

                        if (matches) {
                            log.debug("Matching ticket found - ID: {}, Seat: {}, Status: {}, Time: {}",
                                    ticket.getId(), ticket.getSeatNumber(), ticket.getStatus(), ticket.getDepartureTime());
                        }

                        return matches;
                    })
                    .map(ticket -> ticket.getSeatNumber().trim())
                    .distinct() // Éviter les doublons
                    .sorted((a, b) -> {
                        // Tri numérique des sièges
                        try {
                            return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                        } catch (NumberFormatException e) {
                            return a.compareTo(b); // Tri alphabétique en cas d'échec
                        }
                    })
                    .toList();

            log.info("Found {} taken seats for specific trip: {}", takenSeats.size(), takenSeats);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("busId", busId);
            response.put("busName", bus.getName());
            response.put("busCapacity", bus.getCapacity());
            response.put("departureTime", request.getDepartureTime());
            response.put("origin", request.getOrigin());
            response.put("destination", request.getDestination());
            response.put("takenSeats", takenSeats);
            response.put("takenSeatsCount", takenSeats.size());
            response.put("availableSeatsCount", bus.getCapacity() - takenSeats.size());
            response.put("message", "Taken seats retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting taken seats for bus {}: {}", busId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Error retrieving taken seats: " + e.getMessage()
            ));
        }
    }

    /**
     * DTO pour la requête des sièges pris
     */
    public static class TakenSeatsRequest {
        private Long busId;
        private String departureTime;
        private String origin;
        private String destination;
        private String date; // Format YYYY-MM-DD

        public Long getBusId() { return busId; }
        public void setBusId(Long busId) { this.busId = busId; }

        public String getDepartureTime() { return departureTime; }
        public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

        public String getOrigin() { return origin; }
        public void setOrigin(String origin) { this.origin = origin; }

        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        @Override
        public String toString() {
            return "TakenSeatsRequest{" +
                    "busId=" + busId +
                    ", departureTime='" + departureTime + '\'' +
                    ", origin='" + origin + '\'' +
                    ", destination='" + destination + '\'' +
                    ", date='" + date + '\'' +
                    '}';
        }
    }
}