package IMAS.ImasProject.controller;

import IMAS.ImasProject.dto.StaffDTO;
import IMAS.ImasProject.exception.ResourceNotFoundException;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.StaffRole;
import IMAS.ImasProject.services.EmailService;
import IMAS.ImasProject.services.JwtService;
import IMAS.ImasProject.services.OtpService;
import IMAS.ImasProject.services.StaffService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StaffController {

    private static final Logger logger = LoggerFactory.getLogger(StaffController.class);
    private static final boolean DEBUG_MODE = true;

    private final StaffService staffService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final OtpService otpService;

    @Autowired
    public StaffController(StaffService staffService, PasswordEncoder passwordEncoder,
                           JwtService jwtService, EmailService emailService, OtpService otpService) {
        this.staffService = staffService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.otpService = otpService;
    }

    // Structure pour la réponse de login
    private static class LoginResponse {
        public boolean success;
        public String message;
        public StaffDTO staff;
        public String token;
        public boolean requiresOtp;

        public LoginResponse(boolean success, String message, StaffDTO staff, String token, boolean requiresOtp) {
            this.success = success;
            this.message = message;
            this.staff = staff;
            this.token = token;
            this.requiresOtp = requiresOtp;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        try {
            String firstName = registerRequest.get("firstName");
            String lastName = registerRequest.get("lastName");
            String email = registerRequest.get("email");
            String phoneNumber = registerRequest.get("phoneNumber");
            String password = registerRequest.get("password");

            // Validation
            if (firstName == null || firstName.trim().isEmpty() ||
                    lastName == null || lastName.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    phoneNumber == null || phoneNumber.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "All fields are required"));
            }

            if (password.length() < 8) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Password must be at least 8 characters long"));
            }

            // Check if email already exists
            try {
                staffService.getStaffByEmail(email);
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email already exists"));
            } catch (ResourceNotFoundException e) {
                // Email doesn't exist, continue with registration
            }

            // Create new staff with PASSENGER role
            StaffDTO newStaff = new StaffDTO();
            newStaff.setFirstName(firstName);
            newStaff.setLastName(lastName);
            newStaff.setEmail(email);
            newStaff.setPhoneNumber(phoneNumber);
            newStaff.setPassword(password);
            newStaff.setRole(StaffRole.PASSENGER);
            newStaff.setActive(true);

            StaffDTO createdStaff = staffService.createStaff(newStaff);

            // Create safe response without password
            StaffDTO safeStaff = createSafeStaffDTO(createdStaff);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Registration successful",
                    "staff", safeStaff
            ));

        } catch (Exception e) {
            logger.error("Error during registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred processing your request"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");

            if (DEBUG_MODE) {
                logger.info("Login attempt for email: {}", email);
            }

            // Validation
            if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email and password are required"));
            }

            // Get user
            StaffDTO staffDTO;
            try {
                staffDTO = staffService.getStaffByEmail(email);
                if (DEBUG_MODE) {
                    logger.info("User found: {}", email);
                }
            } catch (ResourceNotFoundException e) {
                if (DEBUG_MODE) {
                    logger.info("User not found: {}", email);
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid credentials"));
            }

            // Check if account is active
            if (!staffDTO.isActive()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Account is inactive"));
            }

            // Verify password
            boolean passwordMatches = false;
            try {
                String storedPasswordHash = staffService.getPasswordHashByEmail(email);
                if (storedPasswordHash != null) {
                    passwordMatches = passwordEncoder.matches(password, storedPasswordHash);
                }
            } catch (Exception e) {
                logger.error("Error checking password: {}", e.getMessage());
            }

            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Invalid credentials"));
            }

            // Check if OTP is required for this role
            boolean requiresOtp = requiresOtpForRole(staffDTO.getRole());

            if (requiresOtp) {
                // Generate and send OTP
                String otp = otpService.generateOtp(email);
                emailService.sendOtpEmail(staffDTO, otp);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "OTP sent to your email",
                        "requiresOtp", true,
                        "email", email
                ));
            } else {
                // Direct login for PASSENGER
                return generateLoginResponse(staffDTO);
            }

        } catch (Exception e) {
            logger.error("Error during login", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Server error occurred"));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> otpRequest) {
        try {
            String email = otpRequest.get("email");
            String otp = otpRequest.get("otp");

            if (email == null || email.trim().isEmpty() || otp == null || otp.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email and OTP are required"));
            }

            // Verify OTP
            if (!otpService.validateOtp(email, otp)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Invalid or expired OTP"));
            }

            // Get user and generate token
            StaffDTO staffDTO = staffService.getStaffByEmail(email);

            // Clear OTP after successful verification
            otpService.clearOtp(email);

            return generateLoginResponse(staffDTO);

        } catch (Exception e) {
            logger.error("Error during OTP verification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Server error occurred"));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email is required"));
            }

            // Get user
            StaffDTO staffDTO = staffService.getStaffByEmail(email);

            // Check if OTP is required for this role
            if (!requiresOtpForRole(staffDTO.getRole())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "OTP not required for this role"));
            }

            // Generate and send new OTP
            String otp = otpService.generateOtp(email);
            emailService.sendOtpEmail(staffDTO, otp);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "New OTP sent to your email"
            ));

        } catch (Exception e) {
            logger.error("Error during OTP resend", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Server error occurred"));
        }
    }

    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Email is required")
                );
            }

            try {
                StaffDTO staff = staffService.getStaffByEmail(email);
                Map<String, Object> response = new HashMap<>();
                response.put("exists", true);
                response.put("role", staff.getRole().toString());
                response.put("active", staff.isActive());
                response.put("passwordSet", staff.getPassword() != null && !staff.getPassword().isEmpty());
                return ResponseEntity.ok(response);
            } catch (ResourceNotFoundException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("exists", false);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            logger.error("Error checking email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Server error: " + e.getMessage()));
        }
    }

// REPLACE your existing getAllStaff() method with this one:

    @GetMapping
    public ResponseEntity<List<StaffDTO>> getAllStaff(@RequestParam(value = "role", required = false) String role) {
        try {
            List<StaffDTO> staffList;

            if (role != null && !role.trim().isEmpty()) {
                // Filter by role
                try {
                    StaffRole staffRole = StaffRole.valueOf(role.toUpperCase());
                    staffList = staffService.getStaffByRole(staffRole);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid role specified: {}", role);
                    return ResponseEntity.badRequest().build();
                }
            } else {
                // Get all staff if no role specified
                staffList = staffService.getAllStaff();
            }

            return ResponseEntity.ok(staffList);
        } catch (Exception e) {
            logger.error("Error getting staff", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String role = request.get("role");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required.");
            }

            if (role == null || role.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Role is required.");
            }

            try {
                StaffDTO staff = staffService.getStaffByEmail(email);

                if (!staff.getRole().toString().equals(role)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid role selected for this email.");
                }

                String resetToken = generateResetToken();
                staffService.updateStaffResetToken(staff.getId(), resetToken, LocalDateTime.now().plusHours(1));

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
                        staff.getRole(),
                        resetToken
                );

                emailService.sendEmail(email, subject, body);
                logger.info("Reset token sent to email: {}", email);

                return ResponseEntity.ok("Reset code sent to your email.");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No account found with this email.");
            }
        } catch (Exception e) {
            logger.error("Error in forgot password process", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing your request.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String resetToken = request.get("resetToken");
            String newPassword = request.get("newPassword");
            String role = request.get("role");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required.");
            }

            if (resetToken == null || resetToken.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Reset token is required.");
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("New password is required.");
            }

            if (!isPasswordValid(newPassword)) {
                return ResponseEntity.badRequest()
                        .body("New password must be at least 8 characters long");
            }

            try {
                StaffDTO staff = staffService.getStaffByEmail(email);

                if (!staff.getRole().toString().equals(role)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid role selected for this email.");
                }

                if (staff.getResetToken() == null || staff.getResetTokenExpiration() == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("No active reset request found. Please request a new password reset.");
                }

                if (staff.getResetToken().equals(resetToken) &&
                        staff.getResetTokenExpiration().isAfter(LocalDateTime.now())) {

                    staffService.resetPasswordWithToken(email, resetToken, passwordEncoder.encode(newPassword));
                    return ResponseEntity.ok("Password reset successful.");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid or expired reset token.");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No account found with this email.");
            }
        } catch (Exception e) {
            logger.error("Error in password reset process", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing your request.");
        }
    }

    // Helper methods
    private boolean requiresOtpForRole(StaffRole role) {
        // MODIFICATION: Ajoutez ANALYST aux rôles qui nécessitent OTP
        return role == StaffRole.ADMIN ||
                role == StaffRole.TECHNICIAN ||
                role == StaffRole.DRIVER ||
                role == StaffRole.ANALYST; // AJOUT DU ROLE ANALYST
    }




    @PostMapping("/create-analyst")
    public ResponseEntity<?> createAnalyst(@RequestBody Map<String, String> analystRequest) {
        try {
            String firstName = analystRequest.get("firstName");
            String lastName = analystRequest.get("lastName");
            String email = analystRequest.get("email");
            String phoneNumber = analystRequest.get("phoneNumber");
            String password = analystRequest.get("password");

            // Validation
            if (firstName == null || firstName.trim().isEmpty() ||
                    lastName == null || lastName.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    phoneNumber == null || phoneNumber.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "All fields are required"));
            }

            if (password.length() < 8) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Password must be at least 8 characters long"));
            }

            // Check if email already exists
            try {
                staffService.getStaffByEmail(email);
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Email already exists"));
            } catch (ResourceNotFoundException e) {
                // Email doesn't exist, continue with registration
            }

            // Create new staff with ANALYST role
            StaffDTO newAnalyst = new StaffDTO();
            newAnalyst.setFirstName(firstName);
            newAnalyst.setLastName(lastName);
            newAnalyst.setEmail(email);
            newAnalyst.setPhoneNumber(phoneNumber);
            newAnalyst.setPassword(password);
            newAnalyst.setRole(StaffRole.ANALYST); // DÉFINIR LE RÔLE ANALYST
            newAnalyst.setActive(true);

            StaffDTO createdAnalyst = staffService.createStaff(newAnalyst);

            // Create safe response without password
            StaffDTO safeAnalyst = createSafeStaffDTO(createdAnalyst);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Analyst created successfully",
                    "staff", safeAnalyst
            ));

        } catch (Exception e) {
            logger.error("Error during analyst creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "An error occurred processing your request"));
        }
    }


    @GetMapping("/analysts")
    public ResponseEntity<List<StaffDTO>> getAllAnalysts() {
        try {
            List<StaffDTO> analysts = staffService.getStaffByRole(StaffRole.ANALYST);
            return ResponseEntity.ok(analysts);
        } catch (Exception e) {
            logger.error("Error getting analysts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 7. AJOUT dans StaffController.java - Endpoint pour récupérer les analystes actifs
    @GetMapping("/analysts/active")
    public ResponseEntity<List<StaffDTO>> getActiveAnalysts() {
        try {
            List<StaffDTO> activeAnalysts = staffService.getActiveStaffByRole(StaffRole.ANALYST);
            return ResponseEntity.ok(activeAnalysts);
        } catch (Exception e) {
            logger.error("Error getting active analysts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<?> generateLoginResponse(StaffDTO staffDTO) {
        try {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + staffDTO.getRole().name()));

            UserDetails userDetails = User.builder()
                    .username(staffDTO.getEmail())
                    .password("")
                    .authorities(authorities)
                    .build();

            String token = jwtService.generateToken(userDetails);
            StaffDTO safeStaffDTO = createSafeStaffDTO(staffDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("staff", safeStaffDTO);
            response.put("token", token);
            response.put("requiresOtp", false);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating login response", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Server error occurred"));
        }
    }

    private StaffDTO createSafeStaffDTO(StaffDTO staff) {
        StaffDTO safeStaff = new StaffDTO();
        safeStaff.setId(staff.getId());
        safeStaff.setFirstName(staff.getFirstName());
        safeStaff.setLastName(staff.getLastName());
        safeStaff.setEmail(staff.getEmail());
        safeStaff.setPhoneNumber(staff.getPhoneNumber());
        safeStaff.setRole(staff.getRole());
        safeStaff.setActive(staff.isActive());
        safeStaff.setPhoto(staff.getPhoto());
        return safeStaff;
    }

    private String generateResetToken() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.length() >= 8;
    }



    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createStaff(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("role") String role,
            @RequestParam("password") String password,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        try {
            // Validate required fields
            if (firstName == null || firstName.trim().isEmpty() ||
                    lastName == null || lastName.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    phoneNumber == null || phoneNumber.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "All required fields must be filled"));
            }

            // Validate password strength
            if (password.length() < 8) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Password must be at least 8 characters long"));
            }

            // Validate photo if present
            byte[] photoBytes = null;
            if (photo != null && !photo.isEmpty()) {
                // Validate file size (max 2MB)
                if (photo.getSize() > 2 * 1024 * 1024) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Photo size must be less than 2MB"));
                }

                // Validate file type
                String contentType = photo.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Only image files are allowed"));
                }

                // Convert MultipartFile to byte[]
                photoBytes = photo.getBytes();
            }

            StaffDTO staffDTO = new StaffDTO();
            staffDTO.setFirstName(firstName);
            staffDTO.setLastName(lastName);
            staffDTO.setEmail(email);
            staffDTO.setPhoneNumber(phoneNumber);
            staffDTO.setRole(StaffRole.valueOf(role));
            staffDTO.setPassword(password);
            staffDTO.setPhoto(photoBytes); // Set the byte array directly

            StaffDTO createdStaff = staffService.createStaff(staffDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStaff);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Invalid role specified"));
        } catch (IOException e) {
            logger.error("Failed to process photo upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to process photo upload"));
        } catch (Exception e) {
            logger.error("Error creating staff", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error creating staff member"));
        }
    }




    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StaffDTO> updateStaff(
            @PathVariable Long id,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("role") String role,
            @RequestParam("active") String active,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        try {
            StaffDTO staffDTO = new StaffDTO();
            staffDTO.setFirstName(firstName);
            staffDTO.setLastName(lastName);
            staffDTO.setEmail(email);
            staffDTO.setPhoneNumber(phoneNumber);
            staffDTO.setRole(StaffRole.valueOf(role));
            staffDTO.setActive(Boolean.parseBoolean(active));

            // Handle file upload if present
            if (photo != null && !photo.isEmpty()) {
                try {
                    staffDTO.setPhoto(photo.getBytes()); // Set the byte array directly
                } catch (IOException e) {
                    logger.error("Error processing photo upload", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            StaffDTO updatedStaff = staffService.updateStaff(id, staffDTO);
            return ResponseEntity.ok(updatedStaff);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating staff with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long id) {
        try {
            staffService.deleteStaff(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting staff with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}