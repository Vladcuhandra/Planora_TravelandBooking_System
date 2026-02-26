package project.planora_travelandbooking_system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import project.planora_travelandbooking_system.model.JwtRefresher;
import project.planora_travelandbooking_system.model.User;
import project.planora_travelandbooking_system.repository.JwtRefresherRepository;
import project.planora_travelandbooking_system.security.TokenHashUtil;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtRefreshServiceTest {

    @Mock private UserService userService;
    @Mock private JwtRefresherRepository repo;

    @InjectMocks private JwtRefreshService jwtRefreshService;

    @Captor ArgumentCaptor<JwtRefresher> refresherCaptor;

    @Test
    void createToken_savesHashedToken_andReturnsPlainToken() {
        User u = new User();
        u.setId(1L);
        u.setEmail("u@test.com");

        String plain = jwtRefreshService.createToken(u, 7);

        assertThat(plain).isNotBlank();

        verify(repo).save(refresherCaptor.capture());
        JwtRefresher saved = refresherCaptor.getValue();

        assertThat(saved.getUser()).isSameAs(u);
        assertThat(saved.getTokenHash()).isEqualTo(TokenHashUtil.sha256(plain));
        assertThat(saved.isRevoked()).isFalse();
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void validateToken_invalid_throws() {
        String token = "abc";
        when(repo.findByTokenHashAndRevokedFalse(TokenHashUtil.sha256(token)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtRefreshService.validateToken(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid refresh token");
    }

    @Test
    void validateToken_expired_revokesAndThrows() {
        String token = "abc";
        JwtRefresher stored = new JwtRefresher();
        stored.setTokenHash(TokenHashUtil.sha256(token));
        stored.setRevoked(false);
        stored.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(repo.findByTokenHashAndRevokedFalse(TokenHashUtil.sha256(token)))
                .thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> jwtRefreshService.validateToken(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Refresh token expired");

        assertThat(stored.isRevoked()).isTrue();
        verify(repo, atLeastOnce()).save(stored);
    }

    @Test
    void validateToken_valid_setsLastUsedAt_andSaves() {
        String token = "abc";
        JwtRefresher stored = new JwtRefresher();
        stored.setTokenHash(TokenHashUtil.sha256(token));
        stored.setRevoked(false);
        stored.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(repo.findByTokenHashAndRevokedFalse(TokenHashUtil.sha256(token)))
                .thenReturn(Optional.of(stored));

        JwtRefresher result = jwtRefreshService.validateToken(token);

        assertThat(result).isSameAs(stored);
        assertThat(stored.getLastUsedAt()).isNotNull();
        verify(repo, atLeastOnce()).save(stored);
    }

    @Test
    void revoke_setsRevokedTrue_andSaves() {
        JwtRefresher r = new JwtRefresher();
        r.setRevoked(false);

        jwtRefreshService.revoke(r);

        assertThat(r.isRevoked()).isTrue();
        verify(repo).save(r);
    }

    @Test
    void revokeAllForUser_callsRepository() {
        when(repo.revokeAllActiveByUserId(5L)).thenReturn(3);

        int count = jwtRefreshService.revokeAllForUser(5L);

        assertThat(count).isEqualTo(3);
        verify(repo).revokeAllActiveByUserId(5L);
    }
}