package ru.practicum.explorewithme.ewmmain.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.ewmmain.dto.EventFullDto;
import ru.practicum.explorewithme.ewmmain.dto.EventShortDto;
import ru.practicum.explorewithme.ewmmain.dto.NewEventDto;
import ru.practicum.explorewithme.ewmmain.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.ewmmain.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.ewmmain.exception.NotFoundException;
import ru.practicum.explorewithme.ewmmain.model.Event;
import ru.practicum.explorewithme.ewmmain.model.EventSort;
import ru.practicum.explorewithme.ewmmain.model.EventState;
import ru.practicum.explorewithme.ewmmain.model.User;
import ru.practicum.explorewithme.ewmmain.repository.EventRepository;
import ru.practicum.explorewithme.ewmmain.util.EventValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {
    private final EventRepository eventRepository;
    private final EventValidator eventValidator;
    private final EventServiceSupport support;

    public EventService(EventRepository eventRepository,
                        EventValidator eventValidator,
                        EventServiceSupport support) {
        this.eventRepository = eventRepository;
        this.eventValidator = eventValidator;
        this.support = support;
    }

    public List<EventFullDto> getEventsForAdmin(List<Long> users,
                                                List<String> states,
                                                List<Long> categories,
                                                String rangeStart,
                                                String rangeEnd,
                                                int from,
                                                int size) {
        eventValidator.validatePaging(from, size);
        List<EventState> stateFilters = parseStates(states);
        LocalDateTime start = eventValidator.parseDate(rangeStart);
        LocalDateTime end = eventValidator.parseDate(rangeEnd);
        eventValidator.validateDateRange(start, end);
        Pageable pageable = PageRequest.of(0, Math.max(from + size, 1));
        List<Event> events = eventRepository.searchAdminEvents(
                eventValidator.emptyToNull(users),
                stateFilters,
                eventValidator.emptyToNull(categories),
                start,
                end,
                pageable);
        Map<Long, Long> confirmedRequests = support.getConfirmedRequests(events);
        return events.stream()
                .skip(from)
                .limit(size)
                .map(event -> support.toEventFullDto(event, confirmedRequests.getOrDefault(event.getId(), 0L), 0L))
                .collect(Collectors.toList());
    }

    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = support.getEventOrThrow(eventId);
        support.applyAdminUpdate(event, request);
        return support.toEventFullDto(eventRepository.save(event), support.getEventViews(event));
    }

    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               String rangeStart,
                                               String rangeEnd,
                                               Boolean onlyAvailable,
                                               EventSort sort,
                                               int from,
                                               int size) {
        eventValidator.validatePaging(from, size);
        eventValidator.validateSort(sort);
        LocalDateTime start = eventValidator.parseDate(rangeStart);
        LocalDateTime end = eventValidator.parseDate(rangeEnd);
        eventValidator.validateDateRange(start, end);
        if (start == null && end == null) {
            start = LocalDateTime.now();
        }
        String normalizedText = (text == null || text.isBlank()) ? null : text;
        Pageable pageable = PageRequest.of(0, Math.max(from + size, 1));
        List<Event> events = eventRepository.searchPublicEvents(
                EventState.PUBLISHED,
                normalizedText,
                eventValidator.emptyToNull(categories),
                paid,
                start,
                end,
                pageable);
        List<EventShortDto> dtos = support.toShortDtos(events, onlyAvailable, sort, from, size);
        if (sort == EventSort.VIEWS) {
            dtos.sort((a, b) -> Long.compare(b.getViews(), a.getViews()));
        }
        return dtos;
    }

    public EventFullDto getPublicEvent(Long eventId) {
        Event event = support.getEventOrThrow(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        return support.toEventFullDto(event, support.getEventViews(event));
    }

    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        eventValidator.validatePaging(from, size);
        User user = support.findUser(userId);
        Pageable pageable = createPageable(from, size);
        List<Event> events = eventRepository.findByInitiatorId(user.getId(), pageable).getContent();
        Map<Long, Long> confirmedRequests = support.getConfirmedRequests(events);
        return events.stream()
                .skip(from)
                .limit(size)
                .map(event -> support.toEventShortDto(event, confirmedRequests.getOrDefault(event.getId(), 0L), 0L))
                .collect(Collectors.toList());
    }

    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = support.getEventOrThrow(eventId);
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        return support.toEventFullDto(event, support.getEventViews(event));
    }

    public EventFullDto createUserEvent(Long userId, NewEventDto request) {
        User user = support.findUser(userId);
        Event created = eventRepository.save(support.createEvent(user, request));
        return support.toEventFullDto(created, 0L);
    }

    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = support.getEventOrThrow(eventId);
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        if (event.getState() == EventState.PUBLISHED) {
            throw new ru.practicum.explorewithme.ewmmain.exception.ConflictException("Event must not be published");
        }
        support.applyUserUpdate(event, request);
        return support.toEventFullDto(eventRepository.save(event), support.getEventViews(event));
    }

    private Pageable createPageable(int from, int size) {
        return PageRequest.of(0, Math.max(from + size, 1));
    }

    private List<EventState> parseStates(List<String> states) {
        if (states == null || states.isEmpty()) {
            return null;
        }
        return states.stream()
                .map(state -> {
                    try {
                        return EventState.valueOf(state);
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException("Unsupported state: " + state);
                    }
                })
                .collect(Collectors.toList());
    }
}
