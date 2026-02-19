package project.planora_travelandbooking_system.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.Model.JwtRefresher;

import java.util.Optional;

@Repository
public interface JwtRefresherRepository extends JpaRepository<JwtRefresher, Long> {

    Optional<JwtRefresher> findByTokenHashAndRevokedFalse(String hashedToken);

    Void deleteByUser_Id(Long userId);

}
