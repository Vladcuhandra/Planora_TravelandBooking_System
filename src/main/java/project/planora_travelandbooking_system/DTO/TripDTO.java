package project.planora_travelandbooking_system.DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class TripDTO {
    private Long id;
    private Long userId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
}