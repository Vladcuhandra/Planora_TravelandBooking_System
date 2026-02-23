package project.planora_travelandbooking_system.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserProfileUpdateRequest {

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    private String newPassword;
    private String role;

    private Boolean deleted;
    private LocalDateTime deletionDate;

    private Boolean restore;

}