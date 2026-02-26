package project.planora_travelandbooking_system.controller;

import org.junit.jupiter.api.Test;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.planora_travelandbooking_system.dto.BookingDTO;
import project.planora_travelandbooking_system.model.Accommodation;
import project.planora_travelandbooking_system.model.Transport;
import project.planora_travelandbooking_system.model.Trip;
import project.planora_travelandbooking_system.repository.AccommodationRepository;
import project.planora_travelandbooking_system.repository.TransportRepository;
import project.planora_travelandbooking_system.repository.TripRepository;
import project.planora_travelandbooking_system.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BookingRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = project.planora_travelandbooking_system.security.JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class BookingRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private TripRepository tripRepository;

    @MockitoBean
    private TransportRepository transportRepository;

    @MockitoBean
    private AccommodationRepository accommodationRepository;

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
    void getBookings_user_returns200_andCallsServiceAsUser() throws Exception {
        BookingDTO dto = new BookingDTO();
        dto.setId(1L);
        dto.setTripId(10L);
        dto.setBookingType("TRANSPORT");
        dto.setStatus("CONFIRMED");
        dto.setStartDate(LocalDateTime.of(2026, 1, 1, 10, 0));
        dto.setEndDate(LocalDateTime.of(2026, 1, 2, 10, 0));
        dto.setCreatedAt(LocalDateTime.of(2025, 12, 31, 12, 0));
        dto.setTransportId(5L);
        dto.setAccommodationId(null);
        dto.setTotalPrice(123.45);

        Mockito.when(bookingService.getAllBookings(0, 10, "user@planora.test", false))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mvc.perform(get("/api/bookings")
                        .principal(authUser("user@planora.test")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookings", hasSize(1)))
                .andExpect(jsonPath("$.bookings[0].id", is(1)))
                .andExpect(jsonPath("$.bookings[0].tripId", is(10)))
                .andExpect(jsonPath("$.bookings[0].bookingType", is("TRANSPORT")))
                .andExpect(jsonPath("$.bookings[0].status", is("CONFIRMED")))
                .andExpect(jsonPath("$.bookings[0].transportId", is(5)))
                .andExpect(jsonPath("$.bookings[0].totalPrice", is(123.45)))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalItems", is(1)));

        Mockito.verify(bookingService).getAllBookings(0, 10, "user@planora.test", false);
    }

    @Test
    void getBookings_admin_returns200_andCallsServiceAsAdmin() throws Exception {
        BookingDTO dto = new BookingDTO();
        dto.setId(2L);
        dto.setTripId(99L);
        dto.setBookingType("ACCOMMODATION");
        dto.setStatus("PENDING");
        dto.setAccommodationId(7L);
        dto.setTotalPrice(999.0);

        Mockito.when(bookingService.getAllBookings(1, 5, "admin@planora.test", true))
                .thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(1, 5), 6));

        mvc.perform(get("/api/bookings")
                        .param("page", "1")
                        .param("size", "5")
                        .principal(authAdmin("admin@planora.test")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookings", hasSize(1)))
                .andExpect(jsonPath("$.bookings[0].id", is(2)))
                .andExpect(jsonPath("$.bookings[0].bookingType", is("ACCOMMODATION")))
                .andExpect(jsonPath("$.currentPage", is(1)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.totalItems", is(6)));

        Mockito.verify(bookingService).getAllBookings(1, 5, "admin@planora.test", true);
    }

    @Test
    void saveBooking_user_returns201_andCallsService() throws Exception {
        mvc.perform(post("/api/bookings/save")
                        .principal(authUser("user@planora.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tripId": 10,
                                  "bookingType": "TRANSPORT",
                                  "status": "CONFIRMED",
                                  "transportId": 5,
                                  "totalPrice": 123.45
                                }
                                """))
                .andExpect(status().isCreated());

        Mockito.verify(bookingService).saveBooking(any(BookingDTO.class), eq("user@planora.test"), eq(false));
    }

    @Test
    void saveBooking_admin_returns201_andCallsServiceAsAdmin() throws Exception {
        mvc.perform(post("/api/bookings/save")
                        .principal(authAdmin("admin@planora.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tripId": 99,
                                  "bookingType": "ACCOMMODATION",
                                  "status": "PENDING",
                                  "accommodationId": 7,
                                  "totalPrice": 999.0
                                }
                                """))
                .andExpect(status().isCreated());

        Mockito.verify(bookingService).saveBooking(any(BookingDTO.class), eq("admin@planora.test"), eq(true));
    }

    @Test
    void updateBooking_user_returns200_andCallsService() throws Exception {
        mvc.perform(post("/api/bookings/edit/{id}", 5L)
                        .principal(authUser("user@planora.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 5,
                                  "tripId": 10,
                                  "bookingType": "TRANSPORT",
                                  "status": "CANCELLED",
                                  "transportId": 5,
                                  "totalPrice": 50.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        Mockito.verify(bookingService).updateBooking(eq(5L), any(BookingDTO.class), eq("user@planora.test"), eq(false));
    }

    @Test
    void updateBooking_admin_returns200_andCallsServiceAsAdmin() throws Exception {
        mvc.perform(post("/api/bookings/edit/{id}", 6L)
                        .principal(authAdmin("admin@planora.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 6,
                                  "tripId": 99,
                                  "bookingType": "ACCOMMODATION",
                                  "status": "CONFIRMED",
                                  "accommodationId": 7,
                                  "totalPrice": 999.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(6)))
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        Mockito.verify(bookingService).updateBooking(eq(6L), any(BookingDTO.class), eq("admin@planora.test"), eq(true));
    }

    @Test
    void deleteBooking_user_returns204_andCallsService() throws Exception {
        mvc.perform(delete("/api/bookings/{id}", 9L)
                        .principal(authUser("user@planora.test")))
                .andExpect(status().isNoContent());

        Mockito.verify(bookingService).deleteBooking(9L, "user@planora.test", false);
    }

    @Test
    void deleteBooking_admin_returns204_andCallsServiceAsAdmin() throws Exception {
        mvc.perform(delete("/api/bookings/{id}", 10L)
                        .principal(authAdmin("admin@planora.test")))
                .andExpect(status().isNoContent());

        Mockito.verify(bookingService).deleteBooking(10L, "admin@planora.test", true);
    }

    @Test
    void bulkDeleteBookings_admin_returns204_andCallsService() throws Exception {
        mvc.perform(post("/api/bookings/bulk-delete")
                        .principal(authAdmin("admin@planora.test"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2,3]"))
                .andExpect(status().isNoContent());

        Mockito.verify(bookingService).bulkDeleteBookings(eq(List.of(1L, 2L, 3L)), eq("admin@planora.test"), eq(true));
    }

    @Test
    void getTrips_user_returns200_andUsesFindByUserEmail() throws Exception {
        Trip t = new Trip();
        t.setId(1L);

        Mockito.when(tripRepository.findByUserEmail("user@planora.test"))
                .thenReturn(List.of(t));

        mvc.perform(get("/api/bookings/trips")
                        .principal(authUser("user@planora.test")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        Mockito.verify(tripRepository).findByUserEmail("user@planora.test");
        Mockito.verify(tripRepository, Mockito.never()).findAll();
    }

    @Test
    void getTrips_admin_returns200_andUsesFindAll() throws Exception {
        Trip t = new Trip();
        t.setId(2L);

        Mockito.when(tripRepository.findAll())
                .thenReturn(List.of(t));

        mvc.perform(get("/api/bookings/trips")
                        .principal(authAdmin("admin@planora.test")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)));

        Mockito.verify(tripRepository).findAll();
        Mockito.verify(tripRepository, Mockito.never()).findByUserEmail(anyString());
    }

    @Test
    void getTransports_returns200_andCallsRepo() throws Exception {
        Transport tr = new Transport();
        tr.setId(3L);

        Mockito.when(transportRepository.findAll())
                .thenReturn(List.of(tr));

        mvc.perform(get("/api/bookings/transports")
                        .principal(authUser("user@planora.test")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(3)));

        Mockito.verify(transportRepository).findAll();
    }

    @Test
    void getAccommodations_returns200_andCallsRepo() throws Exception {
        Accommodation a = new Accommodation();
        a.setId(4L);

        Mockito.when(accommodationRepository.findAll())
                .thenReturn(List.of(a));

        mvc.perform(get("/api/bookings/accommodations")
                        .principal(authUser("user@planora.test")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(4)));

        Mockito.verify(accommodationRepository).findAll();
    }
}