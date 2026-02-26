package project.planora_travelandbooking_system.dtos;

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
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double pricePerNight;
    private double rating;
    private Integer room;
    private LocalDateTime createdAt;
    private String status;
}