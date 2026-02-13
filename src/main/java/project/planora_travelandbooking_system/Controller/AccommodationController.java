package project.planora_travelandbooking_system.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import project.planora_travelandbooking_system.Service.AccommodationService;

@Controller
public class AccommodationController {

    private final AccommodationService accommodationService;

    @Autowired
    public AccommodationController(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

}