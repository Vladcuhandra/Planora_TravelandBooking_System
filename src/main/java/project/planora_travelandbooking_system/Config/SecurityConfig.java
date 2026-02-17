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
                        .ignoringRequestMatchers("/logout")
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/login", "/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/signup").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // after login
                        .requestMatchers(HttpMethod.GET, "/api/bookings").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/bookings/save").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/bookings/edit/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/bookings/delete/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/admin").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Transports UI: USER read, ADMIN manage
                        .requestMatchers(HttpMethod.GET, "/transports").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/transports/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/transports/save").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/transports/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/transports/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/transports/*").hasRole("ADMIN")

                        // Accommodations UI: USER read, ADMIN manage
                        .requestMatchers(HttpMethod.GET, "/accommodations").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/accommodations/new").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/accommodations/save").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/accommodations/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/accommodations/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/accommodations/*").hasRole("ADMIN")

                        // Trips UI: USER + ADMIN (service restricts to own trips for USER)
                        .requestMatchers(HttpMethod.GET, "/trips").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/trips/new").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/trips/save").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/trips/*/edit").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/trips/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/trips/*").hasAnyRole("USER", "ADMIN")

                        .anyRequest().authenticated()

                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
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
    CommandLineRunner createAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRole(User.Role.ADMIN);
                admin.setSuperAdmin(true);
                userRepository.save(admin);
            }
        };
    }

    // redirect HTTP -> HTTPS
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
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

        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        tomcat.addAdditionalConnectors(connector);

        return tomcat;
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
}
