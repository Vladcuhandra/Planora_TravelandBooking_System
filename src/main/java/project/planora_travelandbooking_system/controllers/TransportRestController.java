package project.planora_travelandbooking_system.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.planora_travelandbooking_system.dtos.TransportDTO;
import project.planora_travelandbooking_system.models.Transport;
import project.planora_travelandbooking_system.services.TransportService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transports")
public class TransportRestController {

    private final TransportService transportService;

    @Autowired
    public TransportRestController(TransportService transportService) {
        this.transportService = transportService;
    }

    @GetMapping
    public ResponseEntity<Page<TransportDTO>> getTransports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<TransportDTO> transports = transportService.getAllTransports(page, size);
        return ResponseEntity.ok(transports);
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveTransport(@RequestBody TransportDTO dto) {
        transportService.saveTransport(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Transport created successfully!");
    }

    @PostMapping("/edit")
    public ResponseEntity<String> editTransport(@RequestBody TransportDTO dto) {
        transportService.saveTransport(dto);
        return ResponseEntity.ok("Transport updated successfully!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransport(@PathVariable Long id) {
        transportService.deleteTransport(id);
        return ResponseEntity.ok("Transport deleted successfully!");
    }

    @GetMapping("/types")
    public ResponseEntity<List<String>> getTransportTypes() {
        List<String> types = Arrays.stream(Transport.TransportType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(types);
    }

    @GetMapping("/statuses")
    public ResponseEntity<List<String>> getStatuses() {
        List<String> statuses = Arrays.stream(Transport.Status.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(statuses);
    }
}