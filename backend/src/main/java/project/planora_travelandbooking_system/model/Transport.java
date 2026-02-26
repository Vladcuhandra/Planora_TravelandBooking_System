package project.planora_travelandbooking_system.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Transport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransportType transportType;

    private String company;
    private String originAddress;
    private String destinationAddress;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer seat;
    private double price;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum TransportType {
        FLIGHT, TRAIN, BUS, SHIP
    }

    public enum Status {
        AVAILABLE, UNAVAILABLE
    }
}