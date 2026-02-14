package project.planora_travelandbooking_system.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.DTO.AccommodationDTO;
import project.planora_travelandbooking_system.Model.Accommodation;
import project.planora_travelandbooking_system.Repository.AccommodationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;

    @Autowired
    public AccommodationService(AccommodationRepository accommodationRepository) {
        this.accommodationRepository = accommodationRepository;
    }

    public AccommodationDTO saveAccommodation(AccommodationDTO accommodationDTO) {
        Accommodation.AccommodationType accommodationType = Accommodation.AccommodationType.valueOf(accommodationDTO.getAccommodationType());
        Accommodation.Status status = Accommodation.Status.valueOf(accommodationDTO.getStatus());

        Accommodation accommodation = convertToEntity(accommodationDTO, accommodationType, status);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        return convertToDTO(savedAccommodation);
    }

    public List<AccommodationDTO> getAllAccommodations() {
        List<Accommodation> accommodations = accommodationRepository.findAll();
        return accommodations.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public AccommodationDTO getAccommodationById(Long accommodationId) {
        Optional<Accommodation> accommodationOptional = accommodationRepository.findById(accommodationId);
        if (accommodationOptional.isPresent()) {
            return convertToDTO(accommodationOptional.get());
        } else {
            throw new RuntimeException("Accommodation not found with ID: " + accommodationId);
        }
    }

    public List<Accommodation> getAccommodationsByStatus(Accommodation.Status status) {
        return accommodationRepository.findByStatus(status);
    }

    public List<Accommodation> getAccommodationsByCity(String city) {
        return accommodationRepository.findByCity(city);
    }

    @Transactional
    public void deleteAccommodation(Long accommodationId) {
        if (!accommodationRepository.existsById(accommodationId)) {
            throw new RuntimeException("Accommodation not found with ID: " + accommodationId);
        }
        accommodationRepository.deleteById(accommodationId);
    }

    private Accommodation convertToEntity(AccommodationDTO accommodationDTO, Accommodation.AccommodationType accommodationType, Accommodation.Status status) {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationDTO.getId());
        accommodation.setAccommodationType(accommodationType);
        accommodation.setName(accommodationDTO.getName());
        accommodation.setCity(accommodationDTO.getCity());
        accommodation.setAddress(accommodationDTO.getAddress());
        accommodation.setPricePerNight(accommodationDTO.getPricePerNight());
        accommodation.setRating(accommodationDTO.getRating());

        if (accommodationDTO.getRoom() != null) {
            accommodation.setRoom(Integer.parseInt(accommodationDTO.getRoom()));
        }

        accommodation.setStatus(status);
        accommodation.setCreatedAt(LocalDateTime.now());
        return accommodation;
    }

    private AccommodationDTO convertToDTO(Accommodation accommodation) {
        AccommodationDTO accommodationDTO = new AccommodationDTO();
        accommodationDTO.setId(accommodation.getId());
        accommodationDTO.setAccommodationType(accommodation.getAccommodationType().name());
        accommodationDTO.setName(accommodation.getName());
        accommodationDTO.setCity(accommodation.getCity());
        accommodationDTO.setAddress(accommodation.getAddress());
        accommodationDTO.setPricePerNight(accommodation.getPricePerNight());
        accommodationDTO.setRating(accommodation.getRating());
        accommodationDTO.setRoom(String.valueOf(accommodation.getRoom()));
        accommodationDTO.setStatus(accommodation.getStatus().name());
        accommodationDTO.setCreatedAt(accommodation.getCreatedAt());
        return accommodationDTO;
    }

}