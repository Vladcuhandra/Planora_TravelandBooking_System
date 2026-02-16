package project.planora_travelandbooking_system.Config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .authorizeHttpRequests(auth -> auth

                        // Public pages
                        .requestMatchers("/login", "/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/signup").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()

                        // Admin dashboard
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Bookings page (after login)
                        .requestMatchers("/api/bookings").hasAnyRole("USER", "ADMIN")

                        // =============================
                        // TRANSPORTS SECURITY
                        // =============================

                        // USER can see transports
                        .requestMatchers(HttpMethod.GET, "/transports")
                        .hasAnyRole("USER", "ADMIN")

                        // ADMIN can create/edit/delete transports
                        .requestMatchers(HttpMethod.GET, "/transports/new")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/transports/save")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/transports/*/edit")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/transports/*")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/transports/*")
                        .hasRole("ADMIN")

                        // =============================
                        // ACCOMMODATIONS SECURITY
                        // =============================

                        // USER can see accommodations
                        .requestMatchers(HttpMethod.GET, "/accommodations")
                        .hasAnyRole("USER", "ADMIN")

                        // ADMIN can create/edit/delete accommodations
                        .requestMatchers(HttpMethod.GET, "/accommodations/new")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/accommodations/save")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/accommodations/*/edit")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/accommodations/*")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.DELETE, "/accommodations/*")
                        .hasRole("ADMIN")

                        // Everything else requires login
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .successHandler(customAuthenticationSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner createAdmin(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String role = authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .findFirst()
                    .orElse("");

            if ("ROLE_ADMIN".equals(role)) {
                response.sendRedirect("/api/admin");
            } else {
                response.sendRedirect("/api/bookings");
            }
        };
    }

    // HTTP → HTTPS redirect
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat =
                new TomcatServletWebServerFactory() {
                    @Override
                    protected void postProcessContext(Context context) {
                        SecurityConstraint constraint = new SecurityConstraint();
                        constraint.setUserConstraint("CONFIDENTIAL");

                        SecurityCollection collection = new SecurityCollection();
                        collection.addPattern("/*");

                        constraint.addCollection(collection);
                        context.addConstraint(constraint);
                    }
                };

        Connector connector =
                new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);

        tomcat.addAdditionalConnectors(connector);

        return tomcat;
    }
}
