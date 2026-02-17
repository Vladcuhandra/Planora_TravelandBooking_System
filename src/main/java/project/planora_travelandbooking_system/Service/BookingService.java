package project.planora_travelandbooking_system.Service;

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
import java.util.List;

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

    public List<BookingDTO> getBookings(String email, boolean isAdmin) {
        List<Booking> bookings = isAdmin
                ? bookingRepository.findAll()
                : bookingRepository.findByTripUserEmail(email);

        return bookings.stream().map(this::toDTO).toList();
    }

    public Booking getBookingEntity(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
    }

    @Transactional
    public void saveBooking(BookingDTO bookingDTO, String email, boolean isAdmin) {
        Trip trip = tripRepository.findById(bookingDTO.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + bookingDTO.getTripId()));

        if (!isAdmin) {
            if (trip.getUser() == null || trip.getUser().getEmail() == null) {
                throw new RuntimeException("Access denied");
            }
            if (!trip.getUser().getEmail().equals(email)) {
                throw new RuntimeException("Access denied");
            }
        }

        Booking booking = new Booking();
        booking.setTrip(trip);

        Booking.BookingType bookingType = Booking.BookingType.valueOf(bookingDTO.getBookingType());
        Booking.BookingStatus status = Booking.BookingStatus.valueOf(bookingDTO.getStatus());

        booking.setBookingType(bookingType);
        booking.setStatus(status);
        booking.setStartDate(bookingDTO.getStartDate());
        booking.setEndDate(bookingDTO.getEndDate());

        applyTypeAndPrice(booking, bookingDTO, bookingType);

        booking.setCreatedAt(LocalDateTime.now());
        bookingRepository.save(booking);
    }

    @Transactional
    public void updateBooking(Long id, BookingDTO bookingDTO, String email, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        if (!isAdmin) {
            if (booking.getTrip() == null || booking.getTrip().getUser() == null) {
                throw new RuntimeException("Access denied");
            }
            if (!email.equals(booking.getTrip().getUser().getEmail())) {
                throw new RuntimeException("Access denied");
            }
        }

        Trip trip = tripRepository.findById(bookingDTO.getTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found with ID: " + bookingDTO.getTripId()));

        if (!isAdmin) {
            if (trip.getUser() == null || !email.equals(trip.getUser().getEmail())) {
                throw new RuntimeException("Access denied");
            }
        }

        booking.setTrip(trip);

        Booking.BookingType bookingType = Booking.BookingType.valueOf(bookingDTO.getBookingType());
        Booking.BookingStatus status = Booking.BookingStatus.valueOf(bookingDTO.getStatus());

        booking.setBookingType(bookingType);
        booking.setStatus(status);
        booking.setStartDate(bookingDTO.getStartDate());
        booking.setEndDate(bookingDTO.getEndDate());

        applyTypeAndPrice(booking, bookingDTO, bookingType);

        bookingRepository.save(booking);
    }

    @Transactional
    public void deleteBooking(Long id, String email, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));

        if (!isAdmin) {
            if (booking.getTrip() == null || booking.getTrip().getUser() == null) {
                throw new RuntimeException("Access denied");
            }
            if (!email.equals(booking.getTrip().getUser().getEmail())) {
                throw new RuntimeException("Access denied");
            }
        }

        bookingRepository.deleteById(id);
    }

    private void applyTypeAndPrice(Booking booking, BookingDTO bookingDTO, Booking.BookingType bookingType) {
        if (bookingType == Booking.BookingType.TRANSPORT) {

            if (bookingDTO.getTransportId() == null) {
                throw new RuntimeException("Transport must be selected for booking");
            }

            Transport transport = transportRepository.findById(bookingDTO.getTransportId())
                    .orElseThrow(() -> new RuntimeException("Transport not found: " + bookingDTO.getTransportId()));

            booking.setTransport(transport);
            booking.setAccommodation(null);
            booking.setTotalPrice(transport.getPrice());

        } else { 

            if (bookingDTO.getAccommodationId() == null) {
                throw new RuntimeException("Accommodation must be selected for booking");
            }

            Accommodation accommodation = accommodationRepository.findById(bookingDTO.getAccommodationId())
                    .orElseThrow(() -> new RuntimeException("Accommodation not found: " + bookingDTO.getAccommodationId()));

            booking.setAccommodation(accommodation);
            booking.setTransport(null);

            long nights = calcNights(bookingDTO.getStartDate(), bookingDTO.getEndDate());
            booking.setTotalPrice(accommodation.getPricePerNight() * nights);
        }
    }

    private long calcNights(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 1;
        long hours = Duration.between(start, end).toHours();
        long nights = hours / 24;
        if (nights <= 0) nights = 1;
        return nights;
    }

    private BookingDTO toDTO(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setTripId(booking.getTrip() != null ? booking.getTrip().getId() : null);
        bookingDTO.setBookingType(booking.getBookingType() != null ? booking.getBookingType().name() : null);
        bookingDTO.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        bookingDTO.setStartDate(booking.getStartDate());
        bookingDTO.setEndDate(booking.getEndDate());
        bookingDTO.setCreatedAt(booking.getCreatedAt());
        bookingDTO.setTotalPrice(booking.getTotalPrice());

        if (booking.getTransport() != null) bookingDTO.setTransportId(booking.getTransport().getId());
        if (booking.getAccommodation() != null) bookingDTO.setAccommodationId(booking.getAccommodation().getId());

        return bookingDTO;
    }
}
