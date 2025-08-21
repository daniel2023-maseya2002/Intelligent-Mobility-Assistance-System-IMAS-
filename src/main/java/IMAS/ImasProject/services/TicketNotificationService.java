package IMAS.ImasProject.services;

import IMAS.ImasProject.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class TicketNotificationService {

    private static final Logger log = LoggerFactory.getLogger(TicketNotificationService.class);

    @Autowired
    private KNotificationService notificationService;

    @Autowired
    private NotificationService mainNotificationService; // Service principal pour notifications

    @Autowired
    private StaffService staffService;

    /**
     * Notify passenger about successful ticket purchase
     */
    public void notifyTicketPurchase(Ticket ticket) {
        try {
            log.info("Creating ticket purchase notification for passenger {}", ticket.getPassengerId());

            // Create notification message
            String message = String.format(
                    "Ticket purchased successfully! Ticket #%s for %s to %s on %s. Seat: %s",
                    ticket.getTicketNumber(),
                    ticket.getOrigin(),
                    ticket.getDestination(),
                    ticket.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    ticket.getSeatNumber()
            );

            // Create notification in the main notification system
            mainNotificationService.createNotificationForPassenger(
                    message,
                    "TICKET_PURCHASE",
                    ticket.getPassengerId()
            );

            log.info("Ticket purchase notification created for passenger {}", ticket.getPassengerId());

        } catch (Exception e) {
            log.error("Error creating ticket purchase notification for passenger {}: {}",
                    ticket.getPassengerId(), e.getMessage(), e);
            throw new RuntimeException("Failed to create ticket purchase notification", e);
        }
    }

    /**
     * Notify driver about new passenger
     */
    public void notifyDriverNewPassenger(Ticket ticket) {
        try {
            if (ticket.getDriver() == null) {
                log.warn("No driver assigned to ticket {}, skipping driver notification", ticket.getTicketNumber());
                return;
            }

            log.info("Creating new passenger notification for driver {}", ticket.getDriver().getId());

            String message = String.format(
                    "New passenger booked: %s %s (Seat %s) for trip %s to %s on %s",
                    ticket.getFirstName(),
                    ticket.getLastName(),
                    ticket.getSeatNumber(),
                    ticket.getOrigin(),
                    ticket.getDestination(),
                    ticket.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );

            KNotification notification = new KNotification();
            notification.setType(KNotificationType.TICKET_ISSUED);
            notification.setMessage(message);
            notification.setRecipient(ticket.getDriver());

            notificationService.save(notification);
            log.info("Driver notification created successfully for ticket {}", ticket.getTicketNumber());

        } catch (Exception e) {
            log.error("Error creating driver notification for ticket {}: {}",
                    ticket.getTicketNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to create driver notification", e);
        }
    }

    /**
     * Notify about successful boarding
     */
    public void notifySuccessfulBoarding(Ticket ticket) {
        try {
            log.info("Creating boarding notification for ticket {}", ticket.getTicketNumber());

            // Notify passenger
            String passengerMessage = String.format(
                    "Successfully boarded! Have a safe trip from %s to %s. Seat: %s",
                    ticket.getOrigin(),
                    ticket.getDestination(),
                    ticket.getSeatNumber()
            );

            mainNotificationService.createNotificationForPassenger(
                    passengerMessage,
                    "BOARDING_SUCCESS",
                    ticket.getPassengerId()
            );

            // Notify driver if available
            if (ticket.getDriver() != null) {
                String driverMessage = String.format(
                        "Passenger %s %s has boarded (Seat %s) for trip to %s",
                        ticket.getFirstName(),
                        ticket.getLastName(),
                        ticket.getSeatNumber(),
                        ticket.getDestination()
                );

                KNotification notification = new KNotification();
                notification.setType(KNotificationType.TICKET_BOARDED);
                notification.setMessage(driverMessage);
                notification.setRecipient(ticket.getDriver());

                notificationService.save(notification);

                // Also create in main notification system
                mainNotificationService.createNotificationForDriver(
                        driverMessage,
                        "PASSENGER_BOARDED",
                        ticket.getDriver().getId()
                );
            }

            log.info("Boarding notifications created for ticket {}", ticket.getTicketNumber());

        } catch (Exception e) {
            log.error("Error creating boarding notification for ticket {}: {}",
                    ticket.getTicketNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to create boarding notification", e);
        }
    }

    /**
     * Send departure reminder
     */
    public void notifyDepartureReminder(Ticket ticket, int minutesBefore) {
        try {
            log.info("Creating departure reminder for ticket {} ({} minutes before)",
                    ticket.getTicketNumber(), minutesBefore);

            // Notify passenger
            String passengerMessage = String.format(
                    "Departure Reminder: Your bus from %s to %s departs in %d minutes at %s. Please be at the boarding gate. Seat: %s",
                    ticket.getOrigin(),
                    ticket.getDestination(),
                    minutesBefore,
                    ticket.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                    ticket.getSeatNumber()
            );

            mainNotificationService.createNotificationForPassenger(
                    passengerMessage,
                    "DEPARTURE_REMINDER",
                    ticket.getPassengerId()
            );

            // Optionally notify driver about pending departure
            if (ticket.getDriver() != null && minutesBefore <= 15) {
                String driverMessage = String.format(
                        "Departure in %d minutes. Passenger %s %s (Seat %s) for trip to %s",
                        minutesBefore,
                        ticket.getFirstName(),
                        ticket.getLastName(),
                        ticket.getSeatNumber(),
                        ticket.getDestination()
                );

                KNotification notification = new KNotification();
                notification.setType(KNotificationType.BUS_STARTED);
                notification.setMessage(driverMessage);
                notification.setRecipient(ticket.getDriver());

                notificationService.save(notification);

                // Also create in main notification system
                mainNotificationService.createNotificationForDriver(
                        driverMessage,
                        "DEPARTURE_REMINDER",
                        ticket.getDriver().getId()
                );
            }

            log.info("Departure reminder notifications created for ticket {}", ticket.getTicketNumber());

        } catch (Exception e) {
            log.error("Error creating departure reminder for ticket {}: {}",
                    ticket.getTicketNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to create departure reminder", e);
        }
    }

    /**
     * Notify about ticket cancellation
     */
    public void notifyTicketCancellation(Ticket ticket, String reason) {
        try {
            log.info("Creating cancellation notification for ticket {}", ticket.getTicketNumber());

            // Notify passenger (log for now)
            String passengerMessage = String.format(
                    "Ticket #%s for %s to %s has been cancelled. Reason: %s. Please contact customer service for refund.",
                    ticket.getTicketNumber(),
                    ticket.getOrigin(),
                    ticket.getDestination(),
                    reason != null ? reason : "Not specified"
            );
            log.info("PASSENGER NOTIFICATION [{}]: {}", ticket.getPassengerId(), passengerMessage);

            // Notify driver if available
            if (ticket.getDriver() != null) {
                String driverMessage = String.format(
                        "Ticket cancelled: %s %s (Seat %s) for trip to %s. Reason: %s",
                        ticket.getFirstName(),
                        ticket.getLastName(),
                        ticket.getSeatNumber(),
                        ticket.getDestination(),
                        reason != null ? reason : "Not specified"
                );

                KNotification notification = new KNotification();
                notification.setType(KNotificationType.TICKET_DELETED);
                notification.setMessage(driverMessage);
                notification.setRecipient(ticket.getDriver());

                notificationService.save(notification);
                log.info("Driver cancellation notification created for ticket {}", ticket.getTicketNumber());
            }

        } catch (Exception e) {
            log.error("Error creating cancellation notification for ticket {}: {}",
                    ticket.getTicketNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to create cancellation notification", e);
        }
    }

    /**
     * Notify about trip delay
     */
    public void notifyTripDelay(Ticket ticket, int delayMinutes, String reason) {
        try {
            log.info("Creating delay notification for ticket {} ({} minutes delay)",
                    ticket.getTicketNumber(), delayMinutes);

            // Notify passenger (log for now)
            String passengerMessage = String.format(
                    "Trip Delay: Your bus from %s to %s is delayed by %d minutes. New departure time: %s. Reason: %s",
                    ticket.getOrigin(),
                    ticket.getDestination(),
                    delayMinutes,
                    ticket.getDepartureTime().plusMinutes(delayMinutes).format(DateTimeFormatter.ofPattern("HH:mm")),
                    reason != null ? reason : "Not specified"
            );
            log.info("PASSENGER NOTIFICATION [{}]: {}", ticket.getPassengerId(), passengerMessage);

        } catch (Exception e) {
            log.error("Error creating delay notification for ticket {}: {}",
                    ticket.getTicketNumber(), e.getMessage(), e);
            throw new RuntimeException("Failed to create delay notification", e);
        }
    }
}