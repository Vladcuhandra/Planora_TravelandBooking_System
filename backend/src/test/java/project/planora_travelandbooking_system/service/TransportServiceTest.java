package project.planora_travelandbooking_system.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import project.planora_travelandbooking_system.dto.TransportDTO;
import project.planora_travelandbooking_system.model.Transport;
import project.planora_travelandbooking_system.repository.TransportRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportServiceTest {

    @Mock private TransportRepository transportRepository;
    @InjectMocks private TransportService transportService;

    @Captor ArgumentCaptor<Transport> transportCaptor;

    @Test
    void saveTransport_arrivalBeforeDeparture_throws() {
        TransportDTO dto = new TransportDTO();
        dto.setDepartureTime(LocalDateTime.of(2026, 1, 10, 10, 0));
        dto.setArrivalTime(LocalDateTime.of(2026, 1, 9, 10, 0));
        dto.setTransportType("FLIGHT");
        dto.setStatus("AVAILABLE");

        assertThatThrownBy(() -> transportService.saveTransport(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("arrivalTime cannot be before departureTime");

        verify(transportRepository, never()).save(any());
    }

    @Test
    void saveTransport_happyPath_setsCreatedAt_andMapsEnums() {
        TransportDTO dto = new TransportDTO();
        dto.setTransportType("FLIGHT");
        dto.setStatus("AVAILABLE");
        dto.setCompany("Air");
        dto.setOriginAddress("Riga");
        dto.setDestinationAddress("Paris");
        dto.setDepartureTime(LocalDateTime.of(2026, 1, 10, 10, 0));
        dto.setArrivalTime(LocalDateTime.of(2026, 1, 10, 12, 0));
        dto.setPrice(120.0);
        dto.setSeat(1);

        when(transportRepository.save(any())).thenAnswer(inv -> {
            Transport t = inv.getArgument(0);
            t.setId(50L);
            return t;
        });

        TransportDTO result = transportService.saveTransport(dto);

        verify(transportRepository).save(transportCaptor.capture());
        Transport saved = transportCaptor.getValue();

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getTransportType()).isEqualTo(Transport.TransportType.FLIGHT);
        assertThat(saved.getStatus()).isEqualTo(Transport.Status.AVAILABLE);
        assertThat(result.getId()).isEqualTo(50L);
    }

    @Test
    void deleteTransport_notFound_throws() {
        when(transportRepository.existsById(10L)).thenReturn(false);

        assertThatThrownBy(() -> transportService.deleteTransport(10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Transport not found with ID: 10");

        verify(transportRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateTransport_notFound_throws() {
        TransportDTO dto = new TransportDTO();
        dto.setDepartureTime(LocalDateTime.of(2026, 1, 10, 10, 0));
        dto.setArrivalTime(LocalDateTime.of(2026, 1, 10, 12, 0));
        dto.setTransportType("FLIGHT");
        dto.setStatus("AVAILABLE");

        when(transportRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transportService.updateTransport(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Transport not found with ID: 1");
    }
}