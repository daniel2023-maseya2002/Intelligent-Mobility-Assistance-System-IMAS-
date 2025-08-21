package IMAS.ImasProject.config;

import IMAS.ImasProject.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/staff/login",
            "/api/staff/check-email",
            "/api/staff/reset-password",
            "/favicon.ico",
            "/error",
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    );

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtAuthenticationFilter(
            JwtService jwtService,
            @Qualifier("customUserDetailsService") UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        logger.debug("Processing request: {} {}", method, requestURI);

        // Vérifier si le chemin est public
        if (isPublicPath(requestURI)) {
            logger.debug("Public path detected: {}, skipping authentication", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        logger.debug("Authorization header: {}", authHeader != null ? "Present" : "Missing");

        // Si pas d'en-têtEd'autorisation ou format incorrect, continuer sans authentification
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No valid Authorization header found for URI: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            logger.debug("JWT token extracted, length: {}", jwt.length());

            String userEmail = jwtService.extractUsername(jwt);
            logger.debug("JWT extracted, username: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.debug("Loading user details for: {}", userEmail);

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                logger.debug("User details loaded for: {}", userEmail);

                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication successful for user: {} with authorities: {}",
                            userEmail, userDetails.getAuthorities());
                } else {
                    logger.warn("Token validation failed for user: {}", userEmail);
                }
            } else if (userEmail == null) {
                logger.warn("Could not extract username from JWT token");
            } else {
                logger.debug("User already authenticated: {}",
                        SecurityContextHolder.getContext().getAuthentication().getName());
            }
        } catch (Exception e) {
            logger.error("JWT authentication error for URI {}: {}", requestURI, e.getMessage());
            SecurityContextHolder.clearContext();
            // Ne pas interrompre la chaîne de filtres en cas d'erreur de JWT
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String requestURI) {
        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path ->
                requestURI.startsWith(path) || requestURI.contains(path)
        );
        logger.debug("Path {} is public: {}", requestURI, isPublic);
        return isPublic;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return isPublicPath(path);
    }
}