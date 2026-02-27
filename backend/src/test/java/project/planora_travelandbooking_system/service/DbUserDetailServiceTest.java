package project.planora_travelandbooking_system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import project.planora_travelandbooking_system.model.User;
import project.planora_travelandbooking_system.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbUserDetailServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private DbUserDetailService dbUserDetailService;

    @Test
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> dbUserDetailService.loadUserByUsername("x@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("x@test.com");
    }

    @Test
    void loadUserByUsername_trimsAndLowercasesEmail() {
        User u = new User();
        u.setEmail("User@Test.com");
        u.setPassword("ENC");
        u.setRole(User.Role.USER);
        u.setDeleted(false);
        u.setSuperAdmin(false);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(u));

        var details = dbUserDetailService.loadUserByUsername("  USER@TEST.COM  ");

        verify(userRepository).findByEmail("user@test.com");
        assertThat(details.getUsername()).isEqualTo("User@Test.com"); // uses stored email
        assertThat(details.getPassword()).isEqualTo("ENC");
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_deleted_user_isLockedAndDisabled() {
        User u = new User();
        u.setEmail("u@test.com");
        u.setPassword("ENC");
        u.setRole(User.Role.USER);
        u.setDeleted(true);
        u.setSuperAdmin(false);

        when(userRepository.findByEmail("u@test.com")).thenReturn(Optional.of(u));

        var details = dbUserDetailService.loadUserByUsername("u@test.com");

        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_superAdmin_roleIsSUPER_ADMIN() {
        User u = new User();
        u.setEmail("sa@test.com");
        u.setPassword("ENC");
        u.setRole(User.Role.USER);
        u.setDeleted(false);
        u.setSuperAdmin(true);

        when(userRepository.findByEmail("sa@test.com")).thenReturn(Optional.of(u));

        var details = dbUserDetailService.loadUserByUsername("sa@test.com");

        // Spring stores roles as authorities with prefix ROLE_
        assertThat(details.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_ADMIN".equals(a.getAuthority())))
                .isTrue();
    }
}