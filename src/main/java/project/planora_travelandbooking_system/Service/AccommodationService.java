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

    @Transactional
    public void deleteAccommodation(Long accommodationId) {
        if (!accommodationRepository.existsById(accommodationId)) {
            throw new RuntimeException("Accommodation not found with ID: " + accommodationId);
        }
        accommodationRepository.deleteById(accommodationId);
    }

    public List<Accommodation> getAccommodationsByStatus(Accommodation.Status status) {
        return accommodationRepository.findByStatus(status);
    }

    @Transactional
    public AccommodationDTO updateAccommodation(Long accommodationId, AccommodationDTO accommodationDTO) {

        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new RuntimeException("Accommodation not found with ID: " + accommodationId));

        Accommodation.AccommodationType accommodationType =
                Accommodation.AccommodationType.valueOf(accommodationDTO.getAccommodationType());
        Accommodation.Status status = Accommodation.Status.valueOf(accommodationDTO.getStatus());

        accommodation.setAccommodationType(accommodationType);
        accommodation.setName(accommodationDTO.getName());
        accommodation.setCity(accommodationDTO.getCity());
        accommodation.setAddress(accommodationDTO.getAddress());
        accommodation.setRating(accommodationDTO.getRating());
        accommodation.setRoom(accommodationDTO.getRoom());
        accommodation.setPricePerNight(accommodationDTO.getPricePerNight());
        accommodation.setStatus(status);

        Accommodation updated = accommodationRepository.save(accommodation);

        return convertToDTO(updated);
    }

    private Accommodation convertToEntity(AccommodationDTO accommodationDTO, Accommodation.AccommodationType accommodationType, Accommodation.Status status) {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(accommodationDTO.getId());
        accommodation.setAccommodationType(accommodationType);
        accommodation.setName(accommodationDTO.getName());
        accommodation.setCity(accommodationDTO.getCity());
        accommodation.setAddress(accommodationDTO.getAddress());
        accommodation.setRating(accommodationDTO.getRating());
        accommodation.setRoom(accommodationDTO.getRoom());
        accommodation.setPricePerNight(accommodationDTO.getPricePerNight());
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
        accommodationDTO.setRating(accommodation.getRating());
        accommodationDTO.setRoom(accommodation.getRoom());
        accommodationDTO.setPricePerNight(accommodation.getPricePerNight());
        accommodationDTO.setStatus(accommodation.getStatus().name());
        accommodationDTO.setCreatedAt(accommodation.getCreatedAt());
        return accommodationDTO;
    }
}
