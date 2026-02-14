package project.planora_travelandbooking_system.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;
    private LocalDateTime birthDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    private LocalDateTime createdAt;

    public enum Role {
        USER, ADMIN
    }
}