package project.planora_travelandbooking_system.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.models.User;
import project.planora_travelandbooking_system.reposiitory.UserRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCleanupService {

    private final UserRepository userRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")
    public void permanentlyDeleteOldUsers() {

        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);

        List<User> users =
                userRepository.findAllByDeletedTrueAndDeletionDateBefore(cutoff);

        userRepository.deleteAll(users);
    }
}
