package project.planora_travelandbooking_system.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import project.planora_travelandbooking_system.repository.UserRepository;

@Service
public class DbUserDetailService implements UserDetailsService {

    UserRepository userRepository;

    public DbUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var u = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException(email));

        boolean accountNonLocked = !u.isDeleted();
        boolean enabled = !u.isDeleted();

        String role = u.isSuperAdmin() ? "SUPER_ADMIN" : u.getRole().name();

        return org.springframework.security.core.userdetails.User.builder()
                .username(u.getEmail())
                .password(u.getPassword())
                .roles(role)
                .accountLocked(!accountNonLocked)
                .disabled(!enabled)
                .accountExpired(false)
                .credentialsExpired(false)
                .build();
    }

}