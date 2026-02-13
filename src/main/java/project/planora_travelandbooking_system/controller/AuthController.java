package project.planora_travelandbooking_system.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if(authentication != null && authentication.isAuthenticated())
            return "redirect:/api/bookings";
        else
            return "login";
    }

}
