package IMAS.ImasProject.services;

import IMAS.ImasProject.dto.StaffDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends an email with the given parameters
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            logger.info("Preparing to send email to: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            logger.info("Sending email to: {}", to);
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to: " + to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Sends an email with CC recipients
     */
    public void sendEmailWithCC(String to, String[] ccRecipients, String subject, String body) {
        try {
            logger.info("Preparing to send email to: {} with {} CC recipients", to, ccRecipients != null ? ccRecipients.length : 0);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            // Add CC recipients if provided
            if (ccRecipients != null && ccRecipients.length > 0) {
                // Filter out null or empty CC recipients
                String[] validCcRecipients = java.util.Arrays.stream(ccRecipients)
                        .filter(cc -> cc != null && !cc.trim().isEmpty())
                        .toArray(String[]::new);

                if (validCcRecipients.length > 0) {
                    helper.setCc(validCcRecipients);
                    logger.info("Added {} CC recipients", validCcRecipients.length);
                }
            }

            logger.info("Sending email to: {}", to);
            mailSender.send(message);
            logger.info("Email sent successfully to: {} with CC recipients", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email with CC to: " + to, e);
            throw new RuntimeException("Failed to send email with CC", e);
        }
    }

    /**
     * Sends an email with BCC recipients
     */
    public void sendEmailWithBCC(String to, String[] bccRecipients, String subject, String body) {
        try {
            logger.info("Preparing to send email to: {} with {} BCC recipients", to, bccRecipients != null ? bccRecipients.length : 0);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            // Add BCC recipients if provided
            if (bccRecipients != null && bccRecipients.length > 0) {
                // Filter out null or empty BCC recipients
                String[] validBccRecipients = java.util.Arrays.stream(bccRecipients)
                        .filter(bcc -> bcc != null && !bcc.trim().isEmpty())
                        .toArray(String[]::new);

                if (validBccRecipients.length > 0) {
                    helper.setBcc(validBccRecipients);
                    logger.info("Added {} BCC recipients", validBccRecipients.length);
                }
            }

            logger.info("Sending email to: {}", to);
            mailSender.send(message);
            logger.info("Email sent successfully to: {} with BCC recipients", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email with BCC to: " + to, e);
            throw new RuntimeException("Failed to send email with BCC", e);
        }
    }

    /**
     * Sends an email with both CC and BCC recipients
     */
    public void sendEmailWithCCAndBCC(String to, String[] ccRecipients, String[] bccRecipients, String subject, String body) {
        try {
            logger.info("Preparing to send email to: {} with {} CC and {} BCC recipients",
                    to, ccRecipients != null ? ccRecipients.length : 0, bccRecipients != null ? bccRecipients.length : 0);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            // Add CC recipients if provided
            if (ccRecipients != null && ccRecipients.length > 0) {
                String[] validCcRecipients = java.util.Arrays.stream(ccRecipients)
                        .filter(cc -> cc != null && !cc.trim().isEmpty())
                        .toArray(String[]::new);

                if (validCcRecipients.length > 0) {
                    helper.setCc(validCcRecipients);
                    logger.info("Added {} CC recipients", validCcRecipients.length);
                }
            }

            // Add BCC recipients if provided
            if (bccRecipients != null && bccRecipients.length > 0) {
                String[] validBccRecipients = java.util.Arrays.stream(bccRecipients)
                        .filter(bcc -> bcc != null && !bcc.trim().isEmpty())
                        .toArray(String[]::new);

                if (validBccRecipients.length > 0) {
                    helper.setBcc(validBccRecipients);
                    logger.info("Added {} BCC recipients", validBccRecipients.length);
                }
            }

            logger.info("Sending email to: {}", to);
            mailSender.send(message);
            logger.info("Email sent successfully to: {} with CC and BCC recipients", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email with CC and BCC to: " + to, e);
            throw new RuntimeException("Failed to send email with CC and BCC", e);
        }
    }

    /**
     * Sends an OTP email for login verification
     */
    public void sendOtpEmail(StaffDTO staff, String otp) {
        String subject = "IMAS Login Verification Code";
        String body = String.format(
                "Dear %s %s,\n\n" +
                        "Your login verification code for IMAS is:\n\n" +
                        "%s\n\n" +
                        "This code will expire in 5 minutes.\n\n" +
                        "If you did not attempt to log in, please ignore this email and contact support.\n\n" +
                        "Best regards,\nThe IMAS Security Team",
                staff.getFirstName(),
                staff.getLastName(),
                otp
        );

        sendEmail(staff.getEmail(), subject, body);
        logger.info("OTP email sent to: {}", staff.getEmail());
    }

    /**
     * Sends an HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            logger.info("Preparing to send HTML email to: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content

            logger.info("Sending HTML email to: {}", to);
            mailSender.send(message);
            logger.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send HTML email to: " + to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    /**
     * Sends an HTML email with CC recipients
     */
    public void sendHtmlEmailWithCC(String to, String[] ccRecipients, String subject, String htmlBody) {
        try {
            logger.info("Preparing to send HTML email to: {} with {} CC recipients", to, ccRecipients != null ? ccRecipients.length : 0);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content

            // Add CC recipients if provided
            if (ccRecipients != null && ccRecipients.length > 0) {
                String[] validCcRecipients = java.util.Arrays.stream(ccRecipients)
                        .filter(cc -> cc != null && !cc.trim().isEmpty())
                        .toArray(String[]::new);

                if (validCcRecipients.length > 0) {
                    helper.setCc(validCcRecipients);
                    logger.info("Added {} CC recipients", validCcRecipients.length);
                }
            }

            logger.info("Sending HTML email to: {}", to);
            mailSender.send(message);
            logger.info("HTML email sent successfully to: {} with CC recipients", to);
        } catch (MessagingException e) {
            logger.error("Failed to send HTML email with CC to: " + to, e);
            throw new RuntimeException("Failed to send HTML email with CC", e);
        }
    }

    /**
     * Sends a password reset email with the token
     */
    public void sendPasswordResetEmail(StaffDTO staff, String resetToken) {
        String subject = "Password Reset Request";
        String body = String.format(
                "Dear %s %s,\n\n" +
                        "You have requested to reset your password for your %s account. " +
                        "Please use the following code to reset your password:\n\n" +
                        "%s\n\n" +
                        "This code will expire in 1 hour.\n\n" +
                        "If you did not request a password reset, please ignore this email.\n\n" +
                        "Best regards,\nThe IMAS Management Team",
                staff.getFirstName(),
                staff.getLastName(),
                staff.getRole().getDisplayName(),
                resetToken
        );

        sendEmail(staff.getEmail(), subject, body);
    }

    /**
     * Sends a welcome email to new staff members
     */
    public void sendWelcomeEmail(StaffDTO staff, String temporaryPassword) {
        String subject = "Welcome to IMAS - Account Created";
        String body = String.format(
                "Dear %s %s,\n\n" +
                        "Welcome to the IMAS system! Your account has been created with the following details:\n\n" +
                        "Email: %s\n" +
                        "Role: %s\n" +
                        "Temporary Password: %s\n\n" +
                        "Please log in and change your password immediately for security purposes.\n\n" +
                        "Best regards,\nThe IMAS Management Team",
                staff.getFirstName(),
                staff.getLastName(),
                staff.getEmail(),
                staff.getRole().getDisplayName(),
                temporaryPassword
        );

        sendEmail(staff.getEmail(), subject, body);
    }

    /**
     * Sends a notification email to multiple recipients
     */
    public void sendNotificationEmail(String[] recipients, String subject, String body) {
        if (recipients == null || recipients.length == 0) {
            logger.warn("No recipients provided for notification email");
            return;
        }

        // Filter out null or empty recipients
        String[] validRecipients = java.util.Arrays.stream(recipients)
                .filter(recipient -> recipient != null && !recipient.trim().isEmpty())
                .toArray(String[]::new);

        if (validRecipients.length == 0) {
            logger.warn("No valid recipients found for notification email");
            return;
        }

        // Send to first recipient as primary, others as CC
        if (validRecipients.length == 1) {
            sendEmail(validRecipients[0], subject, body);
        } else {
            String primaryRecipient = validRecipients[0];
            String[] ccRecipients = java.util.Arrays.copyOfRange(validRecipients, 1, validRecipients.length);
            sendEmailWithCC(primaryRecipient, ccRecipients, subject, body);
        }
    }

    /**
     * Sends a bulk email to multiple recipients (each as individual emails)
     */
    public void sendBulkEmail(String[] recipients, String subject, String body) {
        if (recipients == null || recipients.length == 0) {
            logger.warn("No recipients provided for bulk email");
            return;
        }

        int successCount = 0;
        int failureCount = 0;

        for (String recipient : recipients) {
            if (recipient != null && !recipient.trim().isEmpty()) {
                try {
                    sendEmail(recipient, subject, body);
                    successCount++;
                } catch (Exception e) {
                    logger.error("Failed to send bulk email to: " + recipient, e);
                    failureCount++;
                }
            }
        }

        logger.info("Bulk email completed: {} successful, {} failed", successCount, failureCount);
    }

    /**
     * Validates email address format
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailPattern);
    }

    /**
     * Sends an emergency alert email with high priority
     */
    public void sendEmergencyAlert(String to, String[] ccRecipients, String subject, String body) {
        try {
            logger.info("Preparing to send EMERGENCY email to: {} with {} CC recipients", to, ccRecipients != null ? ccRecipients.length : 0);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("🚨 URGENT: " + subject);
            helper.setText(body, false);

            // Set high priority
            message.setHeader("X-Priority", "1");
            message.setHeader("X-MSMail-Priority", "High");
            message.setHeader("Importance", "High");

            // Add CC recipients if provided
            if (ccRecipients != null && ccRecipients.length > 0) {
                String[] validCcRecipients = java.util.Arrays.stream(ccRecipients)
                        .filter(cc -> cc != null && !cc.trim().isEmpty())
                        .toArray(String[]::new);

                if (validCcRecipients.length > 0) {
                    helper.setCc(validCcRecipients);
                    logger.info("Added {} CC recipients to emergency alert", validCcRecipients.length);
                }
            }

            logger.info("Sending EMERGENCY email to: {}", to);
            mailSender.send(message);
            logger.info("EMERGENCY email sent successfully to: {} with CC recipients", to);
        } catch (MessagingException e) {
            logger.error("Failed to send emergency email to: " + to, e);
            throw new RuntimeException("Failed to send emergency email", e);
        }
    }
}