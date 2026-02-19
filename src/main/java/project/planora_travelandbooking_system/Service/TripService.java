package project.planora_travelandbooking_system.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
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
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final UserService userService;

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

    public Page<TripDTO> getTripsByUserEmail(String email, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tripRepository
                .findByUserEmail(email, pageable)
                .map(this::convertToDTO);
    }

    public TripDTO saveTrip(TripDTO dto, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        String email = auth.getName();
        User user;

        if (admin) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            dto.setUserId(user.getId()); // enforce ownership
        }

        Trip trip = new Trip();
        trip.setTitle(dto.getTitle());
        trip.setDescription(dto.getDescription());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());
        trip.setCreatedAt(LocalDateTime.now());
        trip.setUser(user);

        tripRepository.save(trip);
        return convertToDTO(trip);
    }

    public TripDTO updateTrip(Long id, TripDTO dto, Authentication auth) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String email = auth.getName();

        if (!admin && !trip.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("Not allowed");
        }

        trip.setTitle(dto.getTitle());
        trip.setDescription(dto.getDescription());
        trip.setStartDate(dto.getStartDate());
        trip.setEndDate(dto.getEndDate());

        if (admin && dto.getUserId() != null) {
            User newUser = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            trip.setUser(newUser);
        }

        tripRepository.save(trip);
        return convertToDTO(trip);
    }

    public void deleteTrip(Long id, Authentication auth) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String email = auth.getName();

        if (!admin && !trip.getUser().getEmail().equals(email)) {
            throw new AccessDeniedException("Not allowed");
        }

        tripRepository.delete(trip);
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
        tripDTO.setUserEmail(trip.getUser().getEmail());
        tripDTO.setCreatedAt(trip.getCreatedAt());
        return tripDTO;
    }

}