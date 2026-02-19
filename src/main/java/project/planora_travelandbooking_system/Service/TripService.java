package project.planora_travelandbooking_system.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.Model.Trip;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.TripRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserService userService;

    public TripService(TripRepository tripRepository, UserService userService) {
        this.tripRepository = tripRepository;
        this.userService = userService;
    }

    public Page<TripDTO> getAllTrips(int page, int pageSize) {
        Page<Trip> tripPage = tripRepository.findAll(PageRequest.of(page, pageSize));
        return tripPage.map(this::convertToDTO);
    }

    public List<TripDTO> getAllTrips() {
        List<Trip> trips = tripRepository.findAll();
        return trips.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TripDTO getTripById(Long tripId) {
        Optional<Trip> trip = tripRepository.findById(tripId);
        if (trip.isPresent()) {
            return convertToDTO(trip.get());
        } else {
            throw new RuntimeException("Trip not found with ID: " + tripId);
        }
    }

    public TripDTO saveTrip(TripDTO tripDTO) {
        User user = userService.getUserId(tripDTO.getUserId());

        if (user == null) {
            throw new RuntimeException("User not found with ID: " + tripDTO.getUserId());
        }

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

    public void deleteTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new RuntimeException("Trip not found with ID: " + tripId);
        }
        tripRepository.deleteById(tripId);
    }

    private Trip convertToEntity(TripDTO tripDTO, User user) {
        Trip trip = new Trip();
        trip.setId(tripDTO.getId());
        trip.setTitle(tripDTO.getTitle());
        trip.setDescription(tripDTO.getDescription());
        trip.setStartDate(tripDTO.getStartDate());
        trip.setEndDate(tripDTO.getEndDate());
        trip.setUser(user);
        trip.setCreatedAt(LocalDateTime.now());
        return trip;
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