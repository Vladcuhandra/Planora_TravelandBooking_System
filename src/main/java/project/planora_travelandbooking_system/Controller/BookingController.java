package project.planora_travelandbooking_system.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.BookingDTO;
import project.planora_travelandbooking_system.Model.Booking;
import project.planora_travelandbooking_system.Repository.AccommodationRepository;
import project.planora_travelandbooking_system.Repository.TransportRepository;
import project.planora_travelandbooking_system.Repository.TripRepository;
import project.planora_travelandbooking_system.Service.BookingService;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final TripRepository tripRepository;
    private final TransportRepository transportRepository;
    private final AccommodationRepository accommodationRepository;

    public BookingController(BookingService bookingService,
                             TripRepository tripRepository,
                             TransportRepository transportRepository,
                             AccommodationRepository accommodationRepository) {
        this.bookingService = bookingService;
        this.tripRepository = tripRepository;
        this.transportRepository = transportRepository;
        this.accommodationRepository = accommodationRepository;
    }

    @GetMapping("/api/bookings")
    public String bookings(Model model, Authentication auth) {

        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        model.addAttribute("bookings", bookingService.getBookings(email, isAdmin));
        model.addAttribute("bookingDto", new BookingDTO());

        model.addAttribute("bookingTypes", Booking.BookingType.values());
        model.addAttribute("bookingStatuses", Booking.BookingStatus.values());

        model.addAttribute("trips", isAdmin
                ? tripRepository.findAll()
                : tripRepository.findByUserEmail(email));

        model.addAttribute("transports", transportRepository.findAll());
        model.addAttribute("accommodations", accommodationRepository.findAll());

        return "bookings";
    }

    @PostMapping("/api/bookings/save")
    public String saveBooking(@ModelAttribute BookingDTO dto, Authentication auth) {

        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        bookingService.saveBooking(dto, email, isAdmin);
        return "redirect:/api/bookings";
    }

    @PostMapping("/api/bookings/edit/{id}")
    public String updateBooking(@PathVariable Long id,
                                @ModelAttribute BookingDTO dto,
                                Authentication auth) {

        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        bookingService.updateBooking(id, dto, email, isAdmin);
        return "redirect:/api/bookings";
    }

    @RequestMapping(value = "/api/bookings/delete/{id}", method = RequestMethod.DELETE)
    public String deleteBooking(@PathVariable Long id, Authentication auth) {

        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        bookingService.deleteBooking(id, email, isAdmin);
        return "redirect:/api/bookings";
    }
}
