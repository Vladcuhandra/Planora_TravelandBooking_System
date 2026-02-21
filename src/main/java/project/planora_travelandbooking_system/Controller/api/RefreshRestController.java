package project.planora_travelandbooking_system.Controller.api;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.Model.JwtRefresher;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Security.JwtUtil;
import project.planora_travelandbooking_system.Service.JwtRefreshService;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth/refresh")
public class RefreshRestController {


    private static final String REFRESH_COOKIE = "refresh_token";

    private final JwtRefreshService refreshService;
    private final JwtUtil jwtUtil;

    private final int refreshDays = 14;

    public RefreshRestController(JwtRefreshService refreshService, JwtUtil jwtUtil) {
        this.refreshService = refreshService;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawRefresh = readCookie(request, REFRESH_COOKIE);
        if (rawRefresh == null || rawRefresh.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Missing refresh token"));
        }

        JwtRefresher stored;
        try {
            stored = refreshService.validateToken(rawRefresh);
        } catch (RuntimeException ex) {
            // invalid / expired / revoked
            clearRefreshCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", ex.getMessage()));
        }

        // rotating refresh token
        refreshService.revoke(stored);
        User user = stored.getUser();
        String newRefresh = refreshService.createToken(user, refreshDays);
        setRefreshCookie(response, newRefresh, refreshDays);

        String subject = user.getEmail();

        String newAccessToken = jwtUtil.generateToken(subject);

        log.info("[+]New refresh token issued");
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }


    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private void setRefreshCookie(HttpServletResponse response, String token, int days) {
        // Prefer Set-Cookie header to include SameSite reliably
        int maxAge = (int) Duration.ofDays(days).getSeconds();

        log.info("Setting refresh cookie days={}, maxAgeSeconds={}", days, maxAge);

        response.addHeader("Set-Cookie",
                REFRESH_COOKIE + "=" + token
                        + "; Max-Age=" + maxAge
                        + "; Path=/api/auth/refresh"
                        + "; Secure"
                        + "; HttpOnly"
                        + "; SameSite=None");
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie",
                REFRESH_COOKIE + "=; Max-Age=0; Path=/api/auth/refresh; Secure; HttpOnly; SameSite=None");
    }
}
