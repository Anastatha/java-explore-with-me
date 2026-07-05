package ru.practicum.explorewithme.ewmmain.repository;

import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.ewmmain.model.Event;
import ru.practicum.explorewithme.ewmmain.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<Event> searchPublicEvents(EventState state,
                                   String text,
                                   List<Long> categories,
                                   Boolean paid,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   Pageable pageable);

    List<Event> searchAdminEvents(List<Long> users,
                                  List<EventState> states,
                                  List<Long> categories,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Pageable pageable);
}
