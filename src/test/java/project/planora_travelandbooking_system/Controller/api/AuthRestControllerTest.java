package project.planora_travelandbooking_system.Controller.api;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import project.planora_travelandbooking_system.Security.JwtUtil;
import project.planora_travelandbooking_system.Service.JwtRefreshService;
import project.planora_travelandbooking_system.Service.UserService;
import project.planora_travelandbooking_system.Repository.UserEmailHistoryRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = project.planora_travelandbooking_system.Security.JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class AuthRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtRefreshService refreshService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserEmailHistoryRepository userEmailHistoryRepository;

    @Test
    void signup_positive_createsUser_returns201_andLocationHeader() throws Exception {
        // user not exists yet
        Mockito.when(userRepository.findByEmail("new@planora.test")).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode("123456")).thenReturn("ENC(123456)");

        // capture saved user
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.when(userRepository.save(userCaptor.capture()))
                .thenAnswer(inv -> inv.getArgument(0, User.class));

        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new@planora.test","password":"123456", "confirmPassword" : "123456"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/login"));

        User saved = userCaptor.getValue();
        // checks that controller filled fields
        org.junit.jupiter.api.Assertions.assertEquals("new@planora.test", saved.getEmail());
        org.junit.jupiter.api.Assertions.assertEquals("ENC(123456)", saved.getPassword());
        org.junit.jupiter.api.Assertions.assertNotNull(saved.getRole());
        org.junit.jupiter.api.Assertions.assertFalse(saved.isDeleted());
        org.junit.jupiter.api.Assertions.assertNotNull(saved.getCreatedAt());
    }

    @Test
    void login_positive_returns200_token_and_setsRefreshCookie() throws Exception {
        // auth manager authenticates successfully
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("new@planora.test");
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        // access token
        Mockito.when(jwtUtil.generateToken("new@planora.test")).thenReturn("ACCESS_TOKEN");

        // user exists
        User user = new User();
        user.setId(10L);
        user.setEmail("new@planora.test");
        user.setPassword("ENC(123456)");
        user.setRole(User.Role.USER);
        user.setCreatedAt(LocalDateTime.now());

        Mockito.when(userService.getUserByEmail("new@planora.test")).thenReturn(Optional.of(user));

        // refresh token
        Mockito.when(refreshService.createToken(eq(user), anyInt())).thenReturn("REFRESH_TOKEN");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new@planora.test","password":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("ACCESS_TOKEN")))
                .andExpect(jsonPath("$.email", is("new@planora.test")))
                // refresh cookie (set-cookie)
                .andExpect(header().string("Set-Cookie", containsString("refresh_token=REFRESH_TOKEN")));
    }

    @Test
    void login_negative_wrongPassword_returns401() throws Exception {
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(Mockito.mock(AuthenticationException.class));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new@planora.test","password":"WRONG_PASS"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }

    @Test
    void login_negative_incorrectEmail_returns401() throws Exception {
        Mockito.when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(Mockito.mock(AuthenticationException.class));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"nonexistent@planora.test","password":"123456"}
                            """))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }

    @Test
    void signup_negative_emailAlreadyExists_returns400() throws Exception {
        Mockito.when(userRepository.findByEmail("existing@planora.test")).thenReturn(Optional.of(new User()));

        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"existing@planora.test","password":"123456","confirmPassword":"123456"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is already registered or in history"));
    }

    @Test
    void signup_negative_invalidEmailFormat_returns400() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"invalid-email","password":"123456"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please enter a valid email"));
    }

    @Test
    void signup_negative_shortPassword_returns400() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"new@planora.test","password":"123"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password must be at least 6 characters"));
    }

    @Test
    void signup_negative_passwordsDoNotMatch_returns400() throws Exception {
        mvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"email":"new@planora.test","password":"123456","confirmPassword":"654321"}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Passwords do not match"));
    }

}