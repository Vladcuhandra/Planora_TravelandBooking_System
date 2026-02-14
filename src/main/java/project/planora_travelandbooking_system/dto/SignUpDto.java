package project.planora_travelandbooking_system.dto;

import lombok.Getter;
import lombok.Setter;

public class SignUpDto {
    @Setter
    @Getter
    private String email;

    @Setter
    @Getter
    private String password;

    @Setter
    private String confirmPassword;

    public SignUpDto() {
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }


}
