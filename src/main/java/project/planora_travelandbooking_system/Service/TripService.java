package project.planora_travelandbooking_system.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.DTO.TripDTO;
import project.planora_travelandbooking_system.Model.Accommodation;
import project.planora_travelandbooking_system.Model.Transport;
import project.planora_travelandbooking_system.Model.Trip;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.AccommodationRepository;
import project.planora_travelandbooking_system.Repository.TransportRepository;
import project.planora_travelandbooking_system.Repository.TripRepository;
import project.planora_travelandbooking_system.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TransportRepository transportRepository;
    private final AccommodationRepository accommodationRepository;

    public TripService(TripRepository tripRepository,
                       UserRepository userRepository,
                       TransportRepository transportRepository,
                       AccommodationRepository accommodationRepository) {
        this.tripRepository = tripRepository;
        this.userRepository = userRepository;
        this.transportRepository = transportRepository;
        this.accommodationRepository = accommodationRepository;
    }

    public List<Trip> getTripsForUser(String email, boolean isAdmin) {
        if (isAdmin) return tripRepository.findAll();
        return tripRepository.findByUserEmail(email);
    }

    public Trip getTripForUser(Long id, String email, boolean isAdmin) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found: " + id));

        if (!isAdmin && !trip.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Access denied");
        }
        return trip;
    }

    /*@Transactional
    public void createTrip(TripDTO dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Transport transport = transportRepository.findById(dto.getTransportId())
                .orElseThrow(() -> new RuntimeException("Transport not found: " + dto.getTransportId()));

        Accommodation accommodation = accommodationRepository.findById(dto.getAccommodationId())
                .orElseThrow(() -> new RuntimeException("Accommodation not found: " + dto.getAccommodationId()));

        Trip trip = new Trip();
        trip.setTitle(dto.getTitle());
        trip.setDescription(dto.getDescription());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());
        trip.setTransport(transport);
        trip.setAccommodation(accommodation);
        trip.setUser(user);
        trip.setCreatedAt(LocalDateTime.now());

        tripRepository.save(trip);
    }

    @Transactional
    public void updateTrip(Long id, TripDTO dto, String email, boolean isAdmin) {
        Trip trip = getTripForUser(id, email, isAdmin);

        Transport transport = transportRepository.findById(dto.getTransportId())
                .orElseThrow(() -> new RuntimeException("Transport not found: " + dto.getTransportId()));

        Accommodation accommodation = accommodationRepository.findById(dto.getAccommodationId())
                .orElseThrow(() -> new RuntimeException("Accommodation not found: " + dto.getAccommodationId()));

        trip.setTitle(dto.getTitle());
        trip.setDescription(dto.getDescription());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());
        trip.setTransport(transport);
        trip.setAccommodation(accommodation);

        tripRepository.save(trip);
    }

    @Transactional
    public void deleteTrip(Long id, String email, boolean isAdmin) {
        Trip trip = getTripForUser(id, email, isAdmin);
        tripRepository.delete(trip);
    }

    public TripDTO toDTO(Trip trip) {
        TripDTO dto = new TripDTO();
        dto.setId(trip.getId());
        dto.setTitle(trip.getTitle());
        dto.setDescription(trip.getDescription());
        dto.setStartDate(trip.getStartDate());
        dto.setEndDate(trip.getEndDate());
        dto.setTransportId(trip.getTransport().getId());
        dto.setAccommodationId(trip.getAccommodation().getId());
        return dto;
    }*/
}
