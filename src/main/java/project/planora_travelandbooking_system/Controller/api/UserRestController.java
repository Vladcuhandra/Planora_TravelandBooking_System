package project.planora_travelandbooking_system.Controller.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.planora_travelandbooking_system.DTO.EditProfileRequest;
import project.planora_travelandbooking_system.DTO.PasswordDTO;
import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import project.planora_travelandbooking_system.Service.UserService;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.response.AdminDashboardResponse;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserRestController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserRestController(UserService userService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @GetMapping("/admin")
    public ResponseEntity<?> adminDashboard(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "") String searchEmail,
                                            @RequestParam(defaultValue = "") String role,
                                            @RequestParam(defaultValue = "") String accountStatus) {

        User currentUser = userService.getCurrentAuthenticatedUser();
        int pageSize = 10;

        Page<UserDTO> usersPage = userService.searchUsersByFilters(searchEmail, role, accountStatus, page, pageSize);

        var response = new AdminDashboardResponse(
                usersPage.getContent(),
                usersPage.getTotalPages(),
                page,
                searchEmail,
                role,
                accountStatus
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/admin/create")
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {
        try {
            userDTO.setSuperAdmin(false);
            userService.saveUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user");
        }
    }

    @PostMapping("/admin/edit")
    public ResponseEntity<?> editUser(@RequestParam Long userId,
                                      @RequestParam String email,
                                      @RequestParam String role,
                                      @RequestParam(required = false) String password,
                                      @RequestParam(required = false, defaultValue = "KEEP") String restoreOption) {
        try {
            if ("RESTORE".equals(restoreOption)) {
                Optional<User> optionalUser = userRepository.findById(userId);
                optionalUser.ifPresent(user -> {
                    user.setDeleted(false);
                    user.setDeletionDate(null);
                    userRepository.save(user);
                });
            }

            UserDTO existingUserDTO = userService.getUserById(userId);
            UserDTO updatedDTO = new UserDTO();
            updatedDTO.setId(userId);
            updatedDTO.setEmail(email);
            updatedDTO.setRole(role);
            updatedDTO.setPassword(password);
            updatedDTO.setSuperAdmin(existingUserDTO.isSuperAdmin());
            userService.saveUser(updatedDTO);

            return ResponseEntity.ok("User updated successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden action: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user: " + e.getMessage());
        }
    }

    @PostMapping("/user/edit")
    public ResponseEntity<?> editProfile(@RequestBody EditProfileRequest editRequest,
                                         Principal principal) {
        try {
            User currentUser = userService.getCurrentAuthenticatedUser();

            if (!principal.getName().equals(currentUser.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized action.");
            }

            if (!passwordEncoder.matches(editRequest.getCurrentPassword(), currentUser.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect current password.");
            }

            if (editRequest.getEmail() != null && !editRequest.getEmail().isBlank()) {
                currentUser.setEmail(editRequest.getEmail());
            }

            if (editRequest.getRole() != null) {
                currentUser.setRole(User.Role.valueOf(editRequest.getRole()));
            }

            if (editRequest.getNewPassword() != null && !editRequest.getNewPassword().isBlank()) {
                currentUser.setPassword(passwordEncoder.encode(editRequest.getNewPassword()));
            }

            UserDTO updatedUserDTO = new UserDTO();
            updatedUserDTO.setId(currentUser.getId());
            updatedUserDTO.setEmail(currentUser.getEmail());
            updatedUserDTO.setRole(currentUser.getRole().name());
            updatedUserDTO.setPassword(currentUser.getPassword());
            userService.saveUser(updatedUserDTO);

            return ResponseEntity.ok("Profile updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating profile: " + e.getMessage());
        }
    }

    @PostMapping("/admin/delete")
    public ResponseEntity<?> deleteUser(@RequestParam Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok("User deleted successfully.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden action: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserProfile(Principal principal) {
        User currentUser = userService.getCurrentAuthenticatedUser();

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setId(currentUser.getId());
        userDTO.setEmail(currentUser.getEmail());
        userDTO.setRole(currentUser.getRole().name());
        userDTO.setSuperAdmin(currentUser.isSuperAdmin());

        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/user/delete")
    public ResponseEntity<?> deleteOwnAccount(@RequestBody PasswordDTO passwordDTO, HttpServletRequest request) {
        try {
            User currentUser = userService.getCurrentAuthenticatedUser();

            if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), currentUser.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect current password.");
            }

            if (currentUser.isSuperAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("SuperAdmin account cannot be deleted.");
            }

            userService.deleteUser(currentUser.getId());
            SecurityContextHolder.clearContext();
            request.getSession().invalidate();

            return ResponseEntity.ok("Account deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting account: " + e.getMessage());
        }
    }

    @PostMapping("/user/restore")
    @Transactional
    public ResponseEntity<?> restoreAccount(@RequestParam String email,
                                            @RequestParam String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        User user = userOptional.get();

        if (!user.isDeleted()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Account is not scheduled for deletion.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect password.");
        }

        user.setDeleted(false);
        user.setDeletionDate(null);
        userRepository.save(user);

        return ResponseEntity.ok("Account restored successfully.");
    }
}
