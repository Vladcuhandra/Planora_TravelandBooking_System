package project.planora_travelandbooking_system.Controller.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.TransportDTO;
import project.planora_travelandbooking_system.Model.Transport;
import project.planora_travelandbooking_system.Service.TransportService;

import java.time.LocalDateTime;

@Controller
public class TransportController {

    private final TransportService transportService;

    @Autowired
    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @GetMapping("/transports")
    public String transports(Model model, Authentication auth) {
        String email = auth.getName();
        boolean admin = isAdmin(auth);

        model.addAttribute("isAdmin", admin);
        model.addAttribute("transports", transportService.getAllTransports());
        model.addAttribute("transportDto", new TransportDTO());
        model.addAttribute("transportTypes", Transport.TransportType.values());
        model.addAttribute("statuses", Transport.Status.values());

        return "transports";
    }

    @PostMapping("/transports/save")
    public String saveTransport(@ModelAttribute TransportDTO dto, Model model) {
        transportService.saveTransport(dto);
        return "redirect:/transports";
    }

    @DeleteMapping("/transports/{id}")
    public String deleteTransport(@PathVariable Long id) {
        transportService.deleteTransport(id);
        return "redirect:/transports";
    }

    @PostMapping("/transports/edit")
    public String editTransport(@RequestParam Long transportId,
                                @RequestParam String transportType,
                                @RequestParam String company,
                                @RequestParam String originAddress,
                                @RequestParam String destinationAddress,
                                @RequestParam String departureTime,
                                @RequestParam String arrivalTime,
                                @RequestParam int seat,
                                @RequestParam double price,
                                @RequestParam String status,
                                Model model) {

        System.out.println("Received request to update transport with ID: " + transportId);

        TransportDTO transportDTO = new TransportDTO();
        transportDTO.setId(transportId);
        transportDTO.setTransportType(transportType);
        transportDTO.setCompany(company);
        transportDTO.setOriginAddress(originAddress);
        transportDTO.setDestinationAddress(destinationAddress);
        transportDTO.setDepartureTime(LocalDateTime.parse(departureTime));
        transportDTO.setArrivalTime(LocalDateTime.parse(arrivalTime));
        transportDTO.setSeat(seat);
        transportDTO.setPrice(price);
        transportDTO.setStatus(status);

        try {
            transportService.saveTransport(transportDTO);
            System.out.println("Transport updated successfully!");
            return "redirect:/transports";
        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
            return "redirect:/transports?error=" + e.getMessage();
        }
    }

    @PutMapping("/transports/{id}")
    public String updateTransport(@PathVariable Long id,
                                  @ModelAttribute("transportDto") TransportDTO dto) {
        transportService.updateTransport(id, dto);
        return "redirect:/transports";
    }

}