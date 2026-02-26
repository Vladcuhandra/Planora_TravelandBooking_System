package project.planora_travelandbooking_system.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.planora_travelandbooking_system.dto.UserDTO;
import project.planora_travelandbooking_system.dto.UserProfileUpdateRequest;
import project.planora_travelandbooking_system.exception.InvalidPasswordException;
import project.planora_travelandbooking_system.exception.UserNotFoundException;
import project.planora_travelandbooking_system.model.User;
import project.planora_travelandbooking_system.model.UserEmailHistory;
import project.planora_travelandbooking_system.repository.UserEmailHistoryRepository;
import project.planora_travelandbooking_system.repository.UserRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserEmailHistoryRepository userEmailHistoryRepository;

    @InjectMocks private UserService userService;

    @Captor ArgumentCaptor<User> userCaptor;

    @BeforeEach
    void init() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    private void setAuth(String email, String... roles) {
        var authorities = java.util.Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toList();

        var auth = new UsernamePasswordAuthenticationToken(email, "pw", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void getCurrentAuthenticatedUser_noAuth_throws() {
        assertThatThrownBy(() -> userService.getCurrentAuthenticatedUser())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No authenticated user found");
    }

    @Test
    void getCurrentAuthenticatedUser_userNotFound_throws() {
        setAuth("user@test.com", "USER");
        when(userRepository.findByEmailAndDeletedFalse("user@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentAuthenticatedUser())
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void getCurrentAuthenticatedUser_ok() {
        setAuth("user@test.com", "USER");
        User u = new User();
        u.setId(1L);
        u.setEmail("user@test.com");
        u.setRole(User.Role.USER);

        when(userRepository.findByEmailAndDeletedFalse("user@test.com")).thenReturn(Optional.of(u));

        User result = userService.getCurrentAuthenticatedUser();
        assertThat(result.getEmail()).isEqualTo("user@test.com");
    }

    @Test
    void saveUser_newUser_encodesPassword_setsCreatedAt_andSuperAdminFalse() {
        UserDTO dto = new UserDTO();
        dto.setEmail("new@test.com");
        dto.setRole("USER");
        dto.setPassword("plain");

        when(passwordEncoder.encode("plain")).thenReturn("ENC");
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userEmailHistoryRepository.existsByEmail("new@test.com")).thenReturn(false);

        userService.saveUser(dto);

        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getEmail()).isEqualTo("new@test.com");
        assertThat(saved.getRole()).isEqualTo(User.Role.USER);
        assertThat(saved.getPassword()).isEqualTo("ENC");
        assertThat(saved.isSuperAdmin()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void saveUser_emailAlreadyInUse_throws() {
        UserDTO dto = new UserDTO();
        dto.setEmail("taken@test.com");

        when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.saveUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already in use or in history");

        verify(userRepository, never()).save(any());
    }

    @Test
    void saveUser_updateExisting_superAdminCannotBeModified() {
        User existing = new User();
        existing.setId(5L);
        existing.setEmail("sa@test.com");
        existing.setSuperAdmin(true);

        UserDTO dto = new UserDTO();
        dto.setId(5L);
        dto.setEmail("newmail@test.com");

        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.saveUser(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Super admin cannot be modified.");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfile_notAdmin_notSelf_accessDenied() {
        User existing = new User();
        existing.setId(10L);
        existing.setEmail("victim@test.com");
        existing.setPassword("ENC");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));

        // principal != victim and no admin roles
        setAuth("attacker@test.com", "USER");

        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setEmail("new@test.com");

        assertThatThrownBy(() -> userService.updateProfile(10L, "attacker@test.com", req))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Unauthorized action.");
    }

    @Test
    void updateProfile_changePassword_self_missingCurrentPassword_throws() {
        User existing = new User();
        existing.setId(10L);
        existing.setEmail("me@test.com");
        existing.setPassword("ENC");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        setAuth("me@test.com", "USER");

        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setNewPassword("newPw");
        req.setCurrentPassword("   "); // blank

        assertThatThrownBy(() -> userService.updateProfile(10L, "me@test.com", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Current password is required.");
    }

    @Test
    void updateProfile_changePassword_self_wrongCurrentPassword_throwsInvalidPassword() {
        User existing = new User();
        existing.setId(10L);
        existing.setEmail("me@test.com");
        existing.setPassword("ENC");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        setAuth("me@test.com", "USER");

        when(passwordEncoder.matches("wrong", "ENC")).thenReturn(false);

        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setCurrentPassword("wrong");
        req.setNewPassword("newPw");

        assertThatThrownBy(() -> userService.updateProfile(10L, "me@test.com", req))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessage("Wrong current password");
    }

    @Test
    void updateProfile_changeEmail_emailAlreadyInUse_throws() {
        User existing = new User();
        existing.setId(10L);
        existing.setEmail("me@test.com");
        existing.setPassword("ENC");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        setAuth("me@test.com", "USER");

        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setEmail("taken@test.com");

        assertThatThrownBy(() -> userService.updateProfile(10L, "me@test.com", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The email is already in use.");
    }

    @Test
    void updateProfile_changeEmail_savesEmailHistory_andUpdatesEmail() {
        User existing = new User();
        existing.setId(10L);
        existing.setEmail("old@test.com");
        existing.setPassword("ENC");
        existing.setRole(User.Role.USER); // ✅ FIX: role must not be null

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        setAuth("old@test.com", "USER");

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userEmailHistoryRepository.existsByEmail("new@test.com")).thenReturn(false);

        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setEmail("new@test.com");

        userService.updateProfile(10L, "old@test.com", req);

        verify(userEmailHistoryRepository).save(any(UserEmailHistory.class));
        verify(userRepository).save(existing);
        assertThat(existing.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void updateProfile_superAdmin_canChangeRole() {
        User existing = new User();
        existing.setId(10L);
        existing.setEmail("user@test.com");
        existing.setRole(User.Role.USER);
        existing.setPassword("ENC");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));

        setAuth("super@test.com", "SUPER_ADMIN");

        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setRole("ADMIN");

        userService.updateProfile(10L, "super@test.com", req);

        verify(userRepository).save(existing);
        assertThat(existing.getRole()).isEqualTo(User.Role.ADMIN);
    }

    @Test
    void deleteUser_nonSuperAdmin_canOnlyDeleteSelf_softDelete() {
        User current = new User();
        current.setId(7L);
        current.setEmail("me@test.com");
        current.setRole(User.Role.USER);
        current.setSuperAdmin(false);

        User target = new User();
        target.setId(7L);
        target.setEmail("me@test.com");
        target.setRole(User.Role.USER);
        target.setSuperAdmin(false);

        setAuth("me@test.com", "USER");
        when(userRepository.findByEmailAndDeletedFalse("me@test.com")).thenReturn(Optional.of(current));
        when(userRepository.findById(7L)).thenReturn(Optional.of(target));

        userService.deleteUser(7L);

        verify(userRepository).save(target);
        assertThat(target.isDeleted()).isTrue();
        assertThat(target.getDeletionDate()).isNotNull();
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_nonSuperAdmin_deletingOtherUser_throws() {
        User current = new User();
        current.setId(7L);
        current.setEmail("me@test.com");
        current.setSuperAdmin(false);

        User target = new User();
        target.setId(8L);
        target.setEmail("other@test.com");

        setAuth("me@test.com", "USER");
        when(userRepository.findByEmailAndDeletedFalse("me@test.com")).thenReturn(Optional.of(current));
        when(userRepository.findById(8L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> userService.deleteUser(8L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You can only delete your own account");

        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_superAdmin_hardDelete_deletesEmailHistoryAndUser() {
        User current = new User();
        current.setId(1L);
        current.setEmail("super@test.com");
        current.setSuperAdmin(true);

        User target = new User();
        target.setId(8L);
        target.setEmail("target@test.com");

        setAuth("super@test.com", "SUPER_ADMIN");
        when(userRepository.findByEmailAndDeletedFalse("super@test.com")).thenReturn(Optional.of(current));
        when(userRepository.findById(8L)).thenReturn(Optional.of(target));
        when(userEmailHistoryRepository.findByUser(target)).thenReturn(List.of(new UserEmailHistory()));

        userService.deleteUser(8L);

        verify(userEmailHistoryRepository).deleteAll(ArgumentMatchers.<UserEmailHistory>anyList());
        verify(userRepository).delete(target);
        verify(userRepository, never()).save(target);
    }

    @Test
    void restoreUser_notDeleted_throws() {
        User u = new User();
        u.setId(5L);
        u.setDeleted(false);

        when(userRepository.findById(5L)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> userService.restoreUser(5L, "any@test.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is not deleted and cannot be restored.");
    }

    @Test
    void restoreUser_deleted_restoresAndSaves() {
        User u = new User();
        u.setId(5L);
        u.setEmail("a@test.com");
        u.setRole(User.Role.USER);
        u.setDeleted(true);

        when(userRepository.findById(5L)).thenReturn(Optional.of(u));

        var dto = userService.restoreUser(5L, "any@test.com");

        verify(userRepository).save(u);
        assertThat(u.isDeleted()).isFalse();
        assertThat(dto.isDeleted()).isFalse();
    }

    @Test
    void restoreUser_notFound_throwsUserNotFoundException() {
        when(userRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.restoreUser(123L, "x@test.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }
}