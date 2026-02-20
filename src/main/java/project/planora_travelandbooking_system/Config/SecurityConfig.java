package project.planora_travelandbooking_system.Config;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import project.planora_travelandbooking_system.Controller.web.CustomAuthenticationFailureHandler;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import project.planora_travelandbooking_system.Security.JwtFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                      JwtFilter jwtFilter) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(cors -> {})   // Enable CORS for React
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        //ACCOMMODATIONS
                        .requestMatchers(HttpMethod.GET, "/api/accommodation/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/accommodation/**")
                        .hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/accommodation/**")
                        .hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/accommodation/**")
                        .hasAnyRole("ADMIN")

                        //TRIPS
                        .requestMatchers(HttpMethod.GET, "/api/trip/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/trip/**")
                        .hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/trip/**")
                        .hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/trip/**")
                        .hasAnyRole("ADMIN")

                        //TRANSPORTS
                        .requestMatchers(HttpMethod.GET, "/api/transports/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/transports/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/transports/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/transports/**")
                        .hasRole("ADMIN")
                        
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/user/restore").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .formLogin(form -> form.disable())   // important
                .logout(logout -> logout.disable()); // optional (API usually no logout)

        return http.build();
    }

    @Bean
    @org.springframework.core.annotation.Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        RequestMatcher notApi = request -> !request.getRequestURI().startsWith("/api/");

        http
                .securityMatcher(notApi) // ignore /api
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/logout")
                        .ignoringRequestMatchers("/h2-console/**")
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // allow Postman for API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/favicon.ico"
                        ).permitAll()

                        //for JWT auth
                        //.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        //.requestMatchers(HttpMethod.POST, "/api/user/restore").permitAll()
                        .requestMatchers("/login", "/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/signup").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // after login
                        .requestMatchers(HttpMethod.GET, "/bookings").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/bookings/save").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/bookings/edit/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/bookings/delete/*").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

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

                        .requestMatchers(HttpMethod.GET, "/user").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST,"/user/edit").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST,"/user/restore").hasAnyRole("USER", "ADMIN")


                        .anyRequest().authenticated()

                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .successHandler(customAuthenticationSuccessHandler())
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
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

            response.sendRedirect("user");
        };
    }
}
