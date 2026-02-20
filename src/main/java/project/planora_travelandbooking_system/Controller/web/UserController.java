package project.planora_travelandbooking_system.Controller.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import project.planora_travelandbooking_system.Service.UserService;
import org.springframework.ui.Model;
import java.security.Principal;
import java.util.Optional;

@Controller
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService,
                          PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @GetMapping("/admin")
    public String adminDashboard(@RequestParam(defaultValue = "0") int page, Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        model.addAttribute("currentUser", currentUser);
        int pageSize = 10;
        Page<UserDTO> usersPage = userService.getAllUsers(page, pageSize);

        System.out.println("Current Page: " + page);

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("newUser", new UserDTO());

        return "admin";
    }

    @PostMapping("/admin/create")
    public String createUser(@ModelAttribute UserDTO userDTO) {
        userDTO.setSuperAdmin(false);
        try {
            userService.saveUser(userDTO);
            return "redirect:/admin";
        } catch (Exception e) {
            return "redirect:/admin?error=creatingUser";
        }
    }

    @PostMapping("/admin/edit")
    public String editUser(@RequestParam Long userId,
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

            return "redirect:/admin";
        } catch (IllegalStateException e) {
            System.out.println("Forbidden action: " + e.getMessage());
            return "redirect:/admin?error=" + e.getMessage();
        } catch (RuntimeException e) {
            System.out.println("Error updating user: " + e.getMessage());
            return "redirect:/admin?error=updateUser";
        }
    }

    @PostMapping("/user/edit")
    public String editProfile(@RequestParam Long userId,
                              @RequestParam String currentPassword,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String role,
                              @RequestParam(required = false) String password,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            User existingUser = userService.getUserId(userId);

            if (!principal.getName().equals(existingUser.getEmail())) {
                throw new IllegalStateException("Unauthorized action.");
            }

            if (!passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Incorrect current password.");
                return "redirect:/user";
            }

            if (email != null && !email.isBlank()) {
                existingUser.setEmail(email);

                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        existingUser.getEmail(),
                        existingUser.getPassword(),
                        ((UsernamePasswordAuthenticationToken) principal).getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }

            if (!existingUser.isSuperAdmin()) {
                existingUser.setRole(User.Role.USER);
            } else if (role != null) {
                existingUser.setRole(User.Role.valueOf(role));
            }

            UserDTO updatedDTO = new UserDTO();
            updatedDTO.setId(existingUser.getId());
            updatedDTO.setEmail(existingUser.getEmail());
            updatedDTO.setRole(String.valueOf(existingUser.getRole()));

            if (password != null && !password.isBlank()) {
                updatedDTO.setPassword(password);
            } else {
                updatedDTO.setPassword(null);
            }

            userService.saveUser(updatedDTO);

            redirectAttributes.addFlashAttribute("success", "Profile updated successfully.");
            return "redirect:/user";

        } catch (IllegalStateException e) {
            return "redirect:/user?error=" + e.getMessage();
        } catch (Exception e) {
            return "redirect:/user?error=updateUser";
        }
    }

    @PostMapping("/admin/delete")
    public String deleteUser(@RequestParam Long userId) {
        try {
            userService.deleteUser(userId);
        } catch (IllegalStateException e) {
            System.out.println("Forbidden action: " + e.getMessage());
            return "redirect:/admin?error=" + e.getMessage();
        } catch (RuntimeException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return "redirect:/admin?error=deleteUser";
        }

        return "redirect:/admin";
    }

    @GetMapping("/user")
    public String getUserProfile(Model model, Authentication auth) {
        String email = auth.getName();
        boolean admin = isAdmin(auth);
        model.addAttribute("isAdmin", admin);
        User currentUser = userService.getCurrentAuthenticatedUser();
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUser", currentUser);

        return "user-profile";
    }

    @PostMapping("/user/delete")
    public String deleteOwnAccount(@RequestParam String currentPassword,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   RedirectAttributes redirectAttributes) {

        try {
            User currentUser = userService.getCurrentAuthenticatedUser();

            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Incorrect current password.");
                return "redirect:/user";
            }

            if (currentUser.isSuperAdmin()) {
                throw new IllegalStateException("SuperAdmin account cannot be deleted.");
            }

            userService.deleteUser(currentUser.getId());
            SecurityContextHolder.clearContext();
            request.getSession().invalidate();

            return "redirect:/login?deleted";

        } catch (IllegalStateException e) {
            return "redirect:/user?error=" + e.getMessage();
        }
    }

    @PostMapping("/user/restore")
    @Transactional
    public String restoreAccount(@RequestParam String email,
                                 @RequestParam String password,
                                 RedirectAttributes redirectAttributes) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/login";
        }

        User user = userOptional.get();

        if (!user.isDeleted()) {
            redirectAttributes.addFlashAttribute("error", "Account is not scheduled for deletion.");
            return "redirect:/login";
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Incorrect password.");
            return "redirect:/login?restore=true&email=" + email;
        }

        user.setDeleted(false);
        user.setDeletionDate(null);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("restored", true);

        return "redirect:/login";
    }

}