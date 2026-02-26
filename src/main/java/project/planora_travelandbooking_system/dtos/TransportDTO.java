package project.planora_travelandbooking_system.dtos;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransportDTO {
    private Long id;
    private String transportType;
    private String company;
    private String originAddress;
    private String destinationAddress;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private double price;
    private Integer seat;
    private String status;
    private LocalDateTime createdAt;
}