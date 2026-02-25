package project.planora_travelandbooking_system.Repository;

import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Model.UserEmailHistory;

import java.util.List;

@Repository
public interface UserEmailHistoryRepository extends
        JpaRepository<UserEmailHistory, Long>, JpaSpecificationExecutor<UserEmailHistory> {

    List<UserEmailHistory> findByUser(User user);

    boolean existsByEmail(String email);

}