package project.planora_travelandbooking_system.controllers;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.planora_travelandbooking_system.dtos.TransportDTO;
import project.planora_travelandbooking_system.models.Transport;
import project.planora_travelandbooking_system.services.TransportService;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TransportRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = project.planora_travelandbooking_system.securiity.JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
public class TransportRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TransportService transportService;

    @Test
    void getTransports_defaultParams_returns200_andPage() throws Exception {
        TransportDTO dto = new TransportDTO();
        dto.setId(1L);
        dto.setTransportType("FLIGHT");
        dto.setCompany("AirTest");
        dto.setStatus("AVAILABLE");

        Page<TransportDTO> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10),
                1
        );

        Mockito.when(transportService.getAllTransports(0, 10)).thenReturn(page);

        mvc.perform(get("/api/transports"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                // Spring Page -> JSON { content: [ ... ], totalElements: ..., ... }
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].transportType", is("FLIGHT")))
                .andExpect(jsonPath("$.content[0].company", is("AirTest")))
                .andExpect(jsonPath("$.content[0].status", is("AVAILABLE")))
                .andExpect(jsonPath("$.totalElements", is(1)));

        Mockito.verify(transportService).getAllTransports(0, 10);
    }

    @Test
    void getTransports_customParams_callsServiceWithThoseParams() throws Exception {
        Mockito.when(transportService.getAllTransports(2, 5))
                .thenReturn(Page.empty(PageRequest.of(2, 5)));

        mvc.perform(get("/api/transports")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk());

        Mockito.verify(transportService).getAllTransports(2, 5);
    }

    @Test
    void saveTransport_returns201_andMessage() throws Exception {
        // service.saveTransport(dto) is mocked, do nothing by default
        mvc.perform(post("/api/transports/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transportType":"TRAIN",
                                  "company":"RailTest",
                                  "originAddress":"A",
                                  "destinationAddress":"B",
                                  "price":10.5,
                                  "seat":50,
                                  "status":"AVAILABLE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("Transport created successfully!"));

        Mockito.verify(transportService).saveTransport(any(TransportDTO.class));
    }

    @Test
    void editTransport_returns200_andMessage() throws Exception {
        mvc.perform(post("/api/transports/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": 10,
                                  "transportType":"BUS",
                                  "company":"BusTest",
                                  "originAddress":"A",
                                  "destinationAddress":"B",
                                  "price":5.0,
                                  "seat":30,
                                  "status":"UNAVAILABLE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Transport updated successfully!"));

        Mockito.verify(transportService).saveTransport(any(TransportDTO.class));
    }

    @Test
    void deleteTransport_returns200_andMessage() throws Exception {
        mvc.perform(delete("/api/transports/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(content().string("Transport deleted successfully!"));

        Mockito.verify(transportService).deleteTransport(7L);
    }

    @Test
    void getTransportTypes_returnsEnumNames() throws Exception {
        mvc.perform(get("/api/transports/types"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(Transport.TransportType.values().length)))
                .andExpect(jsonPath("$", hasItems("FLIGHT", "TRAIN", "BUS", "SHIP")));
    }

    @Test
    void getStatuses_returnsEnumNames() throws Exception {
        mvc.perform(get("/api/transports/statuses"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(Transport.Status.values().length)))
                .andExpect(jsonPath("$", hasItems("AVAILABLE", "UNAVAILABLE")));
    }
}