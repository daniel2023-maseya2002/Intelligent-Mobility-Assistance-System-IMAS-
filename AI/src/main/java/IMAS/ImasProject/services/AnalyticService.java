package IMAS.ImasProject.services;

import IMAS.ImasProject.model.*;
import IMAS.ImasProject.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnalyticService {

    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private BusRepository busRepository;

    // Login Analytics
    public Map<String, Object> getLoginStatistics(String period) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startDate = getStartDateForPeriod(period);
        LocalDateTime endDate = LocalDateTime.now();

        List<LoginAttempt> attempts = loginAttemptRepository.findByAttemptTimeBetween(startDate, endDate);

        long totalAttempts = attempts.size();
        long successfulLogins = attempts.stream().mapToLong(a -> a.isSuccessful() ? 1 : 0).sum();
        long failedLogins = totalAttempts - successfulLogins;

        stats.put("totalAttempts", totalAttempts);
        stats.put("successfulLogins", successfulLogins);
        stats.put("failedLogins", failedLogins);
        stats.put("successRate", totalAttempts > 0 ? (double) successfulLogins / totalAttempts * 100 : 0);
        stats.put("period", period);
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);

        // Group by role
        Map<StaffRole, Long> loginsByRole = attempts.stream()
                .filter(LoginAttempt::isSuccessful)
                .filter(attempt -> attempt.getUserRole() != null)
                .collect(Collectors.groupingBy(
                        LoginAttempt::getUserRole,
                        Collectors.counting()
                ));
        stats.put("loginsByRole", loginsByRole);

        // Group by date for timeline
        Map<String, Long> loginTimeline = attempts.stream()
                .filter(LoginAttempt::isSuccessful)
                .collect(Collectors.groupingBy(
                        attempt -> attempt.getAttemptTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.counting()
                ));
        stats.put("loginTimeline", loginTimeline);

        return stats;
    }

    // Financial Analytics
    public Map<String, Object> getFinancialStatistics(String period) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startDate = getStartDateForPeriod(period);
        LocalDateTime endDate = LocalDateTime.now();

        List<Ticket> paidTickets = ticketRepository.findByStatusAndIssuedAtBetween("PAID", startDate, endDate);

        // Add BOARDED tickets as they are also revenue-generating
        List<Ticket> boardedTickets = ticketRepository.findByStatusAndIssuedAtBetween("BOARDED", startDate, endDate);
        List<Ticket> allRevenueTickets = new ArrayList<>(paidTickets);
        allRevenueTickets.addAll(boardedTickets);

        // Total revenue
        double totalRevenue = allRevenueTickets.stream()
                .mapToDouble(this::calculateTicketPrice)
                .sum();

        stats.put("totalRevenue", totalRevenue);
        stats.put("totalTickets", allRevenueTickets.size());
        stats.put("averageTicketPrice", allRevenueTickets.size() > 0 ? totalRevenue / allRevenueTickets.size() : 0);
        stats.put("period", period);

        // Revenue by bus
        Map<String, Double> revenueByBus = allRevenueTickets.stream()
                .filter(t -> t.getBus() != null)
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getBus().getName(),
                        Collectors.summingDouble(this::calculateTicketPrice)
                ));
        stats.put("revenueByBus", revenueByBus);

        // Revenue by driver
        Map<String, Double> revenueByDriver = allRevenueTickets.stream()
                .filter(t -> t.getDriver() != null)
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getDriver().getFirstName() + " " + ticket.getDriver().getLastName(),
                        Collectors.summingDouble(this::calculateTicketPrice)
                ));
        stats.put("revenueByDriver", revenueByDriver);

        // Daily revenue timeline
        Map<String, Double> dailyRevenue = allRevenueTickets.stream()
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getIssuedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.summingDouble(this::calculateTicketPrice)
                ));
        stats.put("dailyRevenue", dailyRevenue);

        return stats;
    }

    // Passenger Analytics
    public Map<String, Object> getPassengerStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Get all paid and boarded tickets
        List<Ticket> allTickets = ticketRepository.findByStatus("PAID");
        List<Ticket> boardedTickets = ticketRepository.findByStatus("BOARDED");
        allTickets.addAll(boardedTickets);

        // Group tickets by passenger
        Map<Long, List<Ticket>> ticketsByPassenger = allTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getPassengerId));

        // Top customers by ticket count
        List<Map<String, Object>> topCustomersByTickets = ticketsByPassenger.entrySet().stream()
                .map(entry -> {
                    Long passengerId = entry.getKey();
                    List<Ticket> tickets = entry.getValue();
                    double totalSpent = tickets.stream()
                            .mapToDouble(this::calculateTicketPrice)
                            .sum();

                    Map<String, Object> customer = new HashMap<>();
                    customer.put("passengerId", passengerId);
                    customer.put("passengerName", tickets.get(0).getFullName());
                    customer.put("ticketCount", tickets.size());
                    customer.put("totalSpent", totalSpent);
                    return customer;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("ticketCount"), (Integer) a.get("ticketCount")))
                .limit(10)
                .collect(Collectors.toList());

        // Top customers by spending
        List<Map<String, Object>> topCustomersBySpending = ticketsByPassenger.entrySet().stream()
                .map(entry -> {
                    Long passengerId = entry.getKey();
                    List<Ticket> tickets = entry.getValue();
                    double totalSpent = tickets.stream()
                            .mapToDouble(this::calculateTicketPrice)
                            .sum();

                    Map<String, Object> customer = new HashMap<>();
                    customer.put("passengerId", passengerId);
                    customer.put("passengerName", tickets.get(0).getFullName());
                    customer.put("ticketCount", tickets.size());
                    customer.put("totalSpent", totalSpent);
                    return customer;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("totalSpent"), (Double) a.get("totalSpent")))
                .limit(10)
                .collect(Collectors.toList());

        stats.put("totalUniquePassengers", ticketsByPassenger.size());
        stats.put("topCustomersByTickets", topCustomersByTickets);
        stats.put("topCustomersBySpending", topCustomersBySpending);

        return stats;
    }

    // Bus Performance Analytics
    public Map<String, Object> getBusPerformanceStatistics(String period) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startDate = getStartDateForPeriod(period);
        LocalDateTime endDate = LocalDateTime.now();

        // Get all revenue-generating tickets in period
        List<Ticket> paidTickets = ticketRepository.findByStatusAndIssuedAtBetween("PAID", startDate, endDate);
        List<Ticket> boardedTickets = ticketRepository.findByStatusAndIssuedAtBetween("BOARDED", startDate, endDate);
        List<Ticket> allTickets = new ArrayList<>(paidTickets);
        allTickets.addAll(boardedTickets);

        List<Bus> allBuses = busRepository.findAll();

        List<Map<String, Object>> busPerformance = allBuses.stream()
                .map(bus -> {
                    List<Ticket> busTickets = allTickets.stream()
                            .filter(t -> t.getBus() != null && t.getBus().getId().equals(bus.getId()))
                            .collect(Collectors.toList());

                    double revenue = busTickets.stream()
                            .mapToDouble(this::calculateTicketPrice)
                            .sum();

                    Map<String, Object> performance = new HashMap<>();
                    performance.put("busId", bus.getId());
                    performance.put("busName", bus.getName());
                    // Fixed: Use busLine instead of plateNumber since plateNumber doesn't exist
                    performance.put("busLine", bus.getBusLine());
                    performance.put("capacity", bus.getCapacity());
                    performance.put("ticketsCount", busTickets.size());
                    performance.put("revenue", revenue);
                    performance.put("occupancyRate", bus.getCapacity() > 0 ?
                            (double) busTickets.size() / bus.getCapacity() * 100 : 0);
                    performance.put("driverName", bus.getDriver() != null ?
                            bus.getDriver().getFirstName() + " " + bus.getDriver().getLastName() : "No Driver");

                    return performance;
                })
                .sorted((a, b) -> Double.compare((Double) b.get("revenue"), (Double) a.get("revenue")))
                .collect(Collectors.toList());

        stats.put("busPerformance", busPerformance);
        stats.put("period", period);

        return stats;
    }

    // System Overview
    public Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();

        // Staff counts
        long totalStaff = staffRepository.count();
        long activeStaff = staffRepository.countByActiveTrue();
        long totalAnalysts = staffRepository.countByRole(StaffRole.ANALYST);
        long activeAnalysts = staffRepository.countByRoleAndActiveTrue(StaffRole.ANALYST);

        overview.put("totalStaff", totalStaff);
        overview.put("activeStaff", activeStaff);
        overview.put("totalAnalysts", totalAnalysts);
        overview.put("activeAnalysts", activeAnalysts);

        // Recent login attempts
        List<LoginAttempt> recentLogins = loginAttemptRepository.findTop10ByOrderByAttemptTimeDesc();
        overview.put("recentLogins", recentLogins);

        // Today's statistics
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        long todayLogins = loginAttemptRepository.countBySuccessfulTrueAndAttemptTimeBetween(startOfDay, endOfDay);

        // Get today's tickets (PAID and BOARDED)
        List<Ticket> todayPaidTickets = ticketRepository.findByStatusAndIssuedAtBetween("PAID", startOfDay, endOfDay);
        List<Ticket> todayBoardedTickets = ticketRepository.findByStatusAndIssuedAtBetween("BOARDED", startOfDay, endOfDay);

        List<Ticket> allTodayTickets = new ArrayList<>(todayPaidTickets);
        allTodayTickets.addAll(todayBoardedTickets);

        double todayRevenue = allTodayTickets.stream().mapToDouble(this::calculateTicketPrice).sum();

        overview.put("todayLogins", todayLogins);
        overview.put("todayTickets", allTodayTickets.size());
        overview.put("todayRevenue", todayRevenue);

        return overview;
    }

    // Revenue by driver detailed
    public Map<String, Object> getDriverRevenueStatistics(String period) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startDate = getStartDateForPeriod(period);
        LocalDateTime endDate = LocalDateTime.now();

        List<Ticket> paidTickets = ticketRepository.findByStatusAndIssuedAtBetween("PAID", startDate, endDate);
        List<Ticket> boardedTickets = ticketRepository.findByStatusAndIssuedAtBetween("BOARDED", startDate, endDate);
        List<Ticket> allTickets = new ArrayList<>(paidTickets);
        allTickets.addAll(boardedTickets);

        // Group by driver with detailed stats
        Map<String, Map<String, Object>> driverStats = allTickets.stream()
                .filter(t -> t.getDriver() != null)
                .collect(Collectors.groupingBy(
                        ticket -> ticket.getDriver().getFirstName() + " " + ticket.getDriver().getLastName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                tickets -> {
                                    Map<String, Object> driverStat = new HashMap<>();
                                    double revenue = tickets.stream().mapToDouble(this::calculateTicketPrice).sum();
                                    driverStat.put("revenue", revenue);
                                    driverStat.put("ticketCount", tickets.size());
                                    driverStat.put("averageTicketPrice", tickets.size() > 0 ? revenue / tickets.size() : 0);

                                    // Get unique buses this driver used
                                    Set<String> busNames = tickets.stream()
                                            .filter(t -> t.getBus() != null)
                                            .map(t -> t.getBus().getName())
                                            .collect(Collectors.toSet());
                                    driverStat.put("busesUsed", new ArrayList<>(busNames));

                                    return driverStat;
                                }
                        )
                ));

        stats.put("driverStats", driverStats);
        stats.put("period", period);
        return stats;
    }

    // Helper methods
    private LocalDateTime getStartDateForPeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period.toLowerCase()) {
            case "daily":
            case "day":
                return now.withHour(0).withMinute(0).withSecond(0);
            case "weekly":
            case "week":
                return now.minusWeeks(1);
            case "monthly":
            case "month":
                return now.minusMonths(1);
            case "yearly":
            case "year":
                return now.minusYears(1);
            default:
                return now.minusDays(1);
        }
    }

    private double calculateTicketPrice(Ticket ticket) {
        double basePrice = 2000.0; // Base price in FC
        double distanceMultiplier = calculateDistanceMultiplier(ticket.getOrigin(), ticket.getDestination());
        double luggagePrice = (ticket.getHasLuggage() != null && ticket.getHasLuggage()) ? 500.0 : 0.0;
        return (basePrice * distanceMultiplier) + luggagePrice;
    }

    private double calculateDistanceMultiplier(String origin, String destination) {
        if (origin == null || destination == null) return 1.0;

        Map<String, Double> routes = new HashMap<>();
        routes.put("GARE_CENTRALE-MATETE", 1.2);
        routes.put("GARE_CENTRALE-LIMETE", 1.0);
        routes.put("GARE_CENTRALE-BANDALUNGWA", 1.1);
        routes.put("GARE_CENTRALE-NDJILI", 1.5);
        routes.put("GARE_CENTRALE-MASINA", 1.6);
        routes.put("GARE_CENTRALE-KIMBANSEKE", 2.0);

        String route = origin + "-" + destination;
        String reverseRoute = destination + "-" + origin;
        return routes.getOrDefault(route, routes.getOrDefault(reverseRoute, 1.0));
    }

    // Method to get login attempts with pagination
    public List<LoginAttempt> getLoginAttempts(int page, int size) {
        // For now, return all recent attempts
        // In a real implementation, you'd use pagination
        return loginAttemptRepository.findTopByOrderByAttemptTimeDesc(size);
    }

    // Method to get login statistics by role
    public Map<StaffRole, Long> getLoginStatsByRole(String period) {
        LocalDateTime startDate = getStartDateForPeriod(period);
        LocalDateTime endDate = LocalDateTime.now();

        List<LoginAttempt> attempts = loginAttemptRepository.findByAttemptTimeBetween(startDate, endDate);

        return attempts.stream()
                .filter(LoginAttempt::isSuccessful)
                .filter(attempt -> attempt.getUserRole() != null)
                .collect(Collectors.groupingBy(
                        LoginAttempt::getUserRole,
                        Collectors.counting()
                ));
    }
}