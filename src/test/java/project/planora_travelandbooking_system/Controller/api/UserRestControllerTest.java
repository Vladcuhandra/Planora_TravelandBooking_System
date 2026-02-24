package project.planora_travelandbooking_system.Controller.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import project.planora_travelandbooking_system.Service.UserService;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = project.planora_travelandbooking_system.Security.JwtFilter.class
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

    @Test
    void profile_withoutPrincipal_returns401() throws Exception {
        // without @WithMockUser => no authentication => expect 401
        mvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
        // If your controller returns a JSON body like {"message":"Unauthorized"},
        // you can re-enable this assertion (only if it actually exists):
        // .andExpect(jsonPath("$.message", is("Unauthorized")));
    }

    @Test
    @WithMockUser(username = "user@planora.test", roles = {"USER"})
    void profile_withPrincipal_returnsUser() throws Exception {
        User u = new User();
        u.setId(1L);
        u.setEmail("user@planora.test");

        Mockito.when(userService.getUserByEmail("user@planora.test"))
                .thenReturn(Optional.of(u));

        mvc.perform(get("/api/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("user@planora.test")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createUser_whenEmailNotExists_returns201() throws Exception {
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
}