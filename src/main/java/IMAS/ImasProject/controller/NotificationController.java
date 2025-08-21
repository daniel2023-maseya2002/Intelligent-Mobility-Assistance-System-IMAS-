package IMAS.ImasProject.controller;

import IMAS.ImasProject.model.Notification;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.StaffRole;
import IMAS.ImasProject.services.NotificationService;
import IMAS.ImasProject.services.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StaffService staffService;

    // Pour les conducteurs
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Notification>> getDriverNotifications(@PathVariable Long driverId) {
        try {
            // Vérifier que l'utilisateur est bien un conducteur
            Optional<Staff> staffOpt = staffService.findById(driverId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Staff staff = staffOpt.get();
            if (!staff.getRole().equals(StaffRole.DRIVER)) {
                return ResponseEntity.badRequest().build();
            }

            List<Notification> notifications = notificationService.getNotificationsForDriver(driverId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }



    // Add this method to your NotificationController class

    // Marquer toutes les notifications comme lues pour un passager (endpoint spécifique)
    @PutMapping("/passenger/{passengerId}/read-all")
    public ResponseEntity<Void> markAllPassengerNotificationsAsRead(@PathVariable Long passengerId) {
        try {
            Optional<Staff> staffOpt = staffService.findById(passengerId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Staff staff = staffOpt.get();
            if (!staff.getRole().equals(StaffRole.PASSENGER)) {
                return ResponseEntity.badRequest().build();
            }

            notificationService.markAllNotificationsAsReadForUser(passengerId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Pour les passagers
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<Notification>> getPassengerNotifications(@PathVariable Long passengerId) {
        try {
            // Vérifier que l'utilisateur est bien un passager
            Optional<Staff> staffOpt = staffService.findById(passengerId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Staff staff = staffOpt.get();
            if (!staff.getRole().equals(StaffRole.PASSENGER)) {
                return ResponseEntity.badRequest().build();
            }

            List<Notification> notifications = notificationService.getNotificationsForPassenger(passengerId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }

    // Nouvelle méthode pour les notifications non lues
    @GetMapping("/driver/{driverId}/new")
    public ResponseEntity<List<Notification>> getNewDriverNotifications(@PathVariable Long driverId) {
        try {
            Optional<Staff> staffOpt = staffService.findById(driverId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Staff staff = staffOpt.get();
            if (!staff.getRole().equals(StaffRole.DRIVER)) {
                return ResponseEntity.badRequest().build();
            }

            List<Notification> newNotifications = notificationService.getUnreadNotificationsForDriver(driverId);
            return ResponseEntity.ok(newNotifications);
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }

    // Nouvelle méthode pour les notifications non lues des passagers
    @GetMapping("/passenger/{passengerId}/new")
    public ResponseEntity<List<Notification>> getNewPassengerNotifications(@PathVariable Long passengerId) {
        try {
            Optional<Staff> staffOpt = staffService.findById(passengerId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Staff staff = staffOpt.get();
            if (!staff.getRole().equals(StaffRole.PASSENGER)) {
                return ResponseEntity.badRequest().build();
            }

            List<Notification> newNotifications = notificationService.getUnreadNotificationsForPassenger(passengerId);
            return ResponseEntity.ok(newNotifications);
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }

    // Marquer une notification comme lue
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }

    // Récupérer toutes les notifications pour un utilisateur (sans vérifier le rôle)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        try {
            Optional<Staff> staffOpt = staffService.findById(userId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<Notification> notifications = notificationService.getNotificationsForUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }

    // Récupérer les notifications non lues pour un utilisateur
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadUserNotifications(@PathVariable Long userId) {
        try {
            Optional<Staff> staffOpt = staffService.findById(userId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }

    // Compter les notifications non lues
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Long> getUnreadNotificationCount(@PathVariable Long userId) {
        try {
            Optional<Staff> staffOpt = staffService.findById(userId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            long count = notificationService.getUnreadNotificationCountForUser(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }

    // Marquer toutes les notifications comme lues pour un utilisateur
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        try {
            Optional<Staff> staffOpt = staffService.findById(userId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            notificationService.markAllNotificationsAsReadForUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }

    // Créer une notification pour un conducteur
    @PostMapping("/driver/{driverId}")
    public ResponseEntity<Notification> createDriverNotification(
            @PathVariable Long driverId,
            @RequestBody NotificationRequest request) {
        try {
            Optional<Staff> staffOpt = staffService.findById(driverId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Staff staff = staffOpt.get();
            if (!staff.getRole().equals(StaffRole.DRIVER)) {
                return ResponseEntity.badRequest().build();
            }

            Notification notification = notificationService.createNotificationForDriver(
                    request.getMessage(), request.getType(), driverId);

            if (notification == null) {
                // Notification was a duplicate and not created
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }

    // Créer une notification pour un passager
    @PostMapping("/passenger/{passengerId}")
    public ResponseEntity<Notification> createPassengerNotification(
            @PathVariable Long passengerId,
            @RequestBody NotificationRequest request) {
        try {
            Optional<Staff> staffOpt = staffService.findById(passengerId);
            if (staffOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Staff staff = staffOpt.get();
            if (!staff.getRole().equals(StaffRole.PASSENGER)) {
                return ResponseEntity.badRequest().build();
            }

            Notification notification = notificationService.createNotificationForPassenger(
                    request.getMessage(), request.getType(), passengerId);

            if (notification == null) {
                // Notification was a duplicate and not created
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            e.printStackTrace(); // Pour déboguer
            return ResponseEntity.internalServerError().build();
        }
    }

    // Classe interne pour les requêtes de notification
    public static class NotificationRequest {
        private String message;
        private String type;

        public NotificationRequest() {}

        public NotificationRequest(String message, String type) {
            this.message = message;
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}