package project.planora_travelandbooking_system.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.AccommodationDTO;
import project.planora_travelandbooking_system.Model.Accommodation;
import project.planora_travelandbooking_system.Service.AccommodationService;

@Controller
public class AccommodationController {

    private final AccommodationService accommodationService;

    @Autowired
    public AccommodationController(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    @GetMapping("/accommodations")
    public String accommodations(Model model) {
        model.addAttribute("accommodations", accommodationService.getAllAccommodations());
        return "accommodations";
    }

    @GetMapping("/accommodations/new")
    public String newAccommodation(Model model) {
        model.addAttribute("accommodationDto", new AccommodationDTO());
        model.addAttribute("accommodationTypes", Accommodation.AccommodationType.values());
        model.addAttribute("statuses", Accommodation.Status.values());
        return "accommodation-new";
    }

    @PostMapping("/accommodations/save")
    public String saveAccommodation(@ModelAttribute AccommodationDTO dto) {
        accommodationService.saveAccommodation(dto);
        return "redirect:/accommodations";
    }

    @DeleteMapping("/accommodations/{id}")
    public String deleteAccommodation(@PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
        return "redirect:/accommodations";
    }

    @GetMapping("/accommodations/{id}/edit")
    public String editAccommodation(@PathVariable Long id, Model model) {
        model.addAttribute("accommodationDto", accommodationService.getAccommodationById(id));
        model.addAttribute("accommodationTypes", Accommodation.AccommodationType.values());
        model.addAttribute("statuses", Accommodation.Status.values());
        return "accommodation-edit";
    }

    @PutMapping("/accommodations/{id}")
    public String updateAccommodation(@PathVariable Long id,
                                      @ModelAttribute("accommodationDto") AccommodationDTO dto) {
        accommodationService.updateAccommodation(id, dto);
        return "redirect:/accommodations";
    }
}
