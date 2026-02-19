package project.planora_travelandbooking_system.Controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.DTO.SignUpDTO;
import project.planora_travelandbooking_system.Service.UserService;
import java.util.Optional;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<User> userOptional = userRepository.findByEmailAndDeletedFalse(email);

            if (userOptional.isPresent()) {
                return "redirect:/user";
            }
        }
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    @Transactional
    public String signup(SignUpDTO dto, Model model) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            return "signup";
        }

        if(dto.getPassword() == null || dto.getPassword().length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters");
            return "signup";
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            model.addAttribute("error", "Email already registered");
            return "signup";
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.USER);
        userRepository.save(user);

        return "redirect:/login";
    }

}