package project.planora_travelandbooking_system.response;

import lombok.Getter;
import lombok.Setter;
import project.planora_travelandbooking_system.DTO.UserDTO;
import java.util.List;

@Getter
@Setter
public class AdminDashboardResponse {
    private List<UserDTO> users;
    private int totalPages;
    private int currentPage;
    private String searchEmail;
    private String role;
    private String accountStatus;

    public AdminDashboardResponse(List<UserDTO> users, int totalPages, int currentPage,
                                  String searchEmail, String role, String accountStatus) {
        this.users = users;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.searchEmail = searchEmail;
        this.role = role;
        this.accountStatus = accountStatus;
    }
}