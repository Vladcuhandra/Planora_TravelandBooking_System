package project.planora_travelandbooking_system.Controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.Model.Trip;
import project.planora_travelandbooking_system.Service.AccommodationService;
import project.planora_travelandbooking_system.Service.TransportService;
import project.planora_travelandbooking_system.Service.TripService;

import java.util.List;

@Controller
public class TripController {

    private final TripService tripService;
    private final TransportService transportService;
    private final AccommodationService accommodationService;

    public TripController(TripService tripService,
                          TransportService transportService,
                          AccommodationService accommodationService) {
        this.tripService = tripService;
        this.transportService = transportService;
        this.accommodationService = accommodationService;
    }

    @GetMapping("/trips")
    public String trips(Model model, Authentication auth) {
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        List<Trip> trips = tripService.getTripsForUser(email, isAdmin);
        model.addAttribute("trips", trips);
        model.addAttribute("isAdmin", isAdmin);
        return "trips";
    }

    @GetMapping("/trips/new")
    public String newTrip(Model model) {
        model.addAttribute("tripDto", new TripDTO());
        model.addAttribute("transports", transportService.getAllTransports());
        model.addAttribute("accommodations", accommodationService.getAllAccommodations());
        return "trip-new";
    }

    @PostMapping("/trips/save")
    public String saveTrip(@ModelAttribute("tripDto") TripDTO dto, Authentication auth) {
        tripService.createTrip(dto, auth.getName());
        return "redirect:/trips";
    }

    @GetMapping("/trips/{id}/edit")
    public String editTrip(@PathVariable Long id, Model model, Authentication auth) {
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Trip trip = tripService.getTripForUser(id, email, isAdmin);

        model.addAttribute("tripDto", tripService.toDTO(trip));
        model.addAttribute("transports", transportService.getAllTransports());
        model.addAttribute("accommodations", accommodationService.getAllAccommodations());
        return "trip-edit";
    }

    @PutMapping("/trips/{id}")
    public String updateTrip(@PathVariable Long id,
                             @ModelAttribute("tripDto") TripDTO dto,
                             Authentication auth) {
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        tripService.updateTrip(id, dto, email, isAdmin);
        return "redirect:/trips";
    }

    @DeleteMapping("/trips/{id}")
    public String deleteTrip(@PathVariable Long id, Authentication auth) {
        String email = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        tripService.deleteTrip(id, email, isAdmin);
        return "redirect:/trips";
    }
}
