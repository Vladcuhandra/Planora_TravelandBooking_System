package project.planora_travelandbooking_system.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import project.planora_travelandbooking_system.Service.UserService;
import org.springframework.ui.Model;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin")
    public String adminDashboard(@RequestParam(defaultValue = "0") int page, Model model) {
        int pageSize = 10;
        Page<UserDTO> usersPage = userService.getAllUsers(page, pageSize);

        System.out.println("Current Page: " + page);

        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("newUser", new UserDTO());

        return "admin";
    }

    @GetMapping("/user")
    public String getUserProfile(Model model) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        model.addAttribute("user", currentUser);

        return "user-profile";
    }

    @PostMapping("/admin/create")
    public String createUser(@ModelAttribute UserDTO userDTO) {
        userDTO.setSuperAdmin(false);
        try {
            userService.saveUser(userDTO);
            return "redirect:/api/admin";
        } catch (Exception e) {
            return "redirect:/api/admin?error=creatingUser";
        }
    }

    @PostMapping("/admin/edit")
    public String editUser(@RequestParam Long userId,
                           @RequestParam String email,
                           @RequestParam String role,
                           @RequestParam(required = false) String password) {
        try {
            UserDTO existingUserDTO = userService.getUserById(userId);
            UserDTO updatedDTO = new UserDTO();
            updatedDTO.setId(userId);
            updatedDTO.setEmail(email);
            updatedDTO.setRole(role);
            updatedDTO.setPassword(password);
            updatedDTO.setSuperAdmin(existingUserDTO.isSuperAdmin());
            userService.saveUser(updatedDTO);
            return "redirect:/api/admin";
        } catch (IllegalStateException e) {
            System.out.println("Forbidden action: " + e.getMessage());
            return "redirect:/api/admin?error=" + e.getMessage();
        } catch (RuntimeException e) {
            System.out.println("Error updating user: " + e.getMessage());
            return "redirect:/api/admin?error=updateUser";
        }
    }

    @PostMapping("/admin/delete")
    public String deleteUser(@RequestParam Long userId) {
        try {
            userService.deleteUser(userId);
        } catch (IllegalStateException e) {
            System.out.println("Forbidden action: " + e.getMessage());
            return "redirect:/api/admin?error=" + e.getMessage();
        } catch (RuntimeException e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return "redirect:/api/admin?error=deleteUser";
        }

        return "redirect:/api/admin";
    }

    @PostMapping("/user/delete")
    public String deleteOwnAccount(HttpServletRequest request,
                                   HttpServletResponse response) {

        try {
            User currentUser = userService.getCurrentAuthenticatedUser();
            userService.deleteUser(currentUser.getId());
            SecurityContextHolder.clearContext();
            request.getSession().invalidate();

        } catch (IllegalStateException e) {
            return "redirect:/api/profile?error=" + e.getMessage();
        }

        return "redirect:/login?deleted";
    }

}