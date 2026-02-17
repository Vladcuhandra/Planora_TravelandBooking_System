package project.planora_travelandbooking_system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.Model.Booking;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatus(Booking.BookingStatus status);

    List<Booking> findByTripId(Long tripId);

    List<Booking> findByTransportId(Long transportId);

    List<Booking> findByAccommodationId(Long accommodationId);

    // USER sees only bookings where trip.user.email = current user
    List<Booking> findByTripUserEmail(String email);
}
