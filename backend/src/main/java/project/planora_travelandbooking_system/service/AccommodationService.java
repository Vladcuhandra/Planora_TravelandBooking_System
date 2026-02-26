package project.planora_travelandbooking_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.dto.AccommodationDTO;
import project.planora_travelandbooking_system.model.Accommodation;
import project.planora_travelandbooking_system.repository.AccommodationRepository;
import java.time.LocalDateTime;

@Service
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;

    @Autowired
    public AccommodationService(AccommodationRepository accommodationRepository) {
        this.accommodationRepository = accommodationRepository;
    }

    public AccommodationDTO saveAccommodation(AccommodationDTO accommodationDTO) {
        DateValidation.endNotBeforeStart(
                accommodationDTO.getStartTime(),
                accommodationDTO.getEndTime(),
                "startTime",
                "endTime"
        );

        Accommodation.AccommodationType accommodationType = Accommodation.AccommodationType.valueOf(accommodationDTO.getAccommodationType());
        Accommodation.Status status = Accommodation.Status.valueOf(accommodationDTO.getStatus());

        Accommodation accommodation = convertToEntity(accommodationDTO, accommodationType, status);
        Accommodation savedAccommodation = accommodationRepository.save(accommodation);

        return convertToDTO(savedAccommodation);
    }

    public Page<AccommodationDTO> getAllAccommodations(int page, int pageSize) {
        Page<Accommodation> accommodationPage = accommodationRepository.findAll(PageRequest.of(page, pageSize));
        return accommodationPage.map(this::convertToDTO);
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
        Accommodation.Status status = Accommodation.Status.valueOf(accommodationDTO.getStatus());

        accommodation.setAccommodationType(accommodationType);
        accommodation.setName(accommodationDTO.getName());
        accommodation.setCity(accommodationDTO.getCity());
        accommodation.setAddress(accommodationDTO.getAddress());
        accommodation.setStartTime(accommodationDTO.getStartTime());
        accommodation.setEndTime(accommodationDTO.getEndTime());
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
        accommodation.setStartTime(accommodationDTO.getStartTime());
        accommodation.setEndTime(accommodationDTO.getEndTime());
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
        accommodationDTO.setStartTime(accommodation.getStartTime());
        accommodationDTO.setEndTime(accommodation.getEndTime());
        accommodationDTO.setRating(accommodation.getRating());
        accommodationDTO.setRoom(accommodation.getRoom());
        accommodationDTO.setPricePerNight(accommodation.getPricePerNight());
        accommodationDTO.setStatus(accommodation.getStatus().name());
        accommodationDTO.setCreatedAt(accommodation.getCreatedAt());
        return accommodationDTO;
    }

    public AccommodationDTO saveAccommodationAndReturn(AccommodationDTO dto) {
        // DTO -> Entity
        Accommodation accommodation = new Accommodation();


        if (dto.getId() != null) {
            accommodation.setId(dto.getId());
        }

        accommodation.setName(dto.getName());
        accommodation.setCity(dto.getCity());
        accommodation.setAddress(dto.getAddress());
        accommodation.setStartTime(dto.getStartTime());
        accommodation.setEndTime(dto.getEndTime());

        accommodation.setPricePerNight(dto.getPricePerNight());
        accommodation.setRating(dto.getRating());
        accommodation.setRoom(dto.getRoom());

        accommodation.setCreatedAt(dto.getCreatedAt());

        // enums: DTO String -> Entity Enum
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            accommodation.setStatus(Accommodation.Status.valueOf(dto.getStatus()));
        } else {
            accommodation.setStatus(null);
        }

        if (dto.getAccommodationType() != null && !dto.getAccommodationType().isBlank()) {
            accommodation.setAccommodationType(
                    Accommodation.AccommodationType.valueOf(dto.getAccommodationType())
            );
        } else {
            accommodation.setAccommodationType(null);
        }

        Accommodation saved = accommodationRepository.save(accommodation);

        // Entity -> DTO (WITH ID)
        AccommodationDTO result = new AccommodationDTO();
        result.setId(saved.getId());

        result.setName(saved.getName());
        result.setCity(saved.getCity());
        result.setAddress(saved.getAddress());
        result.setStartTime(saved.getStartTime());
        result.setEndTime(saved.getEndTime());

        result.setPricePerNight(saved.getPricePerNight());
        result.setRating(saved.getRating());
        result.setRoom(saved.getRoom());

        result.setCreatedAt(saved.getCreatedAt());

        // enums: Entity Enum -> DTO String
        result.setStatus(saved.getStatus() == null ? null : saved.getStatus().name());
        result.setAccommodationType(
                saved.getAccommodationType() == null ? null : saved.getAccommodationType().name()
        );

        return result;
    }

}