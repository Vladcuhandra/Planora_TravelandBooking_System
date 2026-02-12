package project.planora_travelandbooking_system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.Model.Booking;


@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

}