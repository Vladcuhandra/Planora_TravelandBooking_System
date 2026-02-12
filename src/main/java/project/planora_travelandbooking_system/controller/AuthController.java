package project.planora_travelandbooking_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.Authenticator;

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
