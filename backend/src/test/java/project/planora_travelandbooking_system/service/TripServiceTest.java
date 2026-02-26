package project.planora_travelandbooking_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.planora_travelandbooking_system.dto.TripDTO;
import project.planora_travelandbooking_system.model.Trip;
import project.planora_travelandbooking_system.model.User;
import project.planora_travelandbooking_system.repository.BookingRepository;
import project.planora_travelandbooking_system.repository.TripRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock private TripRepository tripRepository;
    @Mock private UserService userService;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks private TripService tripService;

    @Captor ArgumentCaptor<Trip> tripCaptor;

    private User owner;
    private User admin;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(10L);
        owner.setEmail("user@test.com");
        owner.setRole(User.Role.USER);

        admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setRole(User.Role.ADMIN);
    }

    @Test
    void saveTrip_endBeforeStart_throwsIllegalArgument() {
        TripDTO dto = new TripDTO();
        dto.setUserId(owner.getId());
        dto.setTitle("Trip");
        dto.setStartDate(LocalDateTime.of(2026, 1, 10, 10, 0));
        dto.setEndDate(LocalDateTime.of(2026, 1, 9, 10, 0));

        assertThatThrownBy(() -> tripService.saveTrip(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endDate cannot be before startDate");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void saveTrip_userMustExist() {
        TripDTO dto = new TripDTO();
        dto.setUserId(999L);
        dto.setTitle("Trip");
        dto.setStartDate(LocalDateTime.of(2026, 1, 10, 10, 0));
        dto.setEndDate(LocalDateTime.of(2026, 1, 11, 10, 0));

        when(userService.getUserId(999L)).thenReturn(null);

        assertThatThrownBy(() -> tripService.saveTrip(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with ID: 999");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void saveTrip_happyPath_setsFieldsAndCreatedAt() {
        TripDTO dto = new TripDTO();
        dto.setUserId(owner.getId());
        dto.setTitle("My trip");
        dto.setDescription("Desc");
        dto.setStartDate(LocalDateTime.of(2026, 1, 10, 10, 0));
        dto.setEndDate(LocalDateTime.of(2026, 1, 11, 10, 0));

        when(userService.getUserId(owner.getId())).thenReturn(owner);
        when(tripRepository.save(any(Trip.class))).thenAnswer(inv -> {
            Trip t = inv.getArgument(0);
            t.setId(123L);
            return t;
        });

        TripDTO saved = tripService.saveTrip(dto);

        verify(tripRepository).save(tripCaptor.capture());
        Trip entity = tripCaptor.getValue();

        assertThat(entity.getTitle()).isEqualTo("My trip");
        assertThat(entity.getDescription()).isEqualTo("Desc");
        assertThat(entity.getUser()).isSameAs(owner);
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(saved.getId()).isEqualTo(123L);
        assertThat(saved.getUserId()).isEqualTo(owner.getId());
    }

    @Test
    void updateTrip_nonAdminCannotUpdateOthersTrip_accessDenied() {
        Trip existing = new Trip();
        existing.setId(200L);
        existing.setUser(admin);

        TripDTO dto = new TripDTO();
        dto.setTitle("New");
        dto.setDescription("New");
        dto.setStartDate(LocalDateTime.of(2026, 1, 10, 10, 0));
        dto.setEndDate(LocalDateTime.of(2026, 1, 11, 10, 0));

        when(tripRepository.findById(200L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> tripService.updateTrip(200L, dto, owner))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied");

        verify(tripRepository, never()).save(any());
    }

    @Test
    void deleteTripAuthorized_whenBookingsExist_throws() {
        Trip trip = new Trip();
        trip.setId(300L);
        trip.setUser(owner);

        when(tripRepository.findById(300L)).thenReturn(Optional.of(trip));
        when(bookingRepository.existsByTripId(300L)).thenReturn(true);

        assertThatThrownBy(() -> tripService.deleteTripAuthorized(300L, owner.getEmail(), false))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot delete trip: it has bookings. Delete bookings first.");

        verify(tripRepository, never()).deleteById(anyLong());
    }

    @Test
    void bulkDeleteTrips_adminDeletes_uniqueIds_andChecksBookings() {
        Trip t1 = new Trip(); t1.setId(1L);
        Trip t2 = new Trip(); t2.setId(2L);
        Trip t3 = new Trip(); t3.setId(3L);

        List<Long> ids = Arrays.asList(1L, 2L, 2L, 3L);
        when(tripRepository.findAllById(ids)).thenReturn(List.of(t1, t2, t3));
        when(bookingRepository.existsByTripId(1L)).thenReturn(false);
        when(bookingRepository.existsByTripId(2L)).thenReturn(false);
        when(bookingRepository.existsByTripId(3L)).thenReturn(false);

        tripService.bulkDeleteTrips(ids, admin.getEmail(), true);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Set<Long>> setCaptor = ArgumentCaptor.forClass(java.util.Set.class);
        verify(tripRepository).deleteAllByIdInBatch(setCaptor.capture());
        assertThat(setCaptor.getValue()).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void bulkDeleteTrips_anyTripHasBookings_throws_andDoesNotDelete() {
        Trip t1 = new Trip(); t1.setId(1L);
        Trip t2 = new Trip(); t2.setId(2L);

        List<Long> ids = List.of(1L, 2L);
        when(tripRepository.findAllById(ids)).thenReturn(List.of(t1, t2));
        when(bookingRepository.existsByTripId(1L)).thenReturn(false);
        when(bookingRepository.existsByTripId(2L)).thenReturn(true);

        assertThatThrownBy(() -> tripService.bulkDeleteTrips(ids, admin.getEmail(), true))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cannot delete trip id 2: it has bookings.");

        verify(tripRepository, never()).deleteAllByIdInBatch(any());
    }
}