package project.planora_travelandbooking_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookingsController {

    @GetMapping("/api/bookings")
    public String booking() {
        return "bookings";
    }
}
