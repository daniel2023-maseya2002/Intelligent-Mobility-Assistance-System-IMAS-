package IMAS.ImasProject.services;


import IMAS.ImasProject.dto.StaffDTO;
import IMAS.ImasProject.exception.DuplicateResourceException;
import IMAS.ImasProject.exception.ResourceNotFoundException;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.StaffRole;
import IMAS.ImasProject.repository.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StaffService {
    private static final Logger logger = LoggerFactory.getLogger(StaffService.class);
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public StaffService(StaffRepository staffRepository, PasswordEncoder passwordEncoder) {
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public String getPasswordHashByEmail(String email) {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff with email " + email + " not found"));
        return staff.getPassword();
    }
    public StaffDTO createStaff(StaffDTO staffDTO) {
        if (staffRepository.existsByEmail(staffDTO.getEmail())) {
            throw new DuplicateResourceException("Staff with email " + staffDTO.getEmail() + " already exists");
        }

        Staff staff = convertToEntity(staffDTO);
        staff.setCreatedAt(LocalDateTime.now());
        staff.setActive(true);

        // Encodage du mot de passe
        staff.setPassword(passwordEncoder.encode(staffDTO.getPassword()));

        Staff savedStaff = staffRepository.save(staff);
        return convertToDTO(savedStaff);
    }
    public StaffDTO createStaffWithPhoto(StaffDTO staffDTO, MultipartFile photoFile) {
        if (staffRepository.existsByEmail(staffDTO.getEmail())) {
            throw new DuplicateResourceException("Staff with email " + staffDTO.getEmail() + " already exists");
        }

        Staff staff = convertToEntity(staffDTO);
        staff.setCreatedAt(LocalDateTime.now());
        staff.setActive(true);

        // Encode password before saving
        if (staff.getPassword() != null) {
            staff.setPassword(passwordEncoder.encode(staff.getPassword()));
        }

        // Sauvegarde de la photo si présente
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                staff.setPhoto(photoFile.getBytes());
            } catch (IOException ex) {
                throw new RuntimeException("Impossible de lire la photo: " + ex.getMessage(), ex);
            }
        }

        Staff savedStaff = staffRepository.save(staff);
        return convertToDTO(savedStaff);
    }

    @Transactional(readOnly = true)
    public StaffDTO getStaffById(Long id) {
        Staff staff = findStaffById(id);
        return convertToDTO(staff);
    }

    @Transactional(readOnly = true)
    public StaffDTO getStaffByEmail(String email) {
        logger.debug("Getting staff by email: {}", email);
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff with email " + email + " not found"));

        StaffDTO staffDTO = convertToDTO(staff);
        // Assurer que le mot de passe hash est bien transmis pour l'authentification
        staffDTO.setPassword(staff.getPassword());

        return staffDTO;
    }
// Dans StaffService.java

    public boolean isDriver(Long staffId) {
        return staffRepository.findById(staffId)
                .map(staff -> staff.getRole() == StaffRole.DRIVER)
                .orElse(false);
    }

    public boolean isPassenger(Long staffId) {
        return staffRepository.findById(staffId)
                .map(staff -> staff.getRole() == StaffRole.PASSENGER)
                .orElse(false);
    }
    @Transactional(readOnly = true)
    public List<StaffDTO> getAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StaffDTO> getStaffByRole(StaffRole role) {
        return staffRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StaffDTO> getActiveStaff() {
        return staffRepository.findByActive(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Staff> getActiveAnalysts() {
        return staffRepository.findByRoleAndActive(StaffRole.ANALYST, true);
    }
    @Transactional(readOnly = true)
    public List<StaffDTO> getActiveStaffByRole(StaffRole role) {
        return staffRepository.findByRoleAndActive(role, true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Ajout de la méthode searchStaff manquante
    @Transactional(readOnly = true)
    public List<StaffDTO> searchStaff(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStaff();
        }

        String searchTerm = "%" + keyword.toLowerCase() + "%";
        return staffRepository.findByFirstNameLikeOrLastNameLikeOrEmailLike(searchTerm, searchTerm, searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    // AJOUTS À FAIRE DANS StaffService.java

    // 1. Ajouter cette méthode pour retourner Optional<Staff> au lieu de StaffDTO
    @Transactional(readOnly = true)
    public Optional<Staff> findById(Long id) {
        return staffRepository.findById(id);
    }

    // 2. Ajouter cette méthode pour retourner directement l'entité Staff
    @Transactional(readOnly = true)
    public Staff findStaffEntityById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff with id " + id + " not found"));
    }

    // 3. Ajouter cette méthode pour rechercher par email et retourner l'entité
    @Transactional(readOnly = true)
    public Optional<Staff> findByEmail(String email) {
        return staffRepository.findByEmail(email);
    }

    // 4. Méthode utilitaire pour convertir StaffDTO vers Staff (si nécessaire)
    public Staff convertDTOToEntity(StaffDTO dto) {
        return convertToEntity(dto);
    }

    // 5. Méthode pour obtenir tous les techniciens actifs (entités Staff)
    @Transactional(readOnly = true)
    public List<Staff> getActiveTechnicians() {
        return staffRepository.findByRoleAndActive(StaffRole.TECHNICIAN, true);
    }

    // 6. Méthode pour obtenir un staff par ID avec gestion d'exception personnalisée
    @Transactional(readOnly = true)
    public Staff getStaffEntityById(Long id) {
        return findStaffById(id); // Réutilise la méthode privée existante
    }

    // Méthode updateStaff corrigée - gère correctement les tokens de reset
    public StaffDTO updateStaff(Long id, StaffDTO staffDTO) {
        Staff existingStaff = findStaffById(id);

        // Check if email is being changed and if it's already in use
        if (!existingStaff.getEmail().equals(staffDTO.getEmail()) &&
                staffRepository.existsByEmail(staffDTO.getEmail())) {
            throw new DuplicateResourceException("Email " + staffDTO.getEmail() + " is already in use");
        }

        existingStaff.setFirstName(staffDTO.getFirstName());
        existingStaff.setLastName(staffDTO.getLastName());
        existingStaff.setEmail(staffDTO.getEmail());
        existingStaff.setPhoneNumber(staffDTO.getPhoneNumber());
        existingStaff.setRole(staffDTO.getRole());
        existingStaff.setUpdatedAt(LocalDateTime.now());

        // Update password only if provided
        if (staffDTO.getPassword() != null && !staffDTO.getPassword().isEmpty()) {
            existingStaff.setPassword(staffDTO.getPassword()); // Assumant que c'est déjà encodé dans le controller
        }

        // AJOUT IMPORTANT : Mise à jour des champs de reset token si fournis
        if (staffDTO.getResetToken() != null) {
            existingStaff.setResetToken(staffDTO.getResetToken());
        }
        if (staffDTO.getResetTokenExpiration() != null) {
            existingStaff.setResetTokenExpiration(staffDTO.getResetTokenExpiration());
        }

        Staff updatedStaff = staffRepository.save(existingStaff);
        return convertToDTO(updatedStaff);
    }

    // Méthode spécifique pour mettre à jour le token de reset
    public StaffDTO updateStaffResetToken(Long staffId, String resetToken, LocalDateTime expiration) {
        Staff staff = findStaffById(staffId);
        staff.setResetToken(resetToken);
        staff.setResetTokenExpiration(expiration);
        staff.setUpdatedAt(LocalDateTime.now());

        Staff updatedStaff = staffRepository.save(staff);
        return convertToDTO(updatedStaff);
    }

    // Méthode pour effacer le token de reset
    public StaffDTO clearResetToken(Long staffId) {
        Staff staff = findStaffById(staffId);
        staff.setResetToken(null);
        staff.setResetTokenExpiration(null);
        staff.setUpdatedAt(LocalDateTime.now());

        Staff updatedStaff = staffRepository.save(staff);
        return convertToDTO(updatedStaff);
    }

    // Méthode corrigée pour générer le token de reset
    public StaffDTO generateResetToken(String email) {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff with email " + email + " not found"));

        // Générer un token de 6 chiffres comme dans le controller
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        String token = String.valueOf(number);

        staff.setResetToken(token);
        staff.setResetTokenExpiration(LocalDateTime.now().plusHours(1)); // 1 heure comme dans le controller
        staff.setUpdatedAt(LocalDateTime.now());

        Staff updatedStaff = staffRepository.save(staff);
        return convertToDTO(updatedStaff);
    }

    // Méthode corrigée pour reset le mot de passe
    public StaffDTO resetPasswordWithToken(String email, String token, String newPassword) {
        Staff staff = staffRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Staff with email " + email + " not found"));

        // Vérifier le token et son expiration
        if (staff.getResetToken() == null ||
                !staff.getResetToken().equals(token) ||
                staff.getResetTokenExpiration() == null ||
                staff.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }

        // Mise à jour du mot de passe (assumant qu'il est déjà encodé)
        staff.setPassword(newPassword);
        staff.setResetToken(null);
        staff.setResetTokenExpiration(null);
        staff.setUpdatedAt(LocalDateTime.now());

        Staff updatedStaff = staffRepository.save(staff);
        return convertToDTO(updatedStaff);
    }

    public StaffDTO updateStaffWithPhoto(Long id, StaffDTO staffDTO, MultipartFile photoFile) {
        Staff existingStaff = findStaffById(id);

        // Check if email is being changed and if it's already in use
        if (!existingStaff.getEmail().equals(staffDTO.getEmail()) &&
                staffRepository.existsByEmail(staffDTO.getEmail())) {
            throw new DuplicateResourceException("Email " + staffDTO.getEmail() + " is already in use");
        }

        existingStaff.setFirstName(staffDTO.getFirstName());
        existingStaff.setLastName(staffDTO.getLastName());
        existingStaff.setEmail(staffDTO.getEmail());
        existingStaff.setPhoneNumber(staffDTO.getPhoneNumber());
        existingStaff.setRole(staffDTO.getRole());
        existingStaff.setUpdatedAt(LocalDateTime.now());

        // Update password only if provided
        if (staffDTO.getPassword() != null && !staffDTO.getPassword().isEmpty()) {
            existingStaff.setPassword(passwordEncoder.encode(staffDTO.getPassword()));
        }

        // Mise à jour de la photo si présente
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                existingStaff.setPhoto(photoFile.getBytes());
            } catch (IOException ex) {
                throw new RuntimeException("Impossible de lire la photo: " + ex.getMessage(), ex);
            }
        }

        Staff updatedStaff = staffRepository.save(existingStaff);
        return convertToDTO(updatedStaff);
    }

    public StaffDTO updateStaffPhoto(Long id, MultipartFile photoFile) {
        Staff existingStaff = findStaffById(id);

        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                existingStaff.setPhoto(photoFile.getBytes());
                existingStaff.setUpdatedAt(LocalDateTime.now());

                Staff updatedStaff = staffRepository.save(existingStaff);
                return convertToDTO(updatedStaff);
            } catch (IOException ex) {
                throw new RuntimeException("Impossible de lire la photo: " + ex.getMessage(), ex);
            }
        } else {
            throw new IllegalArgumentException("Le fichier photo ne peut pas être vide");
        }
    }

    public StaffDTO updateStaffPassword(Long id, String password) {
        Staff existingStaff = findStaffById(id);

        if (password != null && !password.isEmpty()) {
            existingStaff.setPassword(passwordEncoder.encode(password));
            existingStaff.setUpdatedAt(LocalDateTime.now());

            Staff updatedStaff = staffRepository.save(existingStaff);
            return convertToDTO(updatedStaff);
        } else {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }
    }

    public StaffDTO updateStaffRole(Long id, StaffRole role) {
        Staff existingStaff = findStaffById(id);
        existingStaff.setRole(role);
        existingStaff.setUpdatedAt(LocalDateTime.now());

        Staff updatedStaff = staffRepository.save(existingStaff);
        return convertToDTO(updatedStaff);
    }

    public StaffDTO deactivateStaff(Long id) {
        Staff staff = findStaffById(id);
        staff.setActive(false);
        staff.setUpdatedAt(LocalDateTime.now());

        Staff updatedStaff = staffRepository.save(staff);

        // Vérification supplémentaire pour debug
        System.out.println("Staff " + id + " deactivated. Active status in DB: " + updatedStaff.isActive());

        return convertToDTO(updatedStaff);
    }

    public StaffDTO activateStaff(Long id) {
        Staff staff = findStaffById(id);
        staff.setActive(true);
        staff.setUpdatedAt(LocalDateTime.now());

        Staff updatedStaff = staffRepository.save(staff);

        // Vérification supplémentaire pour debug
        System.out.println("Staff " + id + " activated. Active status in DB: " + updatedStaff.isActive());

        return convertToDTO(updatedStaff);
    }


    public StaffDTO removeStaffPhoto(Long id) {
        Staff staff = findStaffById(id);
        staff.setPhoto(null);
        staff.setUpdatedAt(LocalDateTime.now());

        Staff updatedStaff = staffRepository.save(staff);
        return convertToDTO(updatedStaff);
    }

    public void deleteStaff(Long id) {
        Staff staff = findStaffById(id);
        staffRepository.deleteById(id);
    }
    public boolean isAnalyst(Long staffId) {
        return staffRepository.findById(staffId)
                .map(staff -> staff.getRole() == StaffRole.ANALYST)
                .orElse(false);
    }



    public StaffDTO resetPassword(String token, String newPassword) {
        Staff staff = staffRepository.findByResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired reset token"));

        // Vérifier si le token n'a pas expiré
        if (staff.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }

        // Mise à jour du mot de passe
        staff.setPassword(passwordEncoder.encode(newPassword));
        staff.setResetToken(null); // Réinitialiser le token après utilisation
        staff.setResetTokenExpiration(null);
        staff.setUpdatedAt(LocalDateTime.now());

        Staff updatedStaff = staffRepository.save(staff);
        return convertToDTO(updatedStaff);
    }

    private Staff findStaffById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff with id " + id + " not found"));
    }

    public StaffDTO convertToDTO(Staff staff) {
        StaffDTO dto = new StaffDTO();
        dto.setId(staff.getId());
        dto.setFirstName(staff.getFirstName());
        dto.setLastName(staff.getLastName());
        dto.setEmail(staff.getEmail());
        dto.setPhoneNumber(staff.getPhoneNumber());
        dto.setRole(staff.getRole());
        dto.setActive(staff.isActive());
        dto.setPhoto(staff.getPhoto());
        dto.setCreatedAt(staff.getCreatedAt());
        dto.setUpdatedAt(staff.getUpdatedAt());
        dto.setResetToken(staff.getResetToken());
        dto.setResetTokenExpiration(staff.getResetTokenExpiration());
        // IMPORTANT: Ici, nous ne copions PAS le mot de passe vers le DTO
        // par défaut pour des raisons de sécurité
        return dto;
    }
    public Map<String, Object> getStaffStatistics() {
        List<Staff> allStaff = staffRepository.findAll();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStaff", allStaff.size());
        stats.put("activeStaff", allStaff.stream().filter(Staff::isActive).count());
        stats.put("inactiveStaff", allStaff.stream().filter(s -> !s.isActive()).count());

        Map<String, Long> roleDistribution = allStaff.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getRole().name(),
                        Collectors.counting()
                ));
        stats.put("roleDistribution", roleDistribution);

        return stats;
    }

    public List<Map<String, Object>> getMonthlyStaffStatistics() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(11).withDayOfMonth(1);

        List<Map<String, Object>> monthlyStats = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            LocalDate currentMonth = startDate.plusMonths(i);
            int year = currentMonth.getYear();
            int month = currentMonth.getMonthValue();

            Map<String, Object> monthStats = new HashMap<>();
            monthStats.put("year", year);
            monthStats.put("month", month);

            // Count new staff created in this month
            long newStaff = staffRepository.countByCreatedAtBetween(
                    currentMonth.atStartOfDay(),
                    currentMonth.plusMonths(1).atStartOfDay()
            );
            monthStats.put("newStaff", newStaff);

            // Count active staff as of the end of this month
            long activeStaff = staffRepository.countByActiveTrueAndCreatedAtBefore(
                    currentMonth.plusMonths(1).atStartOfDay()
            );
            monthStats.put("activeStaff", activeStaff);

            monthlyStats.add(monthStats);
        }

        return monthlyStats;
    }
    private Staff convertToEntity(StaffDTO dto) {
        Staff staff = new Staff();
        staff.setId(dto.getId());
        staff.setFirstName(dto.getFirstName());
        staff.setLastName(dto.getLastName());
        staff.setEmail(dto.getEmail());
        staff.setPhoneNumber(dto.getPhoneNumber());
        staff.setPassword(dto.getPassword()); // Important pour la création/modification
        staff.setRole(dto.getRole());
        staff.setActive(dto.isActive());
        staff.setPhoto(dto.getPhoto());
        staff.setCreatedAt(dto.getCreatedAt());
        staff.setUpdatedAt(dto.getUpdatedAt());
        staff.setResetToken(dto.getResetToken());
        staff.setResetTokenExpiration(dto.getResetTokenExpiration());
        return staff;
    }
}