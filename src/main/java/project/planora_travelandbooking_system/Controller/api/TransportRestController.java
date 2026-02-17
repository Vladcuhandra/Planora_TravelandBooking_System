package project.planora_travelandbooking_system.Controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.DTO.TransportDTO;
import project.planora_travelandbooking_system.Service.TransportService;

import java.util.List;

@RestController
@RequestMapping("/api/transports")
public class TransportRestController {
    private final TransportService transportService;

    public TransportRestController(TransportService transportService) {
        this.transportService = transportService;
    }

    @GetMapping
    public List<TransportDTO> list() {
        return transportService.getAllTransports();
    }

    @GetMapping("/{id}")
    public TransportDTO get(@PathVariable Long id) {
        return transportService.getTransportById(id);
    }

    @PostMapping
    public ResponseEntity<TransportDTO> create(@RequestBody TransportDTO transportDTO) {
        TransportDTO created = transportService.saveTransport(transportDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public TransportDTO update(@PathVariable Long id,
                                               @RequestBody TransportDTO transportDTO) {
        return transportService.updateTransport(id, transportDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transportService.deleteTransport(id);
        return ResponseEntity.noContent().build();
    }

}
