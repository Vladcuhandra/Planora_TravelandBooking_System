package project.planora_travelandbooking_system.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignUpDTO {
    private String email;
    private String password;
    private String confirmPassword;
    public SignUpDTO() {}
}