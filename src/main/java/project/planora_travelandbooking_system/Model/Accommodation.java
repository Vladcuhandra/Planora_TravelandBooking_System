package project.planora_travelandbooking_system.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccommodationType accommodationType;

    private String name;
    private String city;
    private String address;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double rating;
    private Integer room;
    private double pricePerNight;
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum AccommodationType {
        HOTEL, HOSTEL, AIRBNB, GUESTHOUSE, INTERNET_CAFE
    }

    public enum Status {
        AVAILABLE, UNAVAILABLE
    }
}