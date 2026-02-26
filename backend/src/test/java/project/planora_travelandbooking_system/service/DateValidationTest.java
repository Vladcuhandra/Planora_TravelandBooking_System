package project.planora_travelandbooking_system.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class DateValidationTest {

    @Test
    void endNotBeforeStart_nullDates_throw() {
        assertThatThrownBy(() -> DateValidation.endNotBeforeStart(null, LocalDateTime.now(), "start", "end"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void endNotBeforeStart_endBeforeStart_throw() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 9, 10, 0);

        assertThatThrownBy(() -> DateValidation.endNotBeforeStart(start, end, "startDate", "endDate"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("endDate cannot be before startDate");
    }

    @Test
    void endNotBeforeStart_valid_ok() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 11, 10, 0);

        assertThatCode(() -> DateValidation.endNotBeforeStart(start, end, "startDate", "endDate"))
                .doesNotThrowAnyException();
    }
}