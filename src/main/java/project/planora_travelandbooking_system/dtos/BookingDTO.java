package project.planora_travelandbooking_system.dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingDTO {
    private Long id;
    private Long tripId;
    private String bookingType;
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private LocalDateTime createdAt;
    private Long transportId;
    private Long accommodationId;
    private double totalPrice;
}
