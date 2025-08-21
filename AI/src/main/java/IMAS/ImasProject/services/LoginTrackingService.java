package IMAS.ImasProject.services;

import IMAS.ImasProject.model.LoginAttempt;
import IMAS.ImasProject.model.StaffRole;
import IMAS.ImasProject.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LoginTrackingService {

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    public void recordLoginAttempt(String email, String ipAddress, boolean successful,
                                   StaffRole userRole, String failureReason, String userAgent) {
        LoginAttempt attempt = new LoginAttempt();
        attempt.setEmail(email);
        attempt.setIpAddress(ipAddress);
        attempt.setSuccessful(successful);
        attempt.setUserRole(userRole);
        attempt.setFailureReason(failureReason);
        attempt.setUserAgent(userAgent);
        attempt.setAttemptTime(LocalDateTime.now());
        attempt.setDeviceType(extractDeviceType(userAgent));
        attempt.setLocation("Kinshasa, DRC"); // You can integrate with IP geolocation service

        loginAttemptRepository.save(attempt);
    }

    public List<LoginAttempt> getRecentLoginAttempts(int limit) {
        return loginAttemptRepository.findTopByOrderByAttemptTimeDesc(limit);
    }

    public List<LoginAttempt> getLoginAttemptsByEmail(String email) {
        return loginAttemptRepository.findByEmailOrderByAttemptTimeDesc(email);
    }

    public List<LoginAttempt> getFailedLoginAttempts(LocalDateTime since) {
        return loginAttemptRepository.findBySuccessfulFalseAndAttemptTimeAfter(since);
    }

    public List<LoginAttempt> getLoginAttemptsInPeriod(LocalDateTime start, LocalDateTime end) {
        return loginAttemptRepository.findByAttemptTimeBetween(start, end);
    }

    public long countSuccessfulLogins(LocalDateTime since) {
        return loginAttemptRepository.countBySuccessfulTrueAndAttemptTimeAfter(since);
    }

    public long countFailedLogins(LocalDateTime since) {
        return loginAttemptRepository.countBySuccessfulFalseAndAttemptTimeAfter(since);
    }

    // Get login attempts by role
    public List<LoginAttempt> getLoginAttemptsByRole(StaffRole role, LocalDateTime since) {
        return loginAttemptRepository.findByUserRoleAndAttemptTimeAfterOrderByAttemptTimeDesc(role, since);
    }

    // Get success rate for a specific role
    public double getSuccessRateByRole(StaffRole role, LocalDateTime since) {
        long total = loginAttemptRepository.countByUserRoleAndAttemptTimeAfter(role, since);
        long successful = loginAttemptRepository.countByUserRoleAndSuccessfulTrueAndAttemptTimeAfter(role, since);

        return total > 0 ? (double) successful / total * 100 : 0.0;
    }

    // Get login statistics by IP address
    public List<Object[]> getLoginStatsByIP(LocalDateTime since) {
        return loginAttemptRepository.findLoginStatsByIPAddress(since);
    }

    // Get most active users
    public List<Object[]> getMostActiveUsers(LocalDateTime since, int limit) {
        return loginAttemptRepository.findMostActiveUsers(since, limit);
    }

    private String extractDeviceType(String userAgent) {
        if (userAgent == null) return "Unknown";

        userAgent = userAgent.toLowerCase();
        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
}