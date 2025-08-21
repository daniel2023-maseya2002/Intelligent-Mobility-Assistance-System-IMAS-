package IMAS.ImasProject.services;

import IMAS.ImasProject.model.Ticket;
import IMAS.ImasProject.repository.TicketRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    @Autowired
    private TicketRepository ticketRepository;

    /**
     * Save a ticket without generating QR code
     * Used for initial ticket creation
     */
    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    /**
     * Save a ticket and generate QR code if ID is available
     * This method ensures the ticket is saved first, then QR code is generated
     */
    @Transactional
    public Ticket saveWithQrCode(Ticket ticket) {
        // Step 1: Save ticket first to get the ID
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket saved with ID: {}", savedTicket.getId());

        // Step 2: Generate QR code now that we have the ID
        if (savedTicket.getId() != null) {
            try {
                byte[] qrCodeImage = generateQrCodeImage(savedTicket);
                savedTicket.setQrCode(qrCodeImage);

                // Step 3: Update ticket with QR code
                savedTicket = ticketRepository.save(savedTicket);
                log.info("QR code generated and saved for ticket ID: {}, QR code size: {} bytes",
                        savedTicket.getId(), qrCodeImage.length);
            } catch (Exception e) {
                log.error("Failed to generate QR code for ticket ID: {}, error: {}",
                        savedTicket.getId(), e.getMessage());
                // Don't throw exception - ticket is still valid without QR code
            }
        }

        return savedTicket;
    }

    /**
     * Generate QR code for existing ticket
     * This method can be called after ticket creation
     */
    @Transactional
    public Ticket generateQrCodeForExistingTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        if (ticket.getId() == null) {
            throw new IllegalStateException("Cannot generate QR code: Ticket ID is null");
        }

        if (ticket.getTicketNumber() == null || ticket.getTicketNumber().trim().isEmpty()) {
            throw new IllegalStateException("Cannot generate QR code: Ticket number is null or empty");
        }

        try {
            byte[] qrCodeImage = generateQrCodeImage(ticket);
            ticket.setQrCode(qrCodeImage);
            ticket = ticketRepository.save(ticket);
            log.info("QR code generated for existing ticket ID: {}", ticketId);
            return ticket;
        } catch (Exception e) {
            log.error("Failed to generate QR code for ticket ID: {}, error: {}", ticketId, e.getMessage());
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Generate QR code image with proper validation
     */
    public byte[] generateQrCodeImage(Ticket ticket) throws WriterException, IOException {
        // Validate ticket
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }

        if (ticket.getId() == null) {
            throw new IllegalStateException("Cannot generate QR code: Ticket ID is null. Save the ticket first.");
        }

        if (ticket.getTicketNumber() == null || ticket.getTicketNumber().trim().isEmpty()) {
            throw new IllegalStateException("Cannot generate QR code: Ticket number is null or empty");
        }

        // Create QR code data
        String qrData = ticket.getId() + "-" + ticket.getTicketNumber();
        log.debug("Generating QR code with data: {}", qrData);

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrData, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] qrCodeBytes = pngOutputStream.toByteArray();

            log.debug("QR code generated successfully, size: {} bytes", qrCodeBytes.length);
            return qrCodeBytes;

        } catch (WriterException e) {
            log.error("Error encoding QR code for ticket {}: {}", ticket.getId(), e.getMessage());
            throw new WriterException("Failed to encode QR code: " + e.getMessage());
        } catch (IOException e) {
            log.error("Error writing QR code image for ticket {}: {}", ticket.getId(), e.getMessage());
            throw new IOException("Failed to write QR code image: " + e.getMessage());
        }
    }

    /**
     * Regenerate QR code for an existing ticket
     */
    @Transactional
    public Ticket regenerateQrCode(Long ticketId) {
        return generateQrCodeForExistingTicket(ticketId);
    }

    /**
     * Find all tickets
     */
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    /**
     * Find ticket by ID
     */
    public Ticket findById(Long id) {
        return ticketRepository.findById(id).orElse(null);
    }

    /**
     * Find ticket by ticket number
     */
    public Ticket findByTicketNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber);
    }

    /**
     * Find tickets by passenger ID
     */
    public List<Ticket> findByPassengerId(Long passengerId) {
        return ticketRepository.findByPassengerId(passengerId);
    }

    /**
     * Find tickets by bus ID
     */
    public List<Ticket> findByBusId(Long busId) {
        return ticketRepository.findByBusId(busId);
    }

    /**
     * Delete ticket by ID
     */
    public void deleteById(Long id) {
        ticketRepository.deleteById(id);
    }

    /**
     * Verify QR code and return the corresponding ticket
     */
    public Ticket verifyQrCode(String qrData) {
        if (qrData == null || qrData.trim().isEmpty()) {
            throw new IllegalArgumentException("QR data cannot be empty");
        }

        // Format attendu : "ticketId-ticketNumber"
        String[] parts = qrData.split("-", 2); // Limit to 2 parts in case ticket number contains hyphens
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid QR code format. Expected format: ticketId-ticketNumber");
        }

        Long ticketId;
        try {
            ticketId = Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ticket ID in QR code: " + parts[0]);
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with ID: " + ticketId));

        // Verify ticket number matches
        String expectedTicketNumber = parts[1];
        if (!ticket.getTicketNumber().equals(expectedTicketNumber)) {
            throw new IllegalArgumentException("Ticket number mismatch. Expected: " +
                    ticket.getTicketNumber() + ", got: " + expectedTicketNumber);
        }

        log.info("QR code verified successfully for ticket ID: {}, ticket number: {}",
                ticketId, ticket.getTicketNumber());
        return ticket;
    }

    /**
     * Check if ticket has a valid QR code
     */
    public boolean hasValidQrCode(Ticket ticket) {
        if (ticket == null || ticket.getId() == null) {
            return false;
        }
        return ticket.hasQrCode();
    }

    /**
     * Get QR code data string for a ticket
     */
    public String getQrData(Ticket ticket) {
        if (ticket == null || ticket.getId() == null || ticket.getTicketNumber() == null) {
            return null;
        }
        return ticket.getId() + "-" + ticket.getTicketNumber();
    }

    /**
     * Validate ticket before QR code generation
     */
    private void validateTicketForQrGeneration(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        if (ticket.getId() == null) {
            throw new IllegalStateException("Ticket must be saved before generating QR code");
        }
        if (ticket.getTicketNumber() == null || ticket.getTicketNumber().trim().isEmpty()) {
            throw new IllegalStateException("Ticket number cannot be null or empty");
        }
    }
}