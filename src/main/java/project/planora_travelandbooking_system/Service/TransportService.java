package project.planora_travelandbooking_system.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.DTO.TransportDTO;
import project.planora_travelandbooking_system.Model.Transport;
import project.planora_travelandbooking_system.Repository.TransportRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransportService {

    private final TransportRepository transportRepository;

    @Autowired
    public TransportService(TransportRepository transportRepository) {
        this.transportRepository = transportRepository;
    }

    public TransportDTO saveTransport(TransportDTO transportDTO) {
        Transport.TransportType transportType = Transport.TransportType.valueOf(transportDTO.getTransportType());
        Transport.Status status = Transport.Status.valueOf(transportDTO.getStatus());

        Transport transport = convertToEntity(transportDTO, transportType, status);
        Transport savedTransport = transportRepository.save(transport);

        return convertToDTO(savedTransport);
    }

    public Page<TransportDTO> getAllTransports(int page, int pageSize) {
        Page<Transport> transportPage = transportRepository.findAll(PageRequest.of(page, pageSize));
        return transportPage.map(this::convertToDTO);
    }

    public List<TransportDTO> getAllTransports() {
        List<Transport> transports = transportRepository.findAll();
        return transports.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public TransportDTO getTransportById(Long transportId) {
        Optional<Transport> transportOptional = transportRepository.findById(transportId);
        if (transportOptional.isPresent()) {
            return convertToDTO(transportOptional.get());
        } else {
            throw new RuntimeException("Transport not found with ID: " + transportId);
        }
    }

    @Transactional
    public void deleteTransport(Long transportId) {
        if (!transportRepository.existsById(transportId)) {
            throw new RuntimeException("Transport not found with ID: " + transportId);
        }
        transportRepository.deleteById(transportId);
    }

    public List<Transport> getTransportByStatus(Transport.Status status) {
        return transportRepository.findByStatus(status);
    }

    @Transactional
    public TransportDTO updateTransport(Long transportId, TransportDTO transportDTO) {

        Transport transport = transportRepository.findById(transportId)
                .orElseThrow(() ->
                        new RuntimeException("Transport not found with ID: " + transportId));

        Transport.TransportType transportType =
                Transport.TransportType.valueOf(transportDTO.getTransportType());
        Transport.Status status =
                Transport.Status.valueOf(transportDTO.getStatus());

        transport.setTransportType(transportType);
        transport.setCompany(transportDTO.getCompany());
        transport.setOriginAddress(transportDTO.getOriginAddress());
        transport.setDestinationAddress(transportDTO.getDestinationAddress());
        transport.setDepartureTime(transportDTO.getDepartureTime());
        transport.setArrivalTime(transportDTO.getArrivalTime());
        transport.setPrice(transportDTO.getPrice());
        transport.setSeat(transportDTO.getSeat());
        transport.setStatus(status);

        Transport updated = transportRepository.save(transport);

        return convertToDTO(updated);
    }

    private Transport convertToEntity(TransportDTO transportDTO, Transport.TransportType transportType, Transport.Status status) {
        Transport transport = new Transport();
        transport.setId(transportDTO.getId());
        transport.setTransportType(transportType);
        transport.setCompany(transportDTO.getCompany());
        transport.setOriginAddress(transportDTO.getOriginAddress());
        transport.setDestinationAddress(transportDTO.getDestinationAddress());
        transport.setDepartureTime(transportDTO.getDepartureTime());
        transport.setArrivalTime(transportDTO.getArrivalTime());
        transport.setPrice(transportDTO.getPrice());
        transport.setSeat(transportDTO.getSeat());
        transport.setStatus(status);
        transport.setCreatedAt(LocalDateTime.now());
        return transport;
    }

    private TransportDTO convertToDTO(Transport transport) {
        TransportDTO transportDTO = new TransportDTO();
        transportDTO.setId(transport.getId());
        transportDTO.setTransportType(transport.getTransportType().name());
        transportDTO.setCompany(transport.getCompany());
        transportDTO.setOriginAddress(transport.getOriginAddress());
        transportDTO.setDestinationAddress(transport.getDestinationAddress());
        transportDTO.setDepartureTime(transport.getDepartureTime());
        transportDTO.setArrivalTime(transport.getArrivalTime());
        transportDTO.setPrice(transport.getPrice());
        transportDTO.setSeat(transport.getSeat());
        transportDTO.setStatus(transport.getStatus().name());
        transportDTO.setCreatedAt(transport.getCreatedAt());
        return transportDTO;
    }

}