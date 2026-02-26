package project.planora_travelandbooking_system.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.planora_travelandbooking_system.dto.UserDTO;
import project.planora_travelandbooking_system.model.User;
import project.planora_travelandbooking_system.repository.JwtRefresherRepository;
import project.planora_travelandbooking_system.repository.UserRepository;
import project.planora_travelandbooking_system.service.UserService;

import java.security.Principal;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = project.planora_travelandbooking_system.security.JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class UserRestControllerTest {

        @Autowired
        private MockMvc mvc;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private UserRepository userRepository;

        @MockitoBean
        private JwtRefresherRepository jwtRefresherRepository;

        @Test
        void profile_withoutPrincipal_returns401_andMessage() throws Exception {
                mvc.perform(get("/api/users/profile"))
                        .andExpect(status().isUnauthorized())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.message", is("Unauthorized")));
        }

        @Test
        void profile_withPrincipal_returns200_andUserJson() throws Exception {
                Principal principal = () -> "user@planora.test";

                User u = new User();
                u.setId(1L);
                u.setEmail("user@planora.test");

                Mockito.when(userService.getUserByEmail("user@planora.test"))
                        .thenReturn(Optional.of(u));

                mvc.perform(get("/api/users/profile").principal(principal))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.email", is("user@planora.test")));
        }

        @Test
        void createUser_whenEmailNotExists_returns201_andUserDto() throws Exception {
                Mockito.when(userRepository.existsByEmail("new@planora.test"))
                        .thenReturn(false);

                UserDTO created = new UserDTO();
                created.setId(123L);
                created.setEmail("new@planora.test");
                created.setRole("USER");

                Mockito.when(userService.saveUser(any(UserDTO.class)))
                        .thenReturn(created);

                mvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "email":"new@planora.test",
                                  "role":"USER",
                                  "password":"123"
                                }
                                """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.id", is(123)))
                        .andExpect(jsonPath("$.email", is("new@planora.test")))
                        .andExpect(jsonPath("$.role", is("USER")));

                Mockito.verify(userService).saveUser(any(UserDTO.class));
        }

        @Test
        void createUser_whenEmailExists_returns409() throws Exception {
                Mockito.when(userRepository.existsByEmail("new@planora.test"))
                        .thenReturn(true);

                mvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                  "email":"new@planora.test",
                                  "role":"USER",
                                  "password":"123"
                                }
                                """))
                        .andExpect(status().isConflict());

                Mockito.verify(userService, Mockito.never()).saveUser(any(UserDTO.class));
        }
}