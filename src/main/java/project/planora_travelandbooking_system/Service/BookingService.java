package project.planora_travelandbooking_system.Service;

import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final TransportRepository transportRepository;
    private final AccommodationRepository accommodationRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          TripRepository tripRepository,
                          TransportRepository transportRepository,
                          AccommodationRepository accommodationRepository) {
        this.bookingRepository = bookingRepository;
        this.tripRepository = tripRepository;
        this.transportRepository = transportRepository;
        this.accommodationRepository = accommodationRepository;
    }

    public BookingDTO saveBooking(BookingDTO bookingDTO) {
        Booking.BookingType bookingType = Booking.BookingType.valueOf(bookingDTO.getBookingType());
        Booking.BookingStatus status = Booking.BookingStatus.valueOf(bookingDTO.getStatus());

        Booking booking = convertToEntity(bookingDTO, bookingType, status);
        Booking savedBooking = bookingRepository.save(booking);

        return convertToDTO(savedBooking);
    }

    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public BookingDTO getBookingById(Long bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);
        if (bookingOptional.isPresent()) {
            return convertToDTO(bookingOptional.get());
        } else {
            throw new RuntimeException("Booking not found with ID: " + bookingId);
        }
    }

    @Transactional
    public void deleteBooking(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new RuntimeException("Booking not found with ID: " + bookingId);
        }
        bookingRepository.deleteById(bookingId);
    }

    private Booking convertToEntity(BookingDTO bookingDTO, Booking.BookingType bookingType, Booking.BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(bookingDTO.getId());
        booking.setBookingType(bookingType);
        booking.setStatus(status);
        booking.setStartDate(bookingDTO.getStartDate());
        booking.setEndDate(bookingDTO.getEndDate());
        booking.setTotalPrice(bookingDTO.getTotalPrice());

        Optional<Trip> trip = tripRepository.findById(bookingDTO.getTripId());
        trip.ifPresent(booking::setTrip);

        if (bookingDTO.getTransportId() != null) {
            Optional<Transport> transport = transportRepository.findById(bookingDTO.getTransportId());
            transport.ifPresent(booking::setTransport);
        }

        if (bookingDTO.getAccommodationId() != null) {
            Optional<Accommodation> accommodation =
                    accommodationRepository.findById(bookingDTO.getAccommodationId());
            accommodation.ifPresent(booking::setAccommodation);
        }

        booking.setCreatedAt(LocalDateTime.now());
        return booking;
    }

    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setBookingType(booking.getBookingType().name());
        bookingDTO.setStatus(booking.getStatus().name());
        bookingDTO.setStartDate(booking.getStartDate());
        bookingDTO.setEndDate(booking.getEndDate());
        bookingDTO.setTotalPrice(booking.getTotalPrice());
        bookingDTO.setCreatedAt(booking.getCreatedAt());
        bookingDTO.setTripId(booking.getTrip().getId());

        if (booking.getTransport() != null) {
            bookingDTO.setTransportId(booking.getTransport().getId());
        }
        if (booking.getAccommodation() != null) {
            bookingDTO.setAccommodationId(booking.getAccommodation().getId());
        }

        return bookingDTO;
    }

}