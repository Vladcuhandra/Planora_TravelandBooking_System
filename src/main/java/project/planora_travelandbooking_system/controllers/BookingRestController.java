package project.planora_travelandbooking_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.dtos.BookingDTO;
import project.planora_travelandbooking_system.models.Accommodation;
import project.planora_travelandbooking_system.models.Transport;
import project.planora_travelandbooking_system.models.Trip;
import project.planora_travelandbooking_system.reposiitory.AccommodationRepository;
import project.planora_travelandbooking_system.reposiitory.TransportRepository;
import project.planora_travelandbooking_system.reposiitory.TripRepository;
import project.planora_travelandbooking_system.services.BookingService;
import java.util.List;
import org.springframework.data.domain.Page;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private final BookingService bookingService;
    private final TripRepository tripRepository;
    private final TransportRepository transportRepository;
    private final AccommodationRepository accommodationRepository;

    @Autowired
    public BookingRestController(BookingService bookingService,
                                 TripRepository tripRepository,
                                 TransportRepository transportRepository,
                                 AccommodationRepository accommodationRepository) {
        this.bookingService = bookingService;
        this.tripRepository = tripRepository;
        this.transportRepository = transportRepository;
        this.accommodationRepository = accommodationRepository;
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_SUPER_ADMIN".equals(a.getAuthority()));
    }

    // Get all bookings with pagination and optional filtering by type
    /*@GetMapping
    public ResponseEntity<Page<BookingDTO>> getBookings(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(value = "type", required = false) String type,
                                                        Authentication auth) {

        int email = Integer.parseInt(auth.getName());
        boolean admin = isAdmin(auth);
        String selectedType = (type == null) ? "" : type.trim().toUpperCase();
        if (!"TRANSPORT".equals(selectedType) && !"ACCOMMODATION".equals(selectedType)) {
            selectedType = "";
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<BookingDTO> bookingPage = bookingService.getAllBookings(page, size, email, admin);

        return ResponseEntity.ok(bookingPage);
    }*/
    /*@GetMapping
    public List<BookingDTO> list() {
        return bookingService.getAllBookings();
    }*/

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        String email = auth.getName();
        boolean admin = isAdmin(auth);

        Page<BookingDTO> bookingPage =
                bookingService.getAllBookings(page, size, email, admin);

        Map<String, Object> response = new HashMap<>();
        response.put("bookings", bookingPage.getContent());
        response.put("currentPage", bookingPage.getNumber());
        response.put("totalPages", bookingPage.getTotalPages());
        response.put("totalItems", bookingPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    // Update an existing booking
    @PostMapping("/edit/{id}")
    public ResponseEntity<BookingDTO> updateBooking(@PathVariable Long id,
                                                    @RequestBody BookingDTO dto,
                                                    Authentication auth) {
        bookingService.updateBooking(id, dto, auth.getName(), isAdmin(auth));
        return ResponseEntity.ok(dto);
    }

    // Delete a single booking by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id, Authentication auth) {
        bookingService.deleteBooking(id, auth.getName(), isAdmin(auth));
        return ResponseEntity.noContent().build();
    }

    // Bulk delete bookings
    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDeleteBookings(@RequestBody List<Long> ids, Authentication auth) {
        bookingService.bulkDeleteBookings(ids, auth.getName(), isAdmin(auth));
        return ResponseEntity.noContent().build();
    }
    // Create a new booking
    @PostMapping("/save")
    public ResponseEntity<Void> saveBooking(@RequestBody BookingDTO dto, Authentication auth) {
        bookingService.saveBooking(dto, auth.getName(), isAdmin(auth));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Get all trips (for creating/editing bookings)
    @GetMapping("/trips")
    public ResponseEntity<List<Trip>> getTrips(Authentication auth) {
        String email = auth.getName();
        boolean admin = isAdmin(auth);
        List<Trip> trips = admin ? tripRepository.findAll() : tripRepository.findByUserEmail(email);
        return ResponseEntity.ok(trips);
    }

    // Get all transports (for creating/editing bookings)
    @GetMapping("/transports")
    public ResponseEntity<List<Transport>> getTransports() {
        List<Transport> transports = transportRepository.findAll();
        return ResponseEntity.ok(transports);
    }

    // Get all accommodations (for creating/editing bookings)
    @GetMapping("/accommodations")
    public ResponseEntity<List<Accommodation>> getAccommodations() {
        List<Accommodation> accommodations = accommodationRepository.findAll();
        return ResponseEntity.ok(accommodations);
    }

}