package project.planora_travelandbooking_system.Controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.AccommodationDTO;
import project.planora_travelandbooking_system.Model.Accommodation;
import project.planora_travelandbooking_system.Service.AccommodationService;
import project.planora_travelandbooking_system.Service.TransportService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accommodations")
public class AccommodationRestController {

    private final AccommodationService accommodationService;

    @Autowired
    public AccommodationRestController(AccommodationService accommodationService, TransportService transportService) {
        this.accommodationService = accommodationService;
    }

    @GetMapping
    public ResponseEntity<Page<AccommodationDTO>> getAccommodations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<AccommodationDTO> accommodations = accommodationService.getAllAccommodations(page, size);
        return ResponseEntity.ok(accommodations);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/save")
    public ResponseEntity<String> saveAccommodation(@RequestBody AccommodationDTO dto) {
        accommodationService.saveAccommodation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Accommodation created successfully!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/edit")
    public ResponseEntity<String> editAccommodation(@RequestBody AccommodationDTO dto) {
        accommodationService.saveAccommodation(dto);
        return ResponseEntity.ok("Accommodation updated successfully!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccommodation(@PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
        return ResponseEntity.ok("Accommodation deleted successfully!");
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getAccommodationTypes() {
        List<String> types = Arrays.stream(Accommodation.AccommodationType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }

    // Get all accommodation statuses (e.g., Available, Unavailable)
    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getAccommodationStatuses() {
        List<String> statuses = Arrays.stream(Accommodation.Status.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(statuses);
    }
}