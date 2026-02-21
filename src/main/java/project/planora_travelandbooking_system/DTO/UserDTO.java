package project.planora_travelandbooking_system.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String name;
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String role;
    private boolean superAdmin;
    private LocalDateTime createdAt;
    private LocalDateTime birthDate;
    private boolean deleted;
    private LocalDateTime deletionDate;
}