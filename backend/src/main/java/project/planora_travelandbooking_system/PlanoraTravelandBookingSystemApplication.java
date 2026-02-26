package project.planora_travelandbooking_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PlanoraTravelandBookingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlanoraTravelandBookingSystemApplication.class, args);
    }

}
