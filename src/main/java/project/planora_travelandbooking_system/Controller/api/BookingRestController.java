package project.planora_travelandbooking_system.Controller.api;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.BookingDTO;
import project.planora_travelandbooking_system.Service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private final BookingService bookingService;

    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public Page<BookingDTO> bookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(value = "type", required = false) String type,
            Authentication auth
    ) {
        String email = auth.getName();
        boolean admin = isAdmin(auth);

        return bookingService.getAllBookings(page, 10, email, admin);

    }

    @PostMapping
    public ResponseEntity<BookingDTO> booking(@RequestBody BookingDTO dto, Authentication auth) {
        BookingDTO savedBooking = bookingService.saveBookingAndReturn(dto, auth.getName(), isAdmin(auth));
        return  ResponseEntity.status(201).body(savedBooking);
    }



    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
