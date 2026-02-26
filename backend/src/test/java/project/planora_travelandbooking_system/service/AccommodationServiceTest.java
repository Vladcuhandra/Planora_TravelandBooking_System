package project.planora_travelandbooking_system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import project.planora_travelandbooking_system.dto.AccommodationDTO;
import project.planora_travelandbooking_system.model.Accommodation;
import project.planora_travelandbooking_system.repository.AccommodationRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccommodationServiceTest {

    @Mock private AccommodationRepository accommodationRepository;
    @InjectMocks private AccommodationService accommodationService;

    @Captor ArgumentCaptor<Accommodation> accCaptor;

    @Test
    void saveAccommodation_endBeforeStart_throws() {
        AccommodationDTO dto = new AccommodationDTO();
        dto.setStartTime(LocalDateTime.of(2026, 1, 10, 10, 0));
        dto.setEndTime(LocalDateTime.of(2026, 1, 9, 10, 0));

        dto.setAccommodationType("HOTEL");
        dto.setStatus("AVAILABLE");

        assertThatThrownBy(() -> accommodationService.saveAccommodation(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endTime cannot be before startTime");

        verify(accommodationRepository, never()).save(any());
    }

    @Test
    void saveAccommodation_happyPath_mapsEnums_andSetsCreatedAt() {
        AccommodationDTO dto = new AccommodationDTO();
        dto.setAccommodationType("HOTEL");
        dto.setStatus("AVAILABLE");
        dto.setName("Hilton");
        dto.setCity("Riga");
        dto.setAddress("Street 1");
        dto.setStartTime(LocalDateTime.of(2026, 1, 10, 10, 0));
        dto.setEndTime(LocalDateTime.of(2026, 1, 11, 10, 0));
        dto.setPricePerNight(100.0);
        dto.setRating(5.0);
        dto.setRoom(101);

        when(accommodationRepository.save(any())).thenAnswer(inv -> {
            Accommodation a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });

        AccommodationDTO result = accommodationService.saveAccommodation(dto);

        verify(accommodationRepository).save(accCaptor.capture());
        Accommodation saved = accCaptor.getValue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getAccommodationType()).isEqualTo(Accommodation.AccommodationType.HOTEL);
        assertThat(saved.getStatus()).isEqualTo(Accommodation.Status.AVAILABLE);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getAccommodationType()).isEqualTo("HOTEL");
        assertThat(result.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void deleteAccommodation_notFound_throws() {
        when(accommodationRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> accommodationService.deleteAccommodation(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Accommodation not found with ID: 10");

        verify(accommodationRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteAccommodation_found_deletes() {
        when(accommodationRepository.existsById(10L)).thenReturn(true);

        accommodationService.deleteAccommodation(10L);

        verify(accommodationRepository).deleteById(10L);
    }

    @Test
    void updateAccommodation_notFound_throws() {
        when(accommodationRepository.findById(1L)).thenReturn(Optional.empty());

        AccommodationDTO dto = new AccommodationDTO();
        dto.setAccommodationType("HOTEL");
        dto.setStatus("AVAILABLE");

        assertThatThrownBy(() -> accommodationService.updateAccommodation(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Accommodation not found with ID: 1");
    }

    @Test
    void saveAccommodationAndReturn_supportsNullEnums() {
        AccommodationDTO dto = new AccommodationDTO();
        dto.setName("Name");
        dto.setStatus(" "); // blank -> null
        dto.setAccommodationType(null);

        when(accommodationRepository.save(any())).thenAnswer(inv -> {
            Accommodation a = inv.getArgument(0);
            a.setId(123L);
            return a;
        });

        AccommodationDTO result = accommodationService.saveAccommodationAndReturn(dto);

        assertThat(result.getId()).isEqualTo(123L);
        assertThat(result.getStatus()).isNull();
        assertThat(result.getAccommodationType()).isNull();
    }
}