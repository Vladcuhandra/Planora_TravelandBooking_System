package project.planora_travelandbooking_system.Controller.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import project.planora_travelandbooking_system.Model.User;
import project.planora_travelandbooking_system.Repository.UserRepository;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String email = Optional.ofNullable(request.getParameter("email")).orElse("").trim();

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent() && userOptional.get().isDeleted()) {

            response.sendRedirect("/login?restore=true&email=" +
                    URLEncoder.encode(email, "UTF-8"));
            return;
        }

        response.sendRedirect("/login?error=true&email=" +
                URLEncoder.encode(email, "UTF-8"));
    }

}