package project.planora_travelandbooking_system.Service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.planora_travelandbooking_system.DTO.UserDTO;
import project.planora_travelandbooking_system.DTO.UserProfileUpdateRequest;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import project.planora_travelandbooking_system.exceptions.InvalidPasswordException;

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

        // Only update fields that are non-null in DTO
        if (userDTO.getEmail() != null && !userDTO.getEmail().isBlank()) {
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getRole() != null && !userDTO.getRole().isBlank()) {
            user.setRole(User.Role.valueOf(userDTO.getRole()));
        }

        if (userDTO.getBirthDate() != null) {
            user.setBirthDate(userDTO.getBirthDate());
        }

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Preserve all other fields like deleted, creation date, etc.
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


    @Transactional
    public UserDTO updateProfile(Long userId, String principalEmail, UserProfileUpdateRequest req) {

        User existingUser = getUserId(userId);

        var auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean editingSelf = principalEmail != null && principalEmail.equals(existingUser.getEmail());

        // USER can only edit self; ADMIN can edit anyone
        if (!isAdmin && !editingSelf) {
            throw new AccessDeniedException("Unauthorized action.");
        }

        boolean changingPassword = req.getNewPassword() != null && !req.getNewPassword().isBlank();

        // ✅ Require correct currentPassword when password is being changed
        // (and optionally when editing self even without password change)
        if (changingPassword || editingSelf) {

            if (req.getCurrentPassword() == null || req.getCurrentPassword().isBlank()) {
                throw new IllegalArgumentException("Current password is required.");
            }

            if (!passwordEncoder.matches(req.getCurrentPassword(), existingUser.getPassword())) {
                throw new InvalidPasswordException("Wrong current password");
            }
        }

        // Email update
        if (req.getEmail() != null && !req.getEmail().isBlank()
                && !req.getEmail().equals(existingUser.getEmail())) {

            // Optional (recommended):
            // if (userRepository.existsByEmail(req.getEmail())) {
            //     throw new IllegalArgumentException("User already exists with this email.");
            // }

            existingUser.setEmail(req.getEmail());
        }

        // Role update rules (keeps your original behavior)
        if (!existingUser.isSuperAdmin()) {
            existingUser.setRole(User.Role.USER);
        } else if (req.getRole() != null && !req.getRole().isBlank()) {
            existingUser.setRole(User.Role.valueOf(req.getRole()));
        }

        // Password update
        if (changingPassword) {
            existingUser.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }

        userRepository.save(existingUser);
        return convertToDTO(existingUser);
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

    public Page<UserDTO> searchUsersByFilters(String email, String role, String accountStatus, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);

        // Start with an empty specification
        Specification<User> specification = Specification.where((root, query, criteriaBuilder) -> criteriaBuilder.conjunction());

        // Filter by email
        if (!email.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%")
            );
        }

        // Filter by role
        if (!role.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("role"), User.Role.valueOf(role.toUpperCase()))
            );
        }

        // Filter by account status (if provided)
        if (!accountStatus.isEmpty()) {
            if (accountStatus.equalsIgnoreCase("ACTIVE")) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.isNull(root.get("deletionDate")) // Active users have no deletionDate
                );
            } else if (accountStatus.equalsIgnoreCase("DELETED")) {
                specification = specification.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.isNotNull(root.get("deletionDate")) // Deleted users have a deletionDate
                );
            }
        }

        // Return filtered results as a Page
        return userRepository.findAll(specification, pageable).map(this::convertToDTO);
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