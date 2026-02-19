package project.planora_travelandbooking_system.Service;

import jakarta.persistence.EntityNotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }

        String email = auth.getName();
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public UserDTO saveUser(UserDTO userDTO) {
        User user;

        if (userDTO.getId() != null) {
            user = userRepository.findById(userDTO.getId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userDTO.getId()));

            if (user.isSuperAdmin()) {
                throw new IllegalStateException("Super admin cannot be modified.");
            }
        } else {
            user = new User();
            user.setSuperAdmin(false);
            user.setCreatedAt(LocalDateTime.now());
        }

        user.setEmail(userDTO.getEmail());
        user.setRole(User.Role.valueOf(userDTO.getRole()));
        user.setBirthDate(userDTO.getBirthDate());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        userRepository.save(user);
        return convertToDTO(user);
    }

    public Page<UserDTO> getAllUsers(int page, int pageSize) {
        Page<User> usersPage = userRepository.findAll(PageRequest.of(page, pageSize));
        return usersPage.map(this::convertToDTO);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public UserDTO getUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            return convertToDTO(userOptional.get());
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    public User getUserId(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User currentUser = getCurrentAuthenticatedUser();
        User userToDelete = getUserOrThrow(userId);

        validateDeletionPermission(currentUser, userToDelete);

        if (isHardDeleteAllowed(currentUser)) {
            hardDeleteUser(userToDelete);
        } else {
            softDeleteUser(userToDelete);
        }
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private void validateDeletionPermission(User currentUser, User userToDelete) {

        if (!currentUser.isSuperAdmin() &&
                !currentUser.getId().equals(userToDelete.getId())) {
            throw new IllegalStateException("You can only delete your own account");
        }

        if (userToDelete.isSuperAdmin() &&
                !currentUser.isSuperAdmin()) {
            throw new IllegalStateException("SuperAdmin accounts cannot be deleted");
        }
    }

    private boolean isHardDeleteAllowed(User currentUser) {
        return currentUser.isSuperAdmin();
    }

    private void hardDeleteUser(User user) {
        userRepository.delete(user);
    }

    private void softDeleteUser(User user) {
        user.setDeleted(true);
        user.setDeletionDate(LocalDateTime.now());
        userRepository.save(user);
    }

    private User convertToEntity(UserDTO userDTO, User.Role role, String encodedPassword) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setEmail(userDTO.getEmail());
        user.setPassword(encodedPassword);
        user.setBirthDate(userDTO.getBirthDate());
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());
        user.setSuperAdmin(userDTO.isSuperAdmin());
        user.setDeleted(userDTO.isDeleted());
        user.setDeletionDate(userDTO.getDeletionDate());
        return user;
    }

    public UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setPassword(user.getPassword());
        userDTO.setBirthDate(user.getBirthDate());
        userDTO.setRole(user.getRole().name());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setSuperAdmin(user.isSuperAdmin());
        userDTO.setDeleted(user.isDeleted());
        userDTO.setDeletionDate(user.getDeletionDate());
        return userDTO;
    }
}