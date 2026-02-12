package project.planora_travelandbooking_system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.Model.Flight;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long>{

}