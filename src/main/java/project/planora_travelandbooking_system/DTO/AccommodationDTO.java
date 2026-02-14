package project.planora_travelandbooking_system.DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AccommodationDTO {
    private Long id;
    private String accommodationType;
    private String name;
    private String city;
    private String address;
    private double pricePerNight;
    private double rating;
    private String room;
    private LocalDateTime createdAt;
    private String status;
}