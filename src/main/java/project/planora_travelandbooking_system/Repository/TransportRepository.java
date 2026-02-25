package project.planora_travelandbooking_system.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.Model.Transport;
import java.util.List;

@Repository
public interface TransportRepository extends JpaRepository<Transport, Long>{

    Page<Transport> findAll(Pageable pageable);

}