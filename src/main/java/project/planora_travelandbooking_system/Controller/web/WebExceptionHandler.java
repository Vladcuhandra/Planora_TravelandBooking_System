package project.planora_travelandbooking_system.Controller.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(basePackages = "project.planora_travelandbooking_system.Controller.web")
public class WebExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request,
                                        RedirectAttributes ra){
        ra.addFlashAttribute("error", ex.getMessage());
        ra.addFlashAttribute("openCreate", true);

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    };
}
