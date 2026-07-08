package ru.practicum.explorewithme.ewmmain.util;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.ewmmain.model.EventSort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class EventValidator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void validatePaging(int from, int size) {
        if (from < 0) {
            throw new IllegalArgumentException("from must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
    }

    public void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("rangeStart must be earlier than or equal to rangeEnd");
        }
    }

    public void validateSort(EventSort sort) {
        if (sort == null) {
            return;
        }
    }

    public LocalDateTime parseDate(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTime, FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date-time format: " + dateTime);
        }
    }

    public <T> List<T> emptyToNull(List<T> values) {
        return values == null || values.isEmpty() ? null : values;
    }
}
