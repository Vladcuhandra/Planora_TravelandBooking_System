package project.planora_travelandbooking_system.DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDTO {
    private Long id;
    private Long tripId;
    private String bookingType;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private Long transportId;
    private Long accommodationId;
    private double totalPrice;
}