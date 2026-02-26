package project.planora_travelandbooking_system.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.planora_travelandbooking_system.dto.TripDTO;
import project.planora_travelandbooking_system.model.User;
import project.planora_travelandbooking_system.service.TripService;
import project.planora_travelandbooking_system.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TripRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = project.planora_travelandbooking_system.security.JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TripRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private UserService userService;

    private static Authentication authUser(String email) {
        return new UsernamePasswordAuthenticationToken(
                email,
                "N/A",
                AuthorityUtils.createAuthorityList("ROLE_USER")
        );
    }

    private static Authentication authAdmin(String email) {
        return new UsernamePasswordAuthenticationToken(
                email,
                "N/A",
                AuthorityUtils.createAuthorityList("ROLE_ADMIN")
        );
    }

    @Test
    void getTrips_user_returns200_andUsesTripsForUser() throws Exception {
        TripDTO dto = new TripDTO();
        dto.setId(1L);
        dto.setTitle("My trip");
        dto.setDescription("Desc");
        dto.setUserId(10L);

        Mockito.when(tripService.getTripsForUser("user@planora.test", 0, 10))
                .thenReturn(new PageImpl<>(
                        List.of(dto),
                        PageRequest.of(0, 10),
                        1
                ));

        mvc.perform(get("/api/trips")
                .principal(authUser("user@planora.test")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trips", hasSize(1)))
                .andExpect(jsonPath("$.trips[0].id", is(1)))
                .andExpect(jsonPath("$.trips[0].title", is("My trip")))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalItems", is(1)));

        Mockito.verify(tripService).getTripsForUser("user@planora.test", 0, 10);
        Mockito.verify(tripService, Mockito.never()).getAllTrips(anyInt(), anyInt());
    }

    @Test
    void getTrips_admin_returns200_andUsesGetAllTrips() throws Exception {
        TripDTO dto = new TripDTO();
        dto.setId(2L);
        dto.setTitle("Admin trip");
        dto.setUserId(99L);

        Mockito.when(tripService.getAllTrips(1, 5))
                .thenReturn(new PageImpl<>(
                        List.of(dto),
                        PageRequest.of(1, 5),
                        6
                ));

        mvc.perform(get("/api/trips")
                        .param("page", "1")
                        .param("size", "5")
                .principal(authAdmin("admin@planora.test")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trips", hasSize(1)))
                .andExpect(jsonPath("$.trips[0].id", is(2)))
                .andExpect(jsonPath("$.currentPage", is(1)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.totalItems", is(6)));

        Mockito.verify(tripService).getAllTrips(1, 5);
        Mockito.verify(tripService, Mockito.never()).getTripsForUser(anyString(), anyInt(), anyInt());
    }

    @Test
    void saveTrip_user_setsUserId_fromLoggedInUser_andReturns201() throws Exception {
        User current = new User();
        current.setId(10L);
        current.setEmail("user@planora.test");
        current.setRole(User.Role.USER);

        Mockito.when(userService.getUserByEmail("user@planora.test"))
                .thenReturn(Optional.of(current));

        ArgumentCaptor<TripDTO> dtoCaptor = ArgumentCaptor.forClass(TripDTO.class);

        mvc.perform(post("/api/trips/save")
                        .principal(authUser("user@planora.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"T","description":"D"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Trip saved successfully")))
                .andExpect(jsonPath("$.trip.title", is("T")))
                .andExpect(jsonPath("$.trip.userId", is(10)));

        Mockito.verify(userService).getUserByEmail("user@planora.test");
        Mockito.verify(tripService).saveTrip(dtoCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(10L, dtoCaptor.getValue().getUserId());
    }

    @Test
    void saveTrip_admin_keepsProvidedUserId_andReturns201() throws Exception {
        ArgumentCaptor<TripDTO> dtoCaptor = ArgumentCaptor.forClass(TripDTO.class);

        mvc.perform(post("/api/trips/save")
                        .principal(authAdmin("admin@planora.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"T","description":"D","userId":77}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Trip saved successfully")))
                .andExpect(jsonPath("$.trip.userId", is(77)));

        Mockito.verify(userService, Mockito.never()).getUserByEmail(anyString());
        Mockito.verify(tripService).saveTrip(dtoCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(77L, dtoCaptor.getValue().getUserId());
    }

    @Test
    void updateTrip_user_setsUserId_andPassesUserToService_returns200() throws Exception {
        User current = new User();
        current.setId(10L);
        current.setEmail("user@planora.test");
        current.setRole(User.Role.USER);

        Mockito.when(userService.getUserByEmail("user@planora.test"))
                .thenReturn(Optional.of(current));

        ArgumentCaptor<TripDTO> dtoCaptor = ArgumentCaptor.forClass(TripDTO.class);

        mvc.perform(put("/api/trips/edit/{id}", 5L)
                        .principal(authUser("user@planora.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated","description":"D"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Trip updated successfully")))
                .andExpect(jsonPath("$.trip.title", is("Updated")))
                .andExpect(jsonPath("$.trip.userId", is(10)));

        Mockito.verify(tripService).updateTrip(eq(5L), dtoCaptor.capture(), eq(current));
        org.junit.jupiter.api.Assertions.assertEquals(10L, dtoCaptor.getValue().getUserId());
    }

    @Test
    void updateTrip_admin_usesUserIdFromBody_andPassesThatUserToService_returns200() throws Exception {
        User target = new User();
        target.setId(77L);
        target.setEmail("someone@planora.test");
        target.setRole(User.Role.USER);

        Mockito.when(userService.getUserId(77L)).thenReturn(target);

        ArgumentCaptor<TripDTO> dtoCaptor = ArgumentCaptor.forClass(TripDTO.class);

        mvc.perform(put("/api/trips/edit/{id}", 5L)
                        .principal(authAdmin("admin@planora.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated","description":"D","userId":77}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Trip updated successfully")))
                .andExpect(jsonPath("$.trip.userId", is(77)));

        Mockito.verify(userService).getUserId(77L);
        Mockito.verify(tripService).updateTrip(eq(5L), dtoCaptor.capture(), eq(target));
        org.junit.jupiter.api.Assertions.assertEquals(77L, dtoCaptor.getValue().getUserId());
    }

    @Test
    void deleteTrip_user_callsAuthorizedDelete_returns200() throws Exception {
        mvc.perform(delete("/api/trips/delete/{id}", 9L)
                .principal(authUser("user@planora.test")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Trip deleted successfully")));

        Mockito.verify(tripService).deleteTripAuthorized(9L, "user@planora.test", false);
    }

    @Test
    void bulkDeleteTrips_admin_callsBulkDelete_returns200() throws Exception {
        mvc.perform(post("/api/trips/bulk-delete")
                        .principal(authAdmin("admin@planora.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Trips deleted successfully")));

        Mockito.verify(tripService).bulkDeleteTrips(eq(List.of(1L, 2L, 3L)), eq("admin@planora.test"), eq(true));
    }
}