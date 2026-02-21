package project.planora_travelandbooking_system.Controller.api;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Security.JwtUtil;
import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.Service.JwtRefreshService;
import project.planora_travelandbooking_system.Service.UserService;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private static final String REFRESH_COOKIE = "refresh_token";
    private final int refreshDays = 14;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final JwtRefreshService refreshService;
    private final UserService userService;

    public AuthRestController(AuthenticationManager authenticationManager,
                              JwtUtil jwtUtil,
                              JwtRefreshService refreshService,
                              UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshService = refreshService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO userDTO, HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getEmail(), userDTO.getPassword())
            );

            String email = auth.getName();

            String accessToken = jwtUtil.generateToken(email);

            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Refresh token
            String refreshToken = refreshService.createToken(user, refreshDays);
            setRefreshCookie(response, refreshToken, refreshDays);

            return ResponseEntity.ok(Map.of(
                    "token", accessToken,
                    "email", email
            ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Invalid email or password");
        } catch (RuntimeException ex) {
            // If user lookup fails or refresh creation fails
            return ResponseEntity.status(500).body("Login failed");
        }
    }


    private void setRefreshCookie(HttpServletResponse response, String token, int days) {
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
}
