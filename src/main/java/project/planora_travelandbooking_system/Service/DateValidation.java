package project.planora_travelandbooking_system.Service;

import java.time.LocalDateTime;
import java.util.Date;

public final class DateValidation {
    private DateValidation() {
        // Private constructor to prevent instantiation
    }

    public static void endNotBeforeStart(LocalDateTime startTime, LocalDateTime endTime, String startField, String endField){
        if(startTime == null || endTime == null){
            throw new IllegalArgumentException(startField + " and " + endField + "must not be null");
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException(endField + " cannot be before " + startField);
        }
    }
}
