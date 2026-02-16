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
        Accommodation.AccommodationType accommodationType =
                Accommodation.AccommodationType.valueOf(accommodationDTO.getAccommodationType());
        Accommodation.Status status =
                Accommodation.Status.valueOf(accommodationDTO.getStatus());

        Accommodation accommodation = convertToEntity(accommodationDTO, accommodationType, status);

        // set createdAt only on create
        if (accommodation.getId() == null) {
            accommodation.setCreatedAt(LocalDateTime.now());
        }

        Accommodation saved = accommodationRepository.save(accommodation);
        return convertToDTO(saved);
    }

    public List<AccommodationDTO> getAllAccommodations() {
        return accommodationRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AccommodationDTO getAccommodationById(Long accommodationId) {
        Optional<Accommodation> accommodationOptional = accommodationRepository.findById(accommodationId);
        if (accommodationOptional.isPresent()) {
            return convertToDTO(accommodationOptional.get());
        }
        throw new RuntimeException("Accommodation not found with ID: " + accommodationId);
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

    @Transactional
    public AccommodationDTO updateAccommodation(Long accommodationId, AccommodationDTO accommodationDTO) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new RuntimeException("Accommodation not found with ID: " + accommodationId));

        Accommodation.AccommodationType accommodationType =
                Accommodation.AccommodationType.valueOf(accommodationDTO.getAccommodationType());
        Accommodation.Status status =
                Accommodation.Status.valueOf(accommodationDTO.getStatus());

        accommodation.setAccommodationType(accommodationType);
        accommodation.setName(accommodationDTO.getName());
        accommodation.setCity(accommodationDTO.getCity());
        accommodation.setAddress(accommodationDTO.getAddress());
        accommodation.setPricePerNight(accommodationDTO.getPricePerNight());
        accommodation.setRating(accommodationDTO.getRating());
        accommodation.setStatus(status);

        // room is optional (DTO String -> Entity Integer)
        if (accommodationDTO.getRoom() != null && !accommodationDTO.getRoom().isBlank()) {
            accommodation.setRoom(Integer.parseInt(accommodationDTO.getRoom().trim()));
        } else {
            accommodation.setRoom(null);
        }

        Accommodation updated = accommodationRepository.save(accommodation);
        return convertToDTO(updated);
    }

    private Accommodation convertToEntity(AccommodationDTO dto,
                                          Accommodation.AccommodationType accommodationType,
                                          Accommodation.Status status) {

        Accommodation accommodation = new Accommodation();
        accommodation.setId(dto.getId());
        accommodation.setAccommodationType(accommodationType);
        accommodation.setName(dto.getName());
        accommodation.setCity(dto.getCity());
        accommodation.setAddress(dto.getAddress());
        accommodation.setPricePerNight(dto.getPricePerNight());
        accommodation.setRating(dto.getRating());
        accommodation.setStatus(status);

        if (dto.getRoom() != null && !dto.getRoom().isBlank()) {
            accommodation.setRoom(Integer.parseInt(dto.getRoom().trim()));
        } else {
            accommodation.setRoom(null);
        }

        // keep existing createdAt if DTO has it (edit form), otherwise set on saveAccommodation()
        accommodation.setCreatedAt(dto.getCreatedAt());

        return accommodation;
    }

    private AccommodationDTO convertToDTO(Accommodation accommodation) {
        AccommodationDTO dto = new AccommodationDTO();
        dto.setId(accommodation.getId());
        dto.setAccommodationType(accommodation.getAccommodationType().name());
        dto.setName(accommodation.getName());
        dto.setCity(accommodation.getCity());
        dto.setAddress(accommodation.getAddress());
        dto.setPricePerNight(accommodation.getPricePerNight());
        dto.setRating(accommodation.getRating());

        if (accommodation.getRoom() != null) {
            dto.setRoom(String.valueOf(accommodation.getRoom()));
        } else {
            dto.setRoom("");
        }

        dto.setStatus(accommodation.getStatus().name());
        dto.setCreatedAt(accommodation.getCreatedAt());
        return dto;
    }
}
