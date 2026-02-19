package project.planora_travelandbooking_system.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.Model.Trip;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.BookingRepository;
import project.planora_travelandbooking_system.Repository.TripRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    public TripService(TripRepository tripRepository, UserService userService, BookingRepository bookingRepository) {
        this.tripRepository = tripRepository;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
    }

    public Page<TripDTO> getAllTrips(int page, int pageSize) {
        Page<Trip> tripPage = tripRepository.findAll(PageRequest.of(page, pageSize));
        return tripPage.map(this::convertToDTO);
    }

    public Page<TripDTO> getTripsForUser(String email, int page, int pageSize) {
        Page<Trip> tripPage = tripRepository.findByUserEmail(email, PageRequest.of(page, pageSize));
        return tripPage.map(this::convertToDTO);
    }

    public List<TripDTO> getAllTrips() {
        List<Trip> trips = tripRepository.findAll();
        return trips.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TripDTO getTripById(Long tripId) {
        Optional<Trip> trip = tripRepository.findById(tripId);
        if (trip.isPresent()) return convertToDTO(trip.get());
        throw new RuntimeException("Trip not found with ID: " + tripId);
    }

    public TripDTO saveTrip(TripDTO tripDTO) {
        DateValidation.endNotBeforeStart(
                tripDTO.getStartDate(),
                tripDTO.getEndDate(),
                "startDate",
                "endDate"
        );
        User user = userService.getUserId(tripDTO.getUserId());
        if (user == null) throw new RuntimeException("User not found with ID: " + tripDTO.getUserId());

        Trip trip = new Trip();
        trip.setTitle(tripDTO.getTitle());
        trip.setDescription(tripDTO.getDescription());
        trip.setStartDate(tripDTO.getStartDate());
        trip.setEndDate(tripDTO.getEndDate());
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUser(user);

        Trip savedTrip = tripRepository.save(trip);
        return convertToDTO(savedTrip);
    }

    public TripDTO updateTrip(Long tripId, TripDTO tripDTO, User user) {
        DateValidation.endNotBeforeStart(
                tripDTO.getStartDate(),
                tripDTO.getEndDate(),
                "startDate",
                "endDate"
        );
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + tripId));

        trip.setTitle(tripDTO.getTitle());
        trip.setDescription(tripDTO.getDescription());
        trip.setStartDate(tripDTO.getStartDate());
        trip.setEndDate(tripDTO.getEndDate());
        trip.setUser(user);

        Trip updatedTrip = tripRepository.save(trip);
        return convertToDTO(updatedTrip);
    }

    // KEEP: used by old controller mapping if needed
    public void deleteTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) throw new RuntimeException("Trip not found with ID: " + tripId);

        // block if any booking references it
        if (bookingRepository.existsByTripId(tripId)) {
            throw new RuntimeException("Cannot delete trip: it has bookings. Delete bookings first.");
        }

        tripRepository.deleteById(tripId);
    }

    @Transactional
    public void deleteTripAuthorized(Long tripId, String email, boolean isAdmin) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + tripId));

        if (!isAdmin) {
            if (trip.getUser() == null || trip.getUser().getEmail() == null) throw new RuntimeException("Access denied");
            if (!email.equals(trip.getUser().getEmail())) throw new RuntimeException("Access denied");
        }

        if (bookingRepository.existsByTripId(tripId)) {
            throw new RuntimeException("Cannot delete trip: it has bookings. Delete bookings first.");
        }

        tripRepository.deleteById(tripId);
    }

    @Transactional
    public void bulkDeleteTrips(List<Long> ids, String email, boolean isAdmin) {
        if (ids == null || ids.isEmpty()) return;

        List<Trip> trips = tripRepository.findAllById(ids);

        if (!isAdmin) {
            for (Trip t : trips) {
                if (t.getUser() == null || t.getUser().getEmail() == null) throw new RuntimeException("Access denied");
                if (!email.equals(t.getUser().getEmail())) throw new RuntimeException("Access denied");
            }
        }

        for (Trip t : trips) {
            if (bookingRepository.existsByTripId(t.getId())) {
                throw new RuntimeException("Cannot delete trip id " + t.getId() + ": it has bookings.");
            }
        }

        Set<Long> uniqueIds = new HashSet<>(ids);
        tripRepository.deleteAllByIdInBatch(uniqueIds);
    }

    private TripDTO convertToDTO(Trip trip) {
        TripDTO tripDTO = new TripDTO();
        tripDTO.setId(trip.getId());
        tripDTO.setTitle(trip.getTitle());
        tripDTO.setDescription(trip.getDescription());
        tripDTO.setStartDate(trip.getStartDate());
        tripDTO.setEndDate(trip.getEndDate());
        tripDTO.setUserId(trip.getUser().getId());
        tripDTO.setCreatedAt(trip.getCreatedAt());
        return tripDTO;
    }
}
