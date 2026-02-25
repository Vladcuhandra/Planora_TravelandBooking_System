package project.planora_travelandbooking_system.DTO;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;
    private boolean superAdmin;
    private LocalDateTime createdAt;
    private boolean deleted;
    private LocalDateTime deletionDate;
    private String confirmPassword;
}