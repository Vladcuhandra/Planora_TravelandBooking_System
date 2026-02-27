package project.planora_travelandbooking_system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import project.planora_travelandbooking_system.model.User;
import project.planora_travelandbooking_system.repository.UserRepository;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCleanupServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserCleanupService userCleanupService;

    @Test
    void permanentlyDeleteOldUsers_fetchesDeletedBeforeCutoff_andDeletesAll() {
        when(userRepository.findAllByDeletedTrueAndDeletionDateBefore(any()))
                .thenReturn(List.of(new User(), new User()));

        userCleanupService.permanentlyDeleteOldUsers();

        verify(userRepository).findAllByDeletedTrueAndDeletionDateBefore(any());
        verify(userRepository).deleteAll(anyList());
    }
}