package project.planora_travelandbooking_system.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.TransportDTO;
import project.planora_travelandbooking_system.Model.Transport;
import project.planora_travelandbooking_system.Service.TransportService;

@Controller
public class TransportController {

    private final TransportService transportService;

    @Autowired
    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    @GetMapping("/transports")
    public String transports(Model model) {
        model.addAttribute("transports", transportService.getAllTransports());
        return "transports";
    }

    @GetMapping("/transports/new")
    public String newTransport(Model model) {
        model.addAttribute("transportDto", new TransportDTO());
        model.addAttribute("transportTypes", Transport.TransportType.values());
        model.addAttribute("statuses", Transport.Status.values());
        return "transport-new";
    }

    @PostMapping("/transports/save")
    public String newTransport(@ModelAttribute TransportDTO dto, Model model) {
        transportService.saveTransport(dto);
        return "redirect:/transports";
    }

    @DeleteMapping("/transports/{id}")
    public String deleteTransport(@PathVariable Long id) {
        transportService.deleteTransport(id);
        return "redirect:/transports";
    }

    @GetMapping("/transports/{id}/edit")
    public String editTransport(@PathVariable Long id, Model model) {
        model.addAttribute("transportDto", transportService.getTransportById(id));
        model.addAttribute("transportTypes", Transport.TransportType.values());
        model.addAttribute("statuses", Transport.Status.values());
        return "transport-edit";
    }

    @PutMapping("/transports/{id}")
    public String updateTransport(@PathVariable Long id,
                                  @ModelAttribute("transportDto") TransportDTO dto) {
        transportService.updateTransport(id, dto);
        return "redirect:/transports";
    }



}