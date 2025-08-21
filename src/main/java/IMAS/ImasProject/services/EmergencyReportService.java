package IMAS.ImasProject.services;

import IMAS.ImasProject.model.EmergencyReport;
import IMAS.ImasProject.model.Staff;
import IMAS.ImasProject.model.Bus;
import IMAS.ImasProject.repository.EmergencyReportRepository;
import IMAS.ImasProject.repository.StaffRepository;
import IMAS.ImasProject.repository.BusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmergencyReportService {

    @Autowired
    private EmergencyReportRepository emergencyReportRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private BusRepository busRepository;

    public EmergencyReport createEmergencyReport(EmergencyReport report) {
        report.setTimestamp(LocalDateTime.now());
        report.setCreatedAt(LocalDateTime.now());
        return emergencyReportRepository.save(report);
    }

    public List<EmergencyReport> getDriverReports(Long driverId) {
        return emergencyReportRepository.findByDriverIdOrderByTimestampDesc(driverId);
    }

    public List<EmergencyReport> getRecentDriverReports(Long driverId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return emergencyReportRepository.findRecentByDriverId(driverId, since);
    }

    public List<EmergencyReport> getAllReports() {
        return emergencyReportRepository.findAll();
    }

    public Optional<EmergencyReport> getReportById(Long id) {
        return emergencyReportRepository.findById(id);
    }

    public EmergencyReport updateReportStatus(Long id, String status) {
        Optional<EmergencyReport> reportOpt = emergencyReportRepository.findById(id);
        if (reportOpt.isPresent()) {
            EmergencyReport report = reportOpt.get();
            report.setStatus(EmergencyReport.EmergencyStatus.valueOf(status.toUpperCase()));
            report.setUpdatedAt(LocalDateTime.now());
            return emergencyReportRepository.save(report);
        }
        throw new RuntimeException("Emergency report not found with id: " + id);
    }

    public long getDriverReportCount(Long driverId, LocalDateTime start, LocalDateTime end) {
        return emergencyReportRepository.countByDriverIdAndTimestampBetween(driverId, start, end);
    }

    public void deleteReport(Long id) {
        emergencyReportRepository.deleteById(id);
    }

    public List<EmergencyReport> getHighPriorityReports() {
        return emergencyReportRepository.findHighPriorityReports();
    }
}