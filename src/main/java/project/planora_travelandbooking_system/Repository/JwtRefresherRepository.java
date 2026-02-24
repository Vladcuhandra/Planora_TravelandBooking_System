package project.planora_travelandbooking_system.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.Model.JwtRefresher;

import java.util.Optional;

@Repository
public interface JwtRefresherRepository extends JpaRepository<JwtRefresher, Long> {

    Optional<JwtRefresher> findByTokenHashAndRevokedFalse(String hashedToken);

    Void deleteByUser_Id(Long userId);

    @Transactional
    @Modifying
    @Query("""
    update JwtRefresher r
       set r.revoked = true
     where r.user.id = :userId
       and r.revoked = false
""")
    int revokeAllActiveByUserId(@Param("userId") Long userId);

}
