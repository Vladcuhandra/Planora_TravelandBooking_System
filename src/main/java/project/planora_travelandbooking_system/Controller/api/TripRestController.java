package project.planora_travelandbooking_system.Controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Service.TripService;
import project.planora_travelandbooking_system.Service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripRestController {

    private final TripService tripService;
    private final UserService userService;

    public TripRestController(TripService tripService, UserService userService) {
        this.tripService = tripService;
        this.userService = userService;
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    // GET all trips (admin: all, user: own)
    @GetMapping
    public ResponseEntity<List<TripDTO>> getTrips(Authentication auth) {
        boolean admin = isAdmin(auth);
        if (admin) return ResponseEntity.ok(tripService.getAllTrips());
        return ResponseEntity.ok(tripService.getTripsForUser(auth.getName(), 0, Integer.MAX_VALUE).getContent());
    }

    // GET by id
    @GetMapping("/{id}")
    public ResponseEntity<TripDTO> getTrip(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTripById(id));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<TripDTO> createTrip(@RequestBody TripDTO dto, Authentication auth) {
        boolean admin = isAdmin(auth);

        if (!admin) {
            User current = userService.getUserByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            dto.setUserId(current.getId());
        }
        // admin can provide userId in dto as before

        return ResponseEntity.ok(tripService.saveTrip(dto));
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<TripDTO> updateTrip(@PathVariable Long id,
                                              @RequestBody TripDTO dto,
                                              Authentication auth) {
        boolean admin = isAdmin(auth);

        User user;
        if (admin) {
            // admin can update and can assign to userId provided in dto
            user = userService.getUserId(dto.getUserId());
            if (user == null) throw new RuntimeException("User not found with ID: " + dto.getUserId());
        } else {
            user = userService.getUserByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            dto.setUserId(user.getId());
        }

        return ResponseEntity.ok(tripService.updateTrip(id, dto, user));
    }

    // DELETE (blocked if trip has bookings, per your current TripService)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrip(@PathVariable Long id, Authentication auth) {
        tripService.deleteTripAuthorized(id, auth.getName(), isAdmin(auth));
        return ResponseEntity.noContent().build();
    }

    // BULK DELETE
    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDelete(@RequestBody List<Long> ids, Authentication auth) {
        tripService.bulkDeleteTrips(ids, auth.getName(), isAdmin(auth));
        return ResponseEntity.noContent().build();
    }
}
