package project.planora_travelandbooking_system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.Model.Accommodation;
import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    List<Accommodation> findByStatus(Accommodation.Status status);

    List<Accommodation> findByCity(String city);

}