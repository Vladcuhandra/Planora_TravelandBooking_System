package project.planora_travelandbooking_system.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditProfileRequest {
    private String currentPassword;
    private String email;
    private String role;
    private String newPassword;
}