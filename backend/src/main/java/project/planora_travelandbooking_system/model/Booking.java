package project.planora_travelandbooking_system.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private BookingType bookingType;

    private double totalPrice;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @ManyToOne
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    @JoinColumn(name = "transport_id", nullable = true)
    private Transport transport;

    @ManyToOne
    @JoinColumn(name = "accommodation_id", nullable = true)
    private Accommodation accommodation;

    private LocalDateTime createdAt;

    public enum BookingType {
        TRANSPORT, ACCOMMODATION
    }

    public enum BookingStatus {
        CONFIRMED, CANCELLED
    }
}