package project.planora_travelandbooking_system.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.model.User;
import project.planora_travelandbooking_system.repository.UserEmailHistoryRepository;
import project.planora_travelandbooking_system.security.JwtUtil;
import project.planora_travelandbooking_system.dto.UserDTO;
import project.planora_travelandbooking_system.service.JwtRefreshService;
import project.planora_travelandbooking_system.service.UserService;
import project.planora_travelandbooking_system.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEmailHistoryRepository userEmailHistoryRepository;

    public AuthRestController(AuthenticationManager authenticationManager,
                              JwtUtil jwtUtil,
                              JwtRefreshService refreshService,
                              UserService userService,
                              UserRepository userRepository,
                              UserEmailHistoryRepository userEmailHistoryRepository,
                              PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshService = refreshService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEmailHistoryRepository = userEmailHistoryRepository;
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

            String refreshToken = refreshService.createToken(user, refreshDays);
            setRefreshCookie(response, refreshToken, refreshDays);

            return ResponseEntity.ok(Map.of(
                    "token", accessToken,
                    "email", email
            ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("Invalid email or password");
        } catch (RuntimeException ex) {
            return ResponseEntity.status(500).body("Login failed");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserDTO userDTO) {
        // Email validation
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        if (!userDTO.getEmail().matches("\\S+@\\S+\\.\\S+")) {
            return ResponseEntity.badRequest().body("Please enter a valid email");
        }

        // Password validation
        if (userDTO.getPassword() == null || userDTO.getPassword().length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters");
        }

        if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }

        // Check if the email already exists in user repository or user email history
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent() ||
                userEmailHistoryRepository.existsByEmail(userDTO.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already registered or in history");
        }

        // Create a new user
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(User.Role.USER);
        user.setSuperAdmin(false);
        user.setDeleted(false);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/login")
                .build();
    }

    @PostMapping("/restore")
    public ResponseEntity<?> restoreAccount(@RequestParam String email, @RequestParam String password) {
        log.info("Received restore account request for email: {}", email);
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        User user = userOptional.get();

        if (!user.isDeleted()) {
            return ResponseEntity.badRequest().body("Account is not scheduled for deletion.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Incorrect password.");
        }

        user.setDeleted(false);
        user.setDeletionDate(null);
        userRepository.save(user);

        return ResponseEntity.ok(new SuccessResponse("Account restored successfully", user.getEmail()));
    }

    private void setRefreshCookie(HttpServletResponse response, String token, int days) {
        int maxAge = (int) Duration.ofDays(days).getSeconds();

        log.info("Setting refresh cookie days={}, maxAgeSeconds={}", days, maxAge);

        response.addHeader("Set-Cookie",
                REFRESH_COOKIE + "=" + token
                        + "; Max-Age=" + maxAge
                        + "; Path=/api/auth"
                        + "; Secure"
                        + "; HttpOnly"
                        + "; SameSite=None");
    }

    public static class SuccessResponse {
        private String message;
        private String email;

        public SuccessResponse(String message, String email) {
            this.message = message;
            this.email = email;
        }

        public String getMessage() {
            return message;
        }

        public String getEmail() {
            return email;
        }
    }

}
