package project.planora_travelandbooking_system.Controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Service.TripService;
import project.planora_travelandbooking_system.Service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
public class TripRestController {

    private final TripService tripService;
    private final UserService userService;

    @Autowired
    public TripRestController(TripService tripService, UserService userService) {
        this.tripService = tripService;
        this.userService = userService;
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTrips(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        Authentication auth) {
        String email = auth.getName();
        boolean admin = isAdmin(auth);

        System.out.println("Fetching trips for user: " + email + " | Admin: " + admin);

        Page<TripDTO> tripPage = admin
                ? tripService.getAllTrips(page, size)
                : tripService.getTripsForUser(email, page, size);

        System.out.println("Trips fetched: " + tripPage.getContent());

        Map<String, Object> response = new HashMap<>();
        response.put("trips", tripPage.getContent());
        response.put("currentPage", tripPage.getNumber());
        response.put("totalPages", tripPage.getTotalPages());
        response.put("totalItems", tripPage.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveTrip(@RequestBody TripDTO dto, Authentication auth) {
        boolean admin = isAdmin(auth);

        if (!admin) {
            User current = userService.getUserByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            dto.setUserId(current.getId());
        }

        tripService.saveTrip(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trip saved successfully");
        response.put("trip", dto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Update a trip
    @PutMapping("/edit/{id}")
    public ResponseEntity<Map<String, Object>> updateTrip(@PathVariable Long id, @RequestBody TripDTO tripDTO, Authentication auth) {
        boolean admin = isAdmin(auth);
        User user;

        if (admin) {
            user = userService.getUserId(tripDTO.getUserId());
        } else {
            user = userService.getUserByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            tripDTO.setUserId(user.getId()); // Ensure user is set to the logged-in user
        }

        tripService.updateTrip(id, tripDTO, user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trip updated successfully");
        response.put("trip", tripDTO);  // Return the updated trip as part of the response

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Delete a trip
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteTrip(@PathVariable Long id, Authentication auth) {
        tripService.deleteTripAuthorized(id, auth.getName(), isAdmin(auth));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trip deleted successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Bulk delete trips
    @PostMapping("/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDeleteTrips(@RequestBody List<Long> ids, Authentication auth) {
        tripService.bulkDeleteTrips(ids, auth.getName(), isAdmin(auth));

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Trips deleted successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}