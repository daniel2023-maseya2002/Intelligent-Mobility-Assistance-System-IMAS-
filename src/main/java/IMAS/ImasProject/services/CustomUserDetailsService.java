package IMAS.ImasProject.services;

import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.repository.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final StaffRepository staffRepository;

    @Autowired
    public CustomUserDetailsService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
        logger.info("CustomUserDetailsService initialized");
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            logger.error("Email is null or empty");
            throw new UsernameNotFoundException("Email cannot be null or empty");
        }

        try {
            // Récupérer l'utilisateur directement depuis le repository
            Optional<Staff> staffOptional = staffRepository.findByEmail(email.trim().toLowerCase());

            if (staffOptional.isEmpty()) {
                logger.error("User not found with email: {}", email);
                throw new UsernameNotFoundException("User not found with email: " + email);
            }

            Staff staff = staffOptional.get();
            logger.debug("User found: {} with role: {}", staff.getEmail(), staff.getRole());

            // Vérifier si le compte est actif
            if (!staff.isActive()) {
                logger.error("User account is inactive: {}", email);
                throw new UsernameNotFoundException("User account is inactive: " + email);
            }

            // Vérifier si le mot de passe est défini
            if (staff.getPassword() == null || staff.getPassword().trim().isEmpty()) {
                logger.error("User has no password set: {}", email);
                throw new UsernameNotFoundException("User has no password set: " + email);
            }

            // Créer l'autorité basée sur le rôle
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + staff.getRole().name());
            logger.debug("Authority created: {}", authority.getAuthority());

            // Construire et retourner UserDetails
            UserDetails userDetails = User.builder()
                    .username(staff.getEmail())
                    .password(staff.getPassword())
                    .authorities(Collections.singletonList(authority))
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(!staff.isActive())
                    .build();

            logger.debug("UserDetails created successfully for user: {}", email);
            return userDetails;

        } catch (Exception e) {
            logger.error("Error loading user by username: {}", e.getMessage(), e);
            throw new UsernameNotFoundException("Error loading user: " + e.getMessage(), e);
        }
    }
}