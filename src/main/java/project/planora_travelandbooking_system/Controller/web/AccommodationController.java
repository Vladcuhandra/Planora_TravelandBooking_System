package project.planora_travelandbooking_system.Controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
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

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @GetMapping("/accommodations")
    public String accommodations(@RequestParam(defaultValue = "0") int page, Model model, Authentication auth) {
        String email = auth.getName();
        boolean admin = isAdmin(auth);
        int pageSize = 10;
        Page<AccommodationDTO> accommodationPage = accommodationService.getAllAccommodations(page, pageSize);

        model.addAttribute("accommodations", accommodationPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", accommodationPage.getTotalPages());
        model.addAttribute("isAdmin", admin);
        model.addAttribute("accommodationDto", new AccommodationDTO());
        model.addAttribute("accommodationTypes", Accommodation.AccommodationType.values());
        model.addAttribute("statuses", Accommodation.Status.values());
        return "accommodations";
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

    @PostMapping("/accommodations/edit")
    public String editAccommodation(@RequestParam Long accommodationId,
                                    @RequestParam String accommodationType,
                                    @RequestParam String name,
                                    @RequestParam String city,
                                    @RequestParam String address,
                                    @RequestParam double rating,
                                    @RequestParam int room,
                                    @RequestParam double pricePerNight,
                                    @RequestParam String status,
                                    Model model) {

        System.out.println("Received request to update accommodation with ID: " + accommodationId);

        AccommodationDTO accommodationDTO = new AccommodationDTO();
        accommodationDTO.setId(accommodationId);
        accommodationDTO.setAccommodationType(accommodationType);
        accommodationDTO.setName(name);
        accommodationDTO.setCity(city);
        accommodationDTO.setAddress(address);
        accommodationDTO.setRating(rating);
        accommodationDTO.setRoom(room);
        accommodationDTO.setPricePerNight(pricePerNight);
        accommodationDTO.setStatus(status);

        try {
            accommodationService.saveAccommodation(accommodationDTO);
            System.out.println("Accommodation updated successfully!");
            return "redirect:/accommodations";
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return "redirect:/accommodations?error=" + e.getMessage();
        }
    }

    @PutMapping("/accommodations/{id}")
    public String updateAccommodation(@PathVariable Long id,
                                      @ModelAttribute("accommodationDto") AccommodationDTO dto) {
        accommodationService.updateAccommodation(id, dto);
        return "redirect:/accommodations";
    }
}
