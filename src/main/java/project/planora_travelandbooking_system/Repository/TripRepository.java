package project.planora_travelandbooking_system.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.Model.Trip;
import project.planora_travelandbooking_system.Model.User;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByUserEmail(String email);

    Page<Trip> findByUserEmail(String email, Pageable pageable);

    Page<Trip> findByUser(User user, PageRequest pageRequest);
}
