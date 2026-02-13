package project.planora_travelandbooking_system.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.planora_travelandbooking_system.Model.Transport;
import project.planora_travelandbooking_system.Repository.TransportRepository;
import java.util.List;
import java.util.Optional;

@Service
public class TransportService {

    private final TransportRepository transportRepository;

    @Autowired
    public TransportService(TransportRepository transportRepository) {
        this.transportRepository = transportRepository;
    }

    public Transport saveFlight(Transport transport) {
        return transportRepository.save(transport);
    }

    public List<Transport> getAllFlights() {
        return transportRepository.findAll();
    }

    public Optional<Transport> getFlightById(Long id) {
        return transportRepository.findById(id);
    }

    public void deleteFlight(Long id) {
        transportRepository.deleteById(id);
    }

}