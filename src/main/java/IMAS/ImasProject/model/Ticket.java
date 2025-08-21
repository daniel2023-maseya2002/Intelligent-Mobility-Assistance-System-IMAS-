package IMAS.ImasProject.model;

import IMAS.ImasProject.controller.TicketController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.persistence.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "ticket")
public class Ticket {
    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long passengerId;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 20)
    private String ticketNumber;

    @Column(nullable = false, length = 10)
    private String reservationCode;

    @Column(length = 10)
    private String seatNumber;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime boardingTime;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(nullable = false, length = 20)
    private String status;

    // Optimized QR Code storage with proper size specification
    @Column(name = "qr_code", columnDefinition = "MEDIUMBLOB")
    @Lob
    @JsonIgnore // Prevent JSON serialization issues with large binary data
    private byte[] qrCode;

    @Column(name = "luggage_weight")
    private Double luggageWeight;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean hasLuggage;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Use @JsonBackReference to prevent infinite recursion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    @JsonBackReference("bus-tickets")
    private Bus bus;

    // Use @JsonBackReference to prevent infinite recursion
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    @JsonBackReference("driver-tickets")
    private Staff driver;

    // Add Trip reference - this is the key addition
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    @JsonBackReference("trip-tickets")
    private Trip trip;

    // Add these fields for JSON serialization without circular references
    @Transient
    private Long busId;

    @Transient
    private String busName;

    @Transient
    private Long driverId;

    @Transient
    private String driverName;

    @Transient
    private Long tripId;

    // Default constructor
    public Ticket() {
        this.issuedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "ISSUED";
        this.hasLuggage = false;
        this.luggageWeight = 0.0;
    }

    // Constructor for ticket creation
    public Ticket(Long passengerId, String firstName, String lastName, String ticketNumber,
                  String reservationCode, String origin, String destination,
                  LocalDateTime departureTime, Bus bus, Staff driver) {
        this();
        this.passengerId = passengerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ticketNumber = ticketNumber;
        this.reservationCode = reservationCode;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.boardingTime = departureTime.minusMinutes(30);
        this.bus = bus;
        this.driver = driver;
    }

    // Updated constructor with Trip
    public Ticket(Long passengerId, String firstName, String lastName, String ticketNumber,
                  String reservationCode, String origin, String destination,
                  LocalDateTime departureTime, Bus bus, Staff driver, Trip trip) {
        this();
        this.passengerId = passengerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.ticketNumber = ticketNumber;
        this.reservationCode = reservationCode;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.boardingTime = departureTime.minusMinutes(30);
        this.bus = bus;
        this.driver = driver;
        this.trip = trip;
    }

    // Consolidated @PrePersist method - only one allowed per entity
    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = "ISSUED";
        }
        if (this.hasLuggage == null) {
            this.hasLuggage = false;
        }
        if (this.luggageWeight == null) {
            this.luggageWeight = 0.0;
        }

        // NE PAS générer le QR code ici car l'ID n'est pas encore disponible
        log.debug("@PrePersist called for ticket - ID will be generated after persistence");
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PostLoad
    protected void onLoad() {
        // Populate transient fields after loading from database
        if (this.bus != null) {
            this.busId = this.bus.getId();
            this.busName = this.bus.getName();
        }
        if (this.driver != null) {
            this.driverId = this.driver.getId();
            this.driverName = this.driver.getFirstName() + " " + this.driver.getLastName();
        }
        if (this.trip != null) {
            this.tripId = this.trip.getId();
        }
    }

    // Public method to generate QR code AFTER persistence (when ID is available)
    public void generateQrCode() {
        if (this.id == null) {
            throw new IllegalStateException("Cannot generate QR code: Ticket ID is null. Save the ticket first.");
        }

        if (this.ticketNumber == null) {
            throw new IllegalStateException("Cannot generate QR code: Ticket number is null.");
        }

        try {
            String qrData = this.id + "-" + this.ticketNumber;
            log.debug("Generating QR code with data: {}", qrData);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            this.qrCode = pngOutputStream.toByteArray();

            log.debug("QR code generated successfully, size: {} bytes", this.qrCode.length);
        } catch (Exception e) {
            log.error("Error generating QR code for ticket {}: {}", this.id, e.getMessage());
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    // Public method to regenerate QR code (useful for updates)
    public void regenerateQrCode() {
        generateQrCode();
    }

    // Utility methods
    public boolean hasQrCode() {
        return qrCode != null && qrCode.length > 0;
    }

    public int getQrCodeSize() {
        return qrCode != null ? qrCode.length : 0;
    }

    public boolean isValid() {
        return firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty() &&
                ticketNumber != null && !ticketNumber.trim().isEmpty() &&
                reservationCode != null && !reservationCode.trim().isEmpty() &&
                origin != null && !origin.trim().isEmpty() &&
                destination != null && !destination.trim().isEmpty() &&
                departureTime != null &&
                boardingTime != null &&
                bus != null &&
                passengerId != null;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return "PAID".equals(status) || "BOARDED".equals(status);
    }

    public boolean canBoard() {
        return "PAID".equals(status) && LocalDateTime.now().isAfter(boardingTime);
    }

    public boolean canBeScanned() {
        if (!"PAID".equals(status)) {
            return false;
        }

        if (departureTime == null) {
            return false;
        }

        // Allow scanning up to 1 hour after departure time
        return LocalDateTime.now().isBefore(departureTime.plusHours(1));
    }

    // Method to get QR data string
    public String getQrData() {
        if (this.id == null || this.ticketNumber == null) {
            return null;
        }
        return this.id + "-" + this.ticketNumber;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getReservationCode() { return reservationCode; }
    public void setReservationCode(String reservationCode) { this.reservationCode = reservationCode; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public LocalDateTime getBoardingTime() { return boardingTime; }
    public void setBoardingTime(LocalDateTime boardingTime) { this.boardingTime = boardingTime; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public byte[] getQrCode() { return qrCode; }
    public void setQrCode(byte[] qrCode) { this.qrCode = qrCode; }

    public Double getLuggageWeight() { return luggageWeight; }
    public void setLuggageWeight(Double luggageWeight) { this.luggageWeight = luggageWeight; }

    public Boolean getHasLuggage() { return hasLuggage; }
    public void setHasLuggage(Boolean hasLuggage) { this.hasLuggage = hasLuggage; }

    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Bus getBus() { return bus; }
    public void setBus(Bus bus) {
        this.bus = bus;
        if (bus != null) {
            this.busId = bus.getId();
            this.busName = bus.getName();
        }
    }

    public Staff getDriver() { return driver; }
    public void setDriver(Staff driver) {
        this.driver = driver;
        if (driver != null) {
            this.driverId = driver.getId();
            this.driverName = driver.getFirstName() + " " + driver.getLastName();
        }
    }

    public Trip getTrip() { return trip; }
    public void setTrip(Trip trip) {
        this.trip = trip;
        if (trip != null) {
            this.tripId = trip.getId();
        }
    }

    // Transient field getters
    public Long getBusId() { return busId; }
    public String getBusName() { return busName; }
    public Long getDriverId() { return driverId; }
    public String getDriverName() { return driverName; }
    public Long getTripId() { return tripId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return id != null && id.equals(ticket.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", passengerId=" + passengerId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", reservationCode='" + reservationCode + '\'' +
                ", seatNumber='" + seatNumber + '\'' +
                ", origin='" + origin + '\'' +
                ", destination='" + destination + '\'' +
                ", status='" + status + '\'' +
                ", departureTime=" + departureTime +
                ", boardingTime=" + boardingTime +
                ", hasLuggage=" + hasLuggage +
                ", luggageWeight=" + luggageWeight +
                ", issuedAt=" + issuedAt +
                ", updatedAt=" + updatedAt +
                ", busId=" + busId +
                ", busName='" + busName + '\'' +
                ", driverId=" + driverId +
                ", driverName='" + driverName + '\'' +
                ", tripId=" + tripId +
                ", qrCodeSize=" + getQrCodeSize() + " bytes" +
                ", qrData='" + getQrData() + '\'' +
                '}';
    }
}