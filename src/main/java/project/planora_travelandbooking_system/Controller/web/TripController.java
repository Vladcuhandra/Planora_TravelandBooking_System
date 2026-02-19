package project.planora_travelandbooking_system.Controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Service.TripService;
import project.planora_travelandbooking_system.Service.UserService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public String trips(@RequestParam(defaultValue = "0") int page,
                        Model model,
                        Authentication auth) {

        String email = auth.getName();
        boolean admin = isAdmin(auth);
        int pageSize = 10;

        Page<TripDTO> tripPage;
        List<UserDTO> users = new ArrayList<>();

        if (admin) {
            tripPage = tripService.getAllTrips(page, pageSize);
            users = userService.getAllUsers();
        } else {
            tripPage = tripService.getTripsByUserEmail(email, page, pageSize);

            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserDTO userDTO = userService.convertToDTO(user);
            model.addAttribute("users", Collections.singletonList(userDTO));
        }

        model.addAttribute("trips", tripPage.getContent());
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tripPage.getTotalPages());
        model.addAttribute("isAdmin", admin);
        model.addAttribute("tripDto", new TripDTO());

        return "trips";
    }

    @PostMapping("/trips/save")
    public String saveTrip(@ModelAttribute TripDTO dto,
                           Authentication auth) {
        tripService.saveTrip(dto, auth);
        return "redirect:/trips";
    }

    @PostMapping("/trips/edit/{id}")
    public String updateTrip(@PathVariable Long id,
                             @ModelAttribute TripDTO tripDTO,
                             Authentication auth) {
        tripService.updateTrip(id, tripDTO, auth);
        return "redirect:/trips";
    }

    @PostMapping("/delete/{id}")
    public String deleteTrip(@PathVariable Long id, Authentication auth) {
        tripService.deleteTrip(id, auth);
        return "redirect:/trips";
    }

}