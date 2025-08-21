package IMAS.ImasProject.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();
    private final long EXPIRATION_TIME_MS = 5 * 60 * 1000; // 5 minutes
    private final int OTP_LENGTH = 6;
    private final Random random = new Random();

    // Scheduler pour nettoyer automatiquement les OTP expirés
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public OtpService() {
        // Démarrer le nettoyage automatique des OTP expirés toutes les 2 minutes
        scheduler.scheduleAtFixedRate(this::cleanupExpiredOtps, 2, 2, TimeUnit.MINUTES);
        logger.info("OtpService initialized with automatic cleanup every 2 minutes");
    }

    /**
     * Génère un OTP de 6 chiffres pour l'email donné
     * @param email L'email de l'utilisateur
     * @return Le code OTP généré
     */
    public String generateOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.error("Cannot generate OTP: email is null or empty");
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        String normalizedEmail = email.trim().toLowerCase();

        // Générer un OTP de 6 chiffres
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);

        // Stocker l'OTP avec timestamp
        OtpEntry entry = new OtpEntry(otp, Instant.now());
        otpStorage.put(normalizedEmail, entry);

        logger.info("OTP generated for email: {} (expires at: {})",
                normalizedEmail,
                LocalDateTime.now().plusSeconds(EXPIRATION_TIME_MS / 1000)
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return otp;
    }

    /**
     * Valide l'OTP pour l'email donné
     * @param email L'email de l'utilisateur
     * @param otp Le code OTP à valider
     * @return true si l'OTP est valide, false sinon
     */
    public boolean validateOtp(String email, String otp) {
        if (email == null || email.trim().isEmpty()) {
            logger.error("Cannot validate OTP: email is null or empty");
            return false;
        }

        if (otp == null || otp.trim().isEmpty()) {
            logger.error("Cannot validate OTP: OTP is null or empty");
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedOtp = otp.trim();

        // Vérifier si l'OTP existe
        if (!otpStorage.containsKey(normalizedEmail)) {
            logger.warn("OTP validation failed: No OTP found for email: {}", normalizedEmail);
            return false;
        }

        OtpEntry entry = otpStorage.get(normalizedEmail);

        // Vérifier l'expiration
        if (Instant.now().isAfter(entry.timestamp.plusMillis(EXPIRATION_TIME_MS))) {
            logger.warn("OTP validation failed: OTP expired for email: {}", normalizedEmail);
            otpStorage.remove(normalizedEmail);
            return false;
        }

        // Vérifier le code OTP
        boolean isValid = entry.otp.equals(normalizedOtp);

        if (isValid) {
            logger.info("OTP validation successful for email: {}", normalizedEmail);
        } else {
            logger.warn("OTP validation failed: Invalid OTP for email: {}", normalizedEmail);
        }

        return isValid;
    }

    /**
     * Supprime l'OTP pour l'email donné
     * @param email L'email de l'utilisateur
     */
    public void clearOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.error("Cannot clear OTP: email is null or empty");
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (otpStorage.remove(normalizedEmail) != null) {
            logger.info("OTP cleared for email: {}", normalizedEmail);
        } else {
            logger.debug("No OTP found to clear for email: {}", normalizedEmail);
        }
    }

    /**
     * Vérifie si un OTP existe pour l'email donné
     * @param email L'email de l'utilisateur
     * @return true si un OTP existe, false sinon
     */
    public boolean hasOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();
        return otpStorage.containsKey(normalizedEmail);
    }

    /**
     * Retourne le temps restant avant expiration de l'OTP en secondes
     * @param email L'email de l'utilisateur
     * @return Le temps restant en secondes, ou -1 si aucun OTP n'existe
     */
    public long getRemainingTimeSeconds(String email) {
        if (email == null || email.trim().isEmpty()) {
            return -1;
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (!otpStorage.containsKey(normalizedEmail)) {
            return -1;
        }

        OtpEntry entry = otpStorage.get(normalizedEmail);
        long elapsedTime = Instant.now().toEpochMilli() - entry.timestamp.toEpochMilli();
        long remainingTime = (EXPIRATION_TIME_MS - elapsedTime) / 1000;

        return Math.max(0, remainingTime);
    }

    /**
     * Nettoie automatiquement les OTP expirés
     */
    private void cleanupExpiredOtps() {
        try {
            Instant now = Instant.now();
            int removedCount = 0;

            // Parcourir et supprimer les OTP expirés
            otpStorage.entrySet().removeIf(entry -> {
                if (now.isAfter(entry.getValue().timestamp.plusMillis(EXPIRATION_TIME_MS))) {
                    return true;
                }
                return false;
            });

            if (removedCount > 0) {
                logger.info("Cleaned up {} expired OTPs", removedCount);
            }

        } catch (Exception e) {
            logger.error("Error during OTP cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Retourne le nombre d'OTP actuellement en mémoire
     * @return Le nombre d'OTP stockés
     */
    public int getOtpCount() {
        return otpStorage.size();
    }

    /**
     * Vide complètement le stockage des OTP (pour les tests ou la maintenance)
     */
    public void clearAllOtps() {
        int count = otpStorage.size();
        otpStorage.clear();
        logger.info("Cleared all {} OTPs from storage", count);
    }

    /**
     * Arrête le service de nettoyage automatique
     */
    public void shutdown() {
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            logger.info("OtpService shutdown completed");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.error("Error during OtpService shutdown: {}", e.getMessage());
        }
    }

    /**
     * Classe interne pour stocker les entrées OTP
     */
    private static class OtpEntry {
        final String otp;
        final Instant timestamp;

        OtpEntry(String otp, Instant timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "OtpEntry{" +
                    "otp='" + otp + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}