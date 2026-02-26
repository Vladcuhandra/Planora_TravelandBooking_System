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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import project.planora_travelandbooking_system.dto.TransportDTO;
import project.planora_travelandbooking_system.model.Transport;
import project.planora_travelandbooking_system.service.TransportService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TransportRestController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = project.planora_travelandbooking_system.security.JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TransportRestControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TransportService transportService;

    @Test
    void getAllTransports_returns200_andPageContent() throws Exception {
        TransportDTO dto = new TransportDTO();
        dto.setId(1L);
        dto.setTransportType("BUS");
        dto.setCompany("TestCompany");
        dto.setPrice(12.5);
        dto.setStatus("AVAILABLE");

        Mockito.when(transportService.getAllTransports(0, 10))
                .thenReturn(new PageImpl<>(
                        List.of(dto),
                        PageRequest.of(0, 10),
                        1
                ));

        mvc.perform(get("/api/transports")) // defaults: page=0 size=10
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].transportType", is("BUS")))
                .andExpect(jsonPath("$.content[0].company", is("TestCompany")));

        Mockito.verify(transportService).getAllTransports(0, 10);
    }

    @Test
    void saveTransport_returns201_andMessage() throws Exception {
        mvc.perform(post("/api/transports/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "transportType":"BUS",
                                  "company":"TestCompany",
                                  "price":12.5,
                                  "status":"AVAILABLE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().string("Transport created successfully!"));

        Mockito.verify(transportService).saveTransport(any(TransportDTO.class));
    }

    @Test
    void getTransportTypes_returnsEnumNames() throws Exception {
        mvc.perform(get("/api/transports/types"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(Transport.TransportType.values().length)));
    }

    @Test
    void getTransportStatuses_returnsEnumNames() throws Exception {
        mvc.perform(get("/api/transports/statuses"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(Transport.Status.values().length)));
    }
}