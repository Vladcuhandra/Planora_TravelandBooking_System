package project.planora_travelandbooking_system.Controller.api;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.planora_travelandbooking_system.DTO.AccommodationDTO;
import project.planora_travelandbooking_system.Model.Accommodation;
import project.planora_travelandbooking_system.Service.AccommodationService;
import project.planora_travelandbooking_system.Service.TransportService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AccommodationRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = project.planora_travelandbooking_system.Security.JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class AccommodationRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AccommodationService accommodationService;

    @MockitoBean
    private TransportService transportService;

    @Test
    void getAccommodations_defaultParams_returns200_andPage() throws Exception {
        AccommodationDTO dto = new AccommodationDTO();
        dto.setId(1L);
        dto.setAccommodationType("HOTEL");
        dto.setName("Hotel Test");
        dto.setCity("Riga");
        dto.setStatus("AVAILABLE");
        dto.setPricePerNight(50.0);
        dto.setRating(4.5);
        dto.setRoom(10);

        Page<AccommodationDTO> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10),
                1
        );

        Mockito.when(accommodationService.getAllAccommodations(0, 10)).thenReturn(page);

        mvc.perform(get("/api/accommodations"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].accommodationType", is("HOTEL")))
                .andExpect(jsonPath("$.content[0].name", is("Hotel Test")))
                .andExpect(jsonPath("$.content[0].city", is("Riga")))
                .andExpect(jsonPath("$.content[0].status", is("AVAILABLE")))
                .andExpect(jsonPath("$.totalElements", is(1)));

        Mockito.verify(accommodationService).getAllAccommodations(0, 10);
    }

    @Test
    void getAccommodations_customParams_callsServiceWithThoseParams() throws Exception {
        Mockito.when(accommodationService.getAllAccommodations(2, 5))
                .thenReturn(Page.empty(PageRequest.of(2, 5)));

        mvc.perform(get("/api/accommodations")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        Mockito.verify(accommodationService).getAllAccommodations(2, 5);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveAccommodation_admin_returns201_andMessage() throws Exception {
        mvc.perform(post("/api/accommodations/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accommodationType":"HOTEL",
                                  "name":"Hotel Test",
                                  "city":"Riga",
                                  "address":"Street 1",
                                  "pricePerNight":50.0,
                                  "rating":4.5,
                                  "room":10,
                                  "status":"AVAILABLE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("Accommodation created successfully!"));

        Mockito.verify(accommodationService).saveAccommodation(any(AccommodationDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void editAccommodation_admin_returns200_andMessage() throws Exception {
        mvc.perform(post("/api/accommodations/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 10,
                                  "accommodationType":"HOSTEL",
                                  "name":"Hostel Test",
                                  "city":"Riga",
                                  "address":"Street 2",
                                  "pricePerNight":20.0,
                                  "rating":4.0,
                                  "room":5,
                                  "status":"UNAVAILABLE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Accommodation updated successfully!"));

        Mockito.verify(accommodationService).saveAccommodation(any(AccommodationDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAccommodation_admin_returns200_andMessage() throws Exception {
        mvc.perform(delete("/api/accommodations/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(content().string("Accommodation deleted successfully!"));

        Mockito.verify(accommodationService).deleteAccommodation(7L);
    }

    @Test
    void getAccommodationTypes_returnsEnumNames() throws Exception {
        mvc.perform(get("/api/accommodations/types"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(Accommodation.AccommodationType.values().length)))
                .andExpect(jsonPath("$", hasItems("HOTEL", "HOSTEL", "AIRBNB", "GUESTHOUSE", "INTERNET_CAFE")));
    }

    @Test
    void getAccommodationStatuses_returnsEnumNames() throws Exception {
        mvc.perform(get("/api/accommodations/statuses"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(Accommodation.Status.values().length)))
                .andExpect(jsonPath("$", hasItems("AVAILABLE", "UNAVAILABLE")));
    }
}