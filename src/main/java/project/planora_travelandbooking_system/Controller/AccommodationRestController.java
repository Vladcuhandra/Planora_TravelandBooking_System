package project.planora_travelandbooking_system.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.AccommodationDTO;
import project.planora_travelandbooking_system.Service.AccommodationService;
import project.planora_travelandbooking_system.Service.TransportService;

import java.util.List;

@RestController
@RequestMapping("/api/accommodation")
public class AccommodationRestController {

    private final AccommodationService accommodationService;

    @Autowired
    public AccommodationRestController(AccommodationService accommodationService, TransportService transportService) {
        this.accommodationService = accommodationService;
    }

    @GetMapping
    public List<AccommodationDTO> list() {
         return accommodationService.getAllAccommodations();
    }

    @GetMapping("/{id}")
    public AccommodationDTO get(@PathVariable Long id) {
        return accommodationService.getAccommodationById(id);
    }

    @PostMapping
    public ResponseEntity<AccommodationDTO> create(@RequestBody AccommodationDTO accommodationDTO) {
        AccommodationDTO created = accommodationService.saveAccommodation(accommodationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public AccommodationDTO update(@PathVariable Long id
            ,@RequestBody AccommodationDTO accommodationDTO) {
        return accommodationService.updateAccommodation(id, accommodationDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
        return ResponseEntity.noContent().build();
    }
}
