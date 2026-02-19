package project.planora_travelandbooking_system.Controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.Service.TripService;
import project.planora_travelandbooking_system.Service.UserService;
import java.util.List;

@RestController
@RequestMapping("/api/trip")
public class TripRestController {

    private final TripService tripService;
    private final UserService userService;

    @Autowired
    public TripRestController(TripService tripService, UserService userService) {
        this.tripService = tripService;
        this.userService = userService;
    }

    @GetMapping
    public List<TripDTO> getAllTrips() {
        return tripService.getAllTrips();
    }

    @PostMapping
    public ResponseEntity<TripDTO> create(@RequestBody TripDTO tripDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TripDTO created = tripService.saveTrip(tripDTO, auth);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public TripDTO getTrip(@PathVariable Long id) {
        return tripService.getTripById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TripDTO> update(@PathVariable Long id,
                                          @RequestBody TripDTO tripDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TripDTO updated = tripService.updateTrip(id, tripDTO, auth);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        tripService.deleteTrip(id, auth);
        return ResponseEntity.noContent().build();
    }

}
