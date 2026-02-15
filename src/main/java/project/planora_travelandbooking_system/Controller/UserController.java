package project.planora_travelandbooking_system.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import project.planora_travelandbooking_system.Service.UserService;
import org.springframework.ui.Model;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api")
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

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("newUser", new UserDTO());
        return "admin";
    }

    @PostMapping("/admin/create")
    public String createUser(@ModelAttribute UserDTO userDTO) {
        try {
            userService.saveUser(userDTO);
            return "redirect:/api/admin";
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/api/admin?error=creatingUser";
        }
    }

    @PostMapping("/admin/edit")
    public String editUser(@RequestParam Long userId,
                           @RequestParam String email,
                           @RequestParam String role,
                           @RequestParam(required = false) String password) {
        System.out.println("Received request to update user with ID: " + userId);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setEmail(email);
        userDTO.setRole(role);
        userDTO.setPassword(password);

        try {
            userService.saveUser(userDTO);
            System.out.println("User updated successfully!");
            return "redirect:/api/admin";
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return "redirect:/api/admin?error=" + e.getMessage();
        }
    }

    @PostMapping("/admin/delete")
    public String deleteUser(@RequestParam Long userId) {
        try {
            userService.deleteUser(userId);
        } catch (RuntimeException e) {
            System.out.println("Error deleting user: " + e.getMessage());
        }
        return "redirect:/api/admin";
    }

}