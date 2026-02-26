package project.planora_travelandbooking_system.reposiitory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.models.Booking;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatus(Booking.BookingStatus status);

    List<Booking> findByTripId(Long tripId);

    List<Booking> findByTransportId(Long transportId);

    List<Booking> findByAccommodationId(Long accommodationId);

    Page<Booking> findByTripUserEmail(String email, Pageable pageable);

    Page<Booking> findByBookingType(String bookingType, Pageable pageable);

    Page<Booking> findByTripUserEmailAndBookingType(String email, String bookingType, Pageable pageable);

    // ACTIVE = status != CANCELLED (global uniqueness across all trips)
    boolean existsByTransportIdAndStatusNot(Long transportId, Booking.BookingStatus status);
    boolean existsByAccommodationIdAndStatusNot(Long accommodationId, Booking.BookingStatus status);

    boolean existsByTransportIdAndStatusNotAndIdNot(Long transportId, Booking.BookingStatus status, Long id);
    boolean existsByAccommodationIdAndStatusNotAndIdNot(Long accommodationId, Booking.BookingStatus status, Long id);
    boolean existsByTripId(Long tripId);
}