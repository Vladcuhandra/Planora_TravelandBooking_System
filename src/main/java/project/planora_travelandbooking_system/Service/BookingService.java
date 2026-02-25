package project.planora_travelandbooking_system.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.DTO.BookingDTO;
import project.planora_travelandbooking_system.Model.Accommodation;
import project.planora_travelandbooking_system.Model.Booking;
import project.planora_travelandbooking_system.Model.Transport;
import project.planora_travelandbooking_system.Model.Trip;
import project.planora_travelandbooking_system.Repository.AccommodationRepository;
import project.planora_travelandbooking_system.Repository.BookingRepository;
import project.planora_travelandbooking_system.Repository.TransportRepository;
import project.planora_travelandbooking_system.Repository.TripRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final TransportRepository transportRepository;
    private final AccommodationRepository accommodationRepository;

    public BookingService(BookingRepository bookingRepository,
                          TripRepository tripRepository,
                          TransportRepository transportRepository,
                          AccommodationRepository accommodationRepository) {
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.transportRepository = transportRepository;
        this.accommodationRepository = accommodationRepository;
    }

    public Page<BookingDTO> getAllBookings(int page, int pageSize, String email, boolean isAdmin) {
        Page<Booking> bookingPage = isAdmin
                ? bookingRepository.findAll(PageRequest.of(page, pageSize))
                : bookingRepository.findByTripUserEmail(email, PageRequest.of(page, pageSize));
        return bookingPage.map(this::convertToDTO);
    }

    @Transactional
    public void saveBooking(BookingDTO bookingDTO, String email, boolean isAdmin) {

        Trip trip = tripRepository.findById(bookingDTO.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + bookingDTO.getTripId()));

        if (!isAdmin) {
            if (trip.getUser() == null || trip.getUser().getEmail() == null) throw new RuntimeException("Access denied");
            if (!trip.getUser().getEmail().equals(email)) throw new RuntimeException("Access denied");
        }

        Booking booking = new Booking();
        booking.setTrip(trip);

        Booking.BookingType bookingType = Booking.BookingType.valueOf(bookingDTO.getBookingType());
        Booking.BookingStatus status = Booking.BookingStatus.valueOf(bookingDTO.getStatus());

        booking.setBookingType(bookingType);
        booking.setStatus(status);

        applyTypeAndDatesAndPrice(booking, bookingDTO, bookingType, null);


        booking.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Transactional
    public void updateBooking(Long id, BookingDTO bookingDTO, String email, boolean isAdmin) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        if (!isAdmin) {
            if (booking.getTrip() == null || booking.getTrip().getUser() == null) throw new RuntimeException("Access denied");
            if (!email.equals(booking.getTrip().getUser().getEmail())) throw new RuntimeException("Access denied");
        }

        Trip trip = tripRepository.findById(bookingDTO.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + bookingDTO.getTripId()));

        if (!isAdmin) {
            if (trip.getUser() == null || !email.equals(trip.getUser().getEmail())) throw new RuntimeException("Access denied");
        }

        booking.setTrip(trip);

        Booking.BookingType bookingType = Booking.BookingType.valueOf(bookingDTO.getBookingType());
        Booking.BookingStatus status = Booking.BookingStatus.valueOf(bookingDTO.getStatus());

        booking.setBookingType(bookingType);
        booking.setStatus(status);

        applyTypeAndDatesAndPrice(booking, bookingDTO, bookingType, booking.getId());


        bookingRepository.save(booking);
    }

    @Transactional
    public void deleteBooking(Long id, String email, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        if (!isAdmin) {
            if (booking.getTrip() == null || booking.getTrip().getUser() == null) throw new RuntimeException("Access denied");
            if (!email.equals(booking.getTrip().getUser().getEmail())) throw new RuntimeException("Access denied");
        }

        bookingRepository.deleteById(id);
    }

    @Transactional
    public void bulkDeleteBookings(List<Long> ids, String email, boolean isAdmin) {
        if (ids == null || ids.isEmpty()) return;

        List<Booking> bookings = bookingRepository.findAllById(ids);

        if (!isAdmin) {
            for (Booking b : bookings) {
                if (b.getTrip() == null || b.getTrip().getUser() == null) throw new RuntimeException("Access denied");
                if (!email.equals(b.getTrip().getUser().getEmail())) throw new RuntimeException("Access denied");
            }
        }

        Set<Long> uniqueIds = new HashSet<>(ids);
        bookingRepository.deleteAllByIdInBatch(uniqueIds);
    }

    private void applyTypeAndDatesAndPrice(Booking booking,
                                           BookingDTO bookingDTO,
                                           Booking.BookingType bookingType,
                                           Long currentBookingIdOrNull) {

        Booking.BookingStatus cancelled = Booking.BookingStatus.CANCELLED;

        if (bookingType == Booking.BookingType.TRANSPORT) {

            if (bookingDTO.getTransportId() == null) {
                throw new RuntimeException("Transport must be selected for booking");
            }

            Long transportId = bookingDTO.getTransportId();

            boolean alreadyBooked = (currentBookingIdOrNull == null)
                    ? bookingRepository.existsByTransportIdAndStatusNot(transportId, cancelled)
                    : bookingRepository.existsByTransportIdAndStatusNotAndIdNot(transportId, cancelled, currentBookingIdOrNull);

            if (alreadyBooked) {
                throw new RuntimeException("This transport is already booked in another active booking");
            }

            Transport transport = transportRepository.findById(transportId)
                    .orElseThrow(() -> new RuntimeException("Transport not found: " + transportId));

            booking.setTransport(transport);
            booking.setAccommodation(null);

            booking.setTotalPrice(transport.getPrice());

        } else {

            if (bookingDTO.getAccommodationId() == null) {
                throw new RuntimeException("Accommodation must be selected for booking");
            }

            Long accommodationId = bookingDTO.getAccommodationId();

            boolean alreadyBooked = (currentBookingIdOrNull == null)
                    ? bookingRepository.existsByAccommodationIdAndStatusNot(accommodationId, cancelled)
                    : bookingRepository.existsByAccommodationIdAndStatusNotAndIdNot(accommodationId, cancelled, currentBookingIdOrNull);

            if (alreadyBooked) {
                throw new RuntimeException("This accommodation is already booked in another active booking");
            }

            Accommodation accommodation = accommodationRepository.findById(accommodationId)
                    .orElseThrow(() -> new RuntimeException("Accommodation not found: " + accommodationId));

            booking.setAccommodation(accommodation);
            booking.setTransport(null);

            // AUTO DATES (Accommodation has no dates in your model → use Trip dates)
            if (booking.getTrip() == null) throw new RuntimeException("Trip is required for accommodation booking");

        }
    }

    private long calcNights(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 1;
        long hours = Duration.between(start, end).toHours();
        long nights = hours / 24;
        if (nights <= 0) nights = 1;
        return nights;
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setTripId(booking.getTrip() != null ? booking.getTrip().getId() : null);
        bookingDTO.setBookingType(booking.getBookingType() != null ? booking.getBookingType().name() : null);
        bookingDTO.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        bookingDTO.setCreatedAt(booking.getCreatedAt());
        bookingDTO.setTotalPrice(booking.getTotalPrice());

        if (booking.getTransport() != null) bookingDTO.setTransportId(booking.getTransport().getId());
        if (booking.getAccommodation() != null) bookingDTO.setAccommodationId(booking.getAccommodation().getId());

        return bookingDTO;
    }
}