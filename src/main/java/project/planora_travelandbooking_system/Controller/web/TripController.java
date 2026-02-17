package project.planora_travelandbooking_system.Controller.web;

import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/trips")
    public String trips(Model model) {
        model.addAttribute("trips", tripService.getAllTrips());
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
