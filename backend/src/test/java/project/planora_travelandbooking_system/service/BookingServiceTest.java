package project.planora_travelandbooking_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.planora_travelandbooking_system.dto.BookingDTO;
import project.planora_travelandbooking_system.model.*;
import project.planora_travelandbooking_system.repository.AccommodationRepository;
import project.planora_travelandbooking_system.repository.BookingRepository;
import project.planora_travelandbooking_system.repository.TransportRepository;
import project.planora_travelandbooking_system.repository.TripRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private TripRepository tripRepository;
    @Mock private TransportRepository transportRepository;
    @Mock private AccommodationRepository accommodationRepository;

    @InjectMocks private BookingService bookingService;

    @Captor ArgumentCaptor<Booking> bookingCaptor;

    private User owner;
    private Trip ownerTrip;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setId(10L);
        owner.setEmail("user@test.com");
        owner.setRole(User.Role.USER);

        ownerTrip = new Trip();
        ownerTrip.setId(100L);
        ownerTrip.setUser(owner);
    }

    @Test
    void saveBooking_userCannotBookOtherUsersTrip_accessDenied() {
        User other = new User();
        other.setId(11L);
        other.setEmail("other@test.com");

        Trip otherTrip = new Trip();
        otherTrip.setId(200L);
        otherTrip.setUser(other);

        BookingDTO dto = new BookingDTO();
        dto.setTripId(200L);
        dto.setBookingType("TRANSPORT");
        dto.setStatus("CONFIRMED");
        dto.setTransportId(1L);

        when(tripRepository.findById(200L)).thenReturn(Optional.of(otherTrip));

        assertThatThrownBy(() -> bookingService.saveBooking(dto, owner.getEmail(), false))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void saveBooking_transportAlreadyBooked_throwsHelpfulMessage() {
        BookingDTO dto = new BookingDTO();
        dto.setTripId(ownerTrip.getId());
        dto.setBookingType("TRANSPORT");
        dto.setStatus("CONFIRMED");
        dto.setTransportId(55L);

        when(tripRepository.findById(ownerTrip.getId())).thenReturn(Optional.of(ownerTrip));
        when(bookingRepository.existsByTransportIdAndStatusNot(eq(55L), eq(Booking.BookingStatus.CANCELLED)))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.saveBooking(dto, owner.getEmail(), false))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("This transport is already booked in another active booking");

        verify(bookingRepository, never()).save(any());
        verify(transportRepository, never()).findById(anyLong());
    }

    @Test
    void saveBooking_transportHappyPath_setsTransportAndPrice_saves() {
        Transport transport = new Transport();
        transport.setId(55L);
        transport.setPrice(123.45);

        BookingDTO dto = new BookingDTO();
        dto.setTripId(ownerTrip.getId());
        dto.setBookingType("TRANSPORT");
        dto.setStatus("CONFIRMED");
        dto.setTransportId(55L);

        when(tripRepository.findById(ownerTrip.getId())).thenReturn(Optional.of(ownerTrip));
        when(bookingRepository.existsByTransportIdAndStatusNot(eq(55L), eq(Booking.BookingStatus.CANCELLED)))
                .thenReturn(false);
        when(transportRepository.findById(55L)).thenReturn(Optional.of(transport));

        bookingService.saveBooking(dto, owner.getEmail(), false);

        verify(bookingRepository).save(bookingCaptor.capture());
        Booking saved = bookingCaptor.getValue();

        assertThat(saved.getTrip()).isSameAs(ownerTrip);
        assertThat(saved.getBookingType()).isEqualTo(Booking.BookingType.TRANSPORT);
        assertThat(saved.getStatus()).isEqualTo(Booking.BookingStatus.CONFIRMED);
        assertThat(saved.getTransport()).isSameAs(transport);
        assertThat(saved.getAccommodation()).isNull();
        assertThat(saved.getTotalPrice()).isEqualTo(123.45);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void updateBooking_transportAlreadyBookedExcludingSelf_throwsHelpfulMessage() {
        Booking existing = new Booking();
        existing.setId(999L);
        existing.setTrip(ownerTrip);

        BookingDTO dto = new BookingDTO();
        dto.setTripId(ownerTrip.getId());
        dto.setBookingType("TRANSPORT");
        dto.setStatus("CONFIRMED");
        dto.setTransportId(55L);

        when(bookingRepository.findById(999L)).thenReturn(Optional.of(existing));
        when(tripRepository.findById(ownerTrip.getId())).thenReturn(Optional.of(ownerTrip));
        when(bookingRepository.existsByTransportIdAndStatusNotAndIdNot(eq(55L), eq(Booking.BookingStatus.CANCELLED), eq(999L)))
                .thenReturn(true);

        assertThatThrownBy(() -> bookingService.updateBooking(999L, dto, owner.getEmail(), false))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("This transport is already booked in another active booking");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void bulkDeleteBookings_userMustOwnAllBookings_orAccessDenied() {
        Booking b1 = new Booking();
        Trip t1 = new Trip();
        User u1 = new User();
        u1.setEmail(owner.getEmail());
        t1.setUser(u1);
        b1.setTrip(t1);

        Booking b2 = new Booking();
        Trip t2 = new Trip();
        User u2 = new User();
        u2.setEmail("someone@else.com");
        t2.setUser(u2);
        b2.setTrip(t2);

        List<Long> ids = Arrays.asList(1L, 2L, 2L);
        when(bookingRepository.findAllById(ids)).thenReturn(List.of(b1, b2));

        assertThatThrownBy(() -> bookingService.bulkDeleteBookings(ids, owner.getEmail(), false))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Access denied");

        verify(bookingRepository, never()).deleteAllByIdInBatch(any());
    }

    @Test
    void bulkDeleteBookings_adminDeletesInBatch_uniqueIds() {
        List<Long> ids = Arrays.asList(1L, 2L, 2L, 3L);
        when(bookingRepository.findAllById(ids)).thenReturn(List.of(new Booking(), new Booking(), new Booking()));

        bookingService.bulkDeleteBookings(ids, "admin@test.com", true);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Set<Long>> setCaptor = ArgumentCaptor.forClass(java.util.Set.class);
        verify(bookingRepository).deleteAllByIdInBatch(setCaptor.capture());
        assertThat(setCaptor.getValue()).containsExactlyInAnyOrder(1L, 2L, 3L);
    }
}