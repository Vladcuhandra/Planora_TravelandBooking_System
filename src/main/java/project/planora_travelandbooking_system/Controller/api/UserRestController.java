package project.planora_travelandbooking_system.Controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.Repository.JwtRefresherRepository;
import project.planora_travelandbooking_system.exceptions.UserAlreadyExistsException;
import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.DTO.UserProfileUpdateRequest;
import project.planora_travelandbooking_system.Repository.UserRepository;
import project.planora_travelandbooking_system.Service.UserService;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;
    private final UserRepository userRepository;

    private final JwtRefresherRepository jwtRefresherRepository;

    public UserRestController(UserService userService, UserRepository userRepository, JwtRefresherRepository jwtRefresherRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtRefresherRepository = jwtRefresherRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        return userService.getUserByEmail(principal.getName())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "User not found")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getTrip(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO,
                                              Authentication auth) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + userDTO.getEmail()
            );
        } else {
            UserDTO createdUser = userService.saveUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        }
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody UserProfileUpdateRequest req,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        UserDTO updated;
        boolean reauthRequired = false;

        if (req.getRestore() != null && req.getRestore()) {
            updated = userService.restoreUser(id, principal.getName());
        } else {
            updated = userService.updateProfile(id, principal.getName(), req);
        }

        if (updated.getEmail() != null && !updated.getEmail().equals(principal.getName())) {
            reauthRequired = true;
        }

        return ResponseEntity.ok(Map.of(
                "message", "Profile updated successfully",
                "user", updated,
                "reauthRequired", reauthRequired
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        String userName = userService.getUserId(id).getEmail();
        jwtRefresherRepository.deleteAllTokensByUserId(id);
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully: " + userName));
    }

}
