package project.planora_travelandbooking_system.Controller.web;

import org.springframework.data.domain.Page;
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

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(defaultValue = "0") int page, Model model,
                           Authentication auth,
                           @RequestParam(value = "type", required = false) String type) {
        String email = auth.getName();
        boolean admin = isAdmin(auth);
        int pageSize = 10;
        Page<BookingDTO> bookingPage = bookingService.getAllBookings(page, pageSize, email, admin);

        String selectedType = (type == null) ? "" : type.trim().toUpperCase();
        if (!"TRANSPORT".equals(selectedType) && !"ACCOMMODATION".equals(selectedType)) {
            selectedType = "";
        }

        model.addAttribute("bookings", bookingPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        model.addAttribute("isAdmin", admin);

        BookingDTO createDto = new BookingDTO();
        if (!selectedType.isEmpty()) {
            createDto.setBookingType(selectedType);
        }
        model.addAttribute("bookingDto", createDto);

        model.addAttribute("selectedType", selectedType);

        model.addAttribute("bookingTypes", Booking.BookingType.values());
        model.addAttribute("bookingStatuses", Booking.BookingStatus.values());

        model.addAttribute("trips", admin
                ? tripRepository.findAll()
                : tripRepository.findByUserEmail(email));

        model.addAttribute("transports", transportRepository.findAll());
        model.addAttribute("accommodations", accommodationRepository.findAll());

        return "bookings";
    }

    @PostMapping("/bookings/save")
    public String saveBooking(@ModelAttribute BookingDTO dto, Authentication auth) {
        String email = auth.getName();
        bookingService.saveBooking(dto, email, isAdmin(auth));
        return "redirect:/bookings";
    }

    @PostMapping("/bookings/edit/{id}")
    public String updateBooking(@PathVariable Long id,
                                @ModelAttribute BookingDTO dto,
                                Authentication auth) {
        String email = auth.getName();
        bookingService.updateBooking(id, dto, email, isAdmin(auth));
        return "redirect:/bookings";
    }

    @RequestMapping(value = "/bookings/delete/{id}", method = RequestMethod.DELETE)
    public String deleteBooking(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        bookingService.deleteBooking(id, email, isAdmin(auth));
        return "redirect:/bookings";
    }
}
