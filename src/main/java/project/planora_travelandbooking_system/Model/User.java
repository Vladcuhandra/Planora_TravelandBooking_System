package project.planora_travelandbooking_system.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private LocalDateTime birthDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean superAdmin = false;

    private LocalDateTime createdAt;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deleted = false;

    private LocalDateTime deletionDate;

    public enum Role {
        USER, ADMIN
    }

}