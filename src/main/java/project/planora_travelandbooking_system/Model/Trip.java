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
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "transport_id")
    private Transport transport;

    @ManyToOne(optional = false)
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;
}
