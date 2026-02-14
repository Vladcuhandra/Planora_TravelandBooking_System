package project.planora_travelandbooking_system.DTO;

import lombok.AccessLevel;
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