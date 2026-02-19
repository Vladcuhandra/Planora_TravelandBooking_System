package project.planora_travelandbooking_system.Controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Service.TripService;
import project.planora_travelandbooking_system.Service.UserService;

@Controller
public class TripController {

    private final TripService tripService;
    private final UserService userService;

    @Autowired
    public TripController(TripService tripService, UserService userService) {
        this.tripService = tripService;
        this.userService = userService;
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @GetMapping("/trips")
    public String trips(@RequestParam(defaultValue = "0") int page, Model model, Authentication auth) {
        String email = auth.getName();
        boolean admin = isAdmin(auth);
        int pageSize = 10;
        Page<TripDTO> tripPage = tripService.getAllTrips(page, pageSize);

        model.addAttribute("trips", tripPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tripPage.getTotalPages());
        model.addAttribute("isAdmin", admin);
        model.addAttribute("tripDto", new TripDTO());
        model.addAttribute("users", userService.getAllUsers());
        return "trips";
    }

    @PostMapping("/trips/save")
    public String saveTrip(@ModelAttribute TripDTO dto) {
        User user = userService.getUserId(dto.getUserId());
        tripService.saveTrip(dto);
        return "redirect:/trips";
    }

    @PostMapping("/trips/edit/{id}")
    public String updateTrip(@PathVariable Long id, @ModelAttribute TripDTO tripDTO) {
        User user = userService.getUserId(tripDTO.getUserId());
        TripDTO updatedTrip = tripService.updateTrip(id, tripDTO, user);
        return "redirect:/trips";
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    public String deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return "redirect:/trips";
    }

}
