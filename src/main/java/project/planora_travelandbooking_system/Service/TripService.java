package project.planora_travelandbooking_system.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.Model.Trip;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.TripRepository;
import project.planora_travelandbooking_system.Repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Autowired
    public TripService(TripRepository tripRepository, UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
    }

    public TripDTO saveTrip(TripDTO tripDTO) {
        Optional<User> userOptional = userRepository.findById(tripDTO.getUserId());
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + tripDTO.getUserId());
        }

        User user = userOptional.get();
        Trip trip = convertToEntity(tripDTO, user);

        Trip savedTrip = tripRepository.save(trip);
        return convertToDTO(savedTrip);
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

    public List<TripDTO> getTripsByUserId(Long userId) {
        List<Trip> trips = tripRepository.findByUserId(userId);
        return trips.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new RuntimeException("Trip not found with ID " + tripId);
        }
        tripRepository.deleteById(tripId);
    }

    private Trip convertToEntity(TripDTO tripDTO, User user) {
        Trip trip = new Trip();
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
        tripDTO.setUserId(trip.getUser().getId());
        tripDTO.setTitle(trip.getTitle());
        tripDTO.setDescription(trip.getDescription());
        tripDTO.setStartDate(trip.getStartDate());
        tripDTO.setEndDate(trip.getEndDate());
        tripDTO.setCreatedAt(trip.getCreatedAt());
        return tripDTO;
    }

}