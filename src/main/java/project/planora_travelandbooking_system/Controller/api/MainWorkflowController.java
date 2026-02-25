package project.planora_travelandbooking_system.Controller.api;

import jakarta.transaction.Transactional;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.AccommodationDTO;
import project.planora_travelandbooking_system.DTO.BookingDTO;
import project.planora_travelandbooking_system.DTO.TransportDTO;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Service.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/main")
@CrossOrigin
public class MainWorkflowController {

    private final TripService tripService;
    private final TransportService transportService;
    private final AccommodationService accommodationService;
    private final BookingService bookingService;
    private final UserService userService;

    public MainWorkflowController(
            TripService tripService,
            TransportService transportService,
            AccommodationService accommodationService,
            BookingService bookingService,
            UserService userService
    ) {
        this.tripService = tripService;
        this.transportService = transportService;
        this.accommodationService = accommodationService;
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping("/save-booking")
    public ResponseEntity<?> saveBookingViaWorkflow(@RequestBody MainCreateBookingRequest req,
                                                    Authentication auth) {

        if (req == null || req.getBooking() == null) {
            return ResponseEntity.badRequest().body("booking is required");
        }

        BookingDTO booking = req.getBooking();

        Long tripId = booking.getTripId();

        if (tripId == null && req.getTrip() != null) {
            tripId = req.getTrip().getId();
        }

        if (tripId == null) {
            if (req.getTrip() == null) {
                return ResponseEntity.badRequest().body("tripId is required (either booking.tripId or trip.id or provide trip to create)");
            }

            // attach user if missing
            if (req.getTrip().getUserId() == null) {
                var user = userService.getUserByEmail(auth.getName())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                req.getTrip().setUserId(user.getId());
            }

            TripDTO savedTrip = tripService.saveTrip(req.getTrip());
            tripId = savedTrip.getId();
        }

        booking.setTripId(tripId);

        String bt = booking.getBookingType();
        if ("TRANSPORT".equalsIgnoreCase(bt)) {
            if (booking.getTransportId() == null) {
                return ResponseEntity.badRequest().body("transportId is required for TRANSPORT booking");
            }
            booking.setAccommodationId(null);
        } else if ("ACCOMMODATION".equalsIgnoreCase(bt)) {
            if (booking.getAccommodationId() == null) {
                return ResponseEntity.badRequest().body("accommodationId is required for ACCOMMODATION booking");
            }
            booking.setTransportId(null);
        } else {
            return ResponseEntity.badRequest().body("Invalid bookingType (TRANSPORT or ACCOMMODATION)");
        }

        boolean isAdmin = isAdmin(auth);
        bookingService.saveBooking(booking, auth.getName(), isAdmin);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Booking saved");
        response.put("tripId", tripId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/save-trip-and-booking")
    @Transactional
    public ResponseEntity<?> saveTripAndBooking(@RequestBody SaveTripAndBookingRequest req,
                                                Authentication auth) {

        if (req == null || req.getTrip() == null || req.getBooking() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "trip and booking are required"));
        }

        TripDTO tripDto = req.getTrip();
        BookingDTO bookingDto = req.getBooking();

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

        // ensure userId for non-admin
        if (!isAdmin && tripDto.getUserId() == null) {
            User current = userService.getUserByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            tripDto.setUserId(current.getId());
        }

        TripDTO savedTrip = tripService.saveTrip(tripDto);
        if (savedTrip.getId() == null) {
            throw new RuntimeException("Trip saved but id is null (unexpected)");
        }

        bookingDto.setTripId(savedTrip.getId());

        if ("TRANSPORT".equalsIgnoreCase(bookingDto.getBookingType())) {
            if (bookingDto.getTransportId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "transportId required for TRANSPORT"));
            }
            bookingDto.setAccommodationId(null);
        } else if ("ACCOMMODATION".equalsIgnoreCase(bookingDto.getBookingType())) {
            if (bookingDto.getAccommodationId() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "accommodationId required for ACCOMMODATION"));
            }
            bookingDto.setTransportId(null);
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "bookingType must be TRANSPORT or ACCOMMODATION"));
        }

        bookingService.saveBooking(bookingDto, auth.getName(), isAdmin);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Trip + Booking saved",
                "trip", savedTrip
        ));
    }

    /** request wrapper */
    public static class SaveTripAndBookingRequest {
        private TripDTO trip;
        private BookingDTO booking;

        public TripDTO getTrip() { return trip; }
        public void setTrip(TripDTO trip) { this.trip = trip; }

        public BookingDTO getBooking() { return booking; }
        public void setBooking(BookingDTO booking) { this.booking = booking; }
    }

    @Data
    public static class MainCreateBookingRequest {
        private TripDTO trip;
        private BookingDTO booking;
        private TransportDTO transport;           // required if bookingType=TRANSPORT
        private AccommodationDTO accommodation;   // required if bookingType=ACCOMMODATION
    }


    private boolean isAdmin(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            return "ROLE_ADMIN".equals(role) || "ADMIN".equals(role);
        });
    }
}