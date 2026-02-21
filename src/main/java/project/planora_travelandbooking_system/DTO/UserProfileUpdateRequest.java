package project.planora_travelandbooking_system.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {
    private String currentPassword;
    private String email;
    private String role;
    private String newPassword;
}
