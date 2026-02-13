package project.planora_travelandbooking_system.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BookingController {

    @GetMapping("/api/bookings")
    public String booking() {
        return "bookings";
    }
}
