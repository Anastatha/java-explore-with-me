package ru.practicum.explorewithme.ewmmain.util;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class EventValidator {

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

    public void validateSort(String sort) {
        if (sort == null) {
            return;
        }
        if (!"EVENT_DATE".equals(sort) && !"VIEWS".equals(sort)) {
            throw new IllegalArgumentException("Unsupported sort: " + sort);
        }
    }
}
