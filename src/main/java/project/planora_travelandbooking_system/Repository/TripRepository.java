package project.planora_travelandbooking_system.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.Model.Trip;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    Page<Trip> findByUserEmail(String email, Pageable pageable);

    // user sees only own trips
    List<Trip> findByUserEmail(String email);
}
