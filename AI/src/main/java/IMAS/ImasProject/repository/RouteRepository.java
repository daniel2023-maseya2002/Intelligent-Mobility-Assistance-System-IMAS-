package IMAS.ImasProject.repository;

import IMAS.ImasProject.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Route findByRouteCode(String routeCode);
    List<Route> findByRouteNameContaining(String name);
    Route findByRouteName(String routeName);
}