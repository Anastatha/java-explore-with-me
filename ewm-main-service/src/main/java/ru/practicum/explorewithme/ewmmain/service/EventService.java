package ru.practicum.explorewithme.ewmmain.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.ewmmain.client.StatsClient;
import ru.practicum.explorewithme.ewmmain.dto.EventFullDto;
import ru.practicum.explorewithme.ewmmain.dto.EventShortDto;
import ru.practicum.explorewithme.ewmmain.dto.LocationDto;
import ru.practicum.explorewithme.ewmmain.dto.NewEventDto;
import ru.practicum.explorewithme.ewmmain.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.ewmmain.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.ewmmain.exception.ConflictException;
import ru.practicum.explorewithme.ewmmain.exception.NotFoundException;
import ru.practicum.explorewithme.ewmmain.mapper.EventMapper;
import ru.practicum.explorewithme.ewmmain.mapper.LocationMapper;
import ru.practicum.explorewithme.ewmmain.model.Category;
import ru.practicum.explorewithme.ewmmain.model.Event;
import ru.practicum.explorewithme.ewmmain.model.EventState;
import ru.practicum.explorewithme.ewmmain.model.Location;
import ru.practicum.explorewithme.ewmmain.model.RequestStatus;
import ru.practicum.explorewithme.ewmmain.model.User;
import ru.practicum.explorewithme.ewmmain.repository.CategoryRepository;
import ru.practicum.explorewithme.ewmmain.repository.EventRepository;
import ru.practicum.explorewithme.ewmmain.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.ewmmain.repository.UserRepository;
import ru.practicum.explorewithme.ewmmain.dto.stats.ViewStats;
import ru.practicum.explorewithme.ewmmain.util.EventValidator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsClient statsClient;
    private final EventValidator eventValidator;

    public EventService(EventRepository eventRepository,
                        UserRepository userRepository,
                        CategoryRepository categoryRepository,
                        ParticipationRequestRepository requestRepository,
                        StatsClient statsClient,
                        EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;
        this.statsClient = statsClient;
        this.eventValidator = eventValidator;
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
        LocalDateTime start = parseDate(rangeStart);
        LocalDateTime end = parseDate(rangeEnd);
        eventValidator.validateDateRange(start, end);
        Pageable pageable = PageRequest.of(0, Math.max(from + size, 1));
        List<Event> events = eventRepository.searchAdminEvents(
                emptyToNull(users),
                stateFilters,
                emptyToNull(categories),
                start,
                end,
                pageable);
        return events.stream()
                .skip(from)
                .limit(size)
                .map(event -> toEventFullDto(event, 0L))
                .collect(Collectors.toList());
    }

    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        if (request.getCategory() != null) {
            event.setCategory(findCategory(request.getCategory()));
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(toLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getEventDate() != null) {
            LocalDateTime eventDate = parseDate(request.getEventDate());
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new IllegalArgumentException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + request.getEventDate());
            }
            if (event.getPublishedOn() != null && eventDate.isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictException("The event date must be at least one hour after publication time");
            }
            event.setEventDate(eventDate);
        }
        if (request.getStateAction() != null) {
            if ("PUBLISH_EVENT".equals(request.getStateAction())) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException(String.format("Cannot publish the event because it's not in the right state: %s", event.getState()));
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if ("REJECT_EVENT".equals(request.getStateAction())) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject the event because it is already published");
                }
                event.setState(EventState.CANCELED);
            } else {
                throw new IllegalArgumentException("Unsupported stateAction: " + request.getStateAction());
            }
        }
        return toEventFullDto(eventRepository.save(event), getEventViews(event));
    }

    public List<EventShortDto> getPublicEvents(String text,
                                               List<Long> categories,
                                               Boolean paid,
                                               String rangeStart,
                                               String rangeEnd,
                                               Boolean onlyAvailable,
                                               String sort,
                                               int from,
                                               int size) {
        eventValidator.validatePaging(from, size);
        eventValidator.validateSort(sort);
        LocalDateTime start = parseDate(rangeStart);
        LocalDateTime end = parseDate(rangeEnd);
        eventValidator.validateDateRange(start, end);
        if (start == null && end == null) {
            start = LocalDateTime.now();
        }
        String normalizedText = (text == null || text.isBlank()) ? null : text;
        Pageable pageable = PageRequest.of(0, Math.max(from + size, 1));
        List<Event> events = eventRepository.searchPublicEvents(
                EventState.PUBLISHED,
                normalizedText,
                emptyToNull(categories),
                paid,
                start,
                end,
                pageable);
        List<EventShortDto> dtos = toShortDtos(events, onlyAvailable, sort, from, size);
        if ("VIEWS".equals(sort)) {
            dtos.sort((a, b) -> Long.compare(b.getViews(), a.getViews()));
        }
        return dtos;
    }

    public EventFullDto getPublicEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        return toEventFullDto(event, getEventViews(event));
    }

    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        eventValidator.validatePaging(from, size);
        User user = findUser(userId);
        Pageable pageable = PageRequest.of(0, Math.max(from + size, 1));
        return eventRepository.findByInitiatorId(user.getId(), pageable).stream()
                .skip(from)
                .limit(size)
                .map(event -> toEventShortDto(event, 0L))
                .collect(Collectors.toList());
    }

    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        return toEventFullDto(event, getEventViews(event));
    }

    public EventFullDto createUserEvent(Long userId, NewEventDto request) {
        User user = findUser(userId);
        LocalDateTime eventDate = parseDate(request.getEventDate());
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalArgumentException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + request.getEventDate());
        }
        Event event = new Event();
        event.setAnnotation(request.getAnnotation());
        event.setCategory(findCategory(request.getCategory()));
        event.setDescription(request.getDescription());
        event.setEventDate(eventDate);
        event.setInitiator(user);
        event.setTitle(request.getTitle());
        event.setLocation(toLocation(request.getLocation()));
        event.setPaid(request.getPaid() != null ? request.getPaid() : false);
        event.setParticipantLimit(request.getParticipantLimit() != null ? request.getParticipantLimit() : 0);
        event.setRequestModeration(request.getRequestModeration() != null ? request.getRequestModeration() : true);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        Event created = eventRepository.save(event);
        return toEventFullDto(created, 0L);
    }

    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Event must not be published");
        }
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(findCategory(request.getCategory()));
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(toLocation(request.getLocation()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        if (request.getEventDate() != null) {
            LocalDateTime eventDate = parseDate(request.getEventDate());
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new IllegalArgumentException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + request.getEventDate());
            }
            event.setEventDate(eventDate);
        }
        if (request.getStateAction() != null) {
            if ("SEND_TO_REVIEW".equals(request.getStateAction())) {
                event.setState(EventState.PENDING);
            } else if ("CANCEL_REVIEW".equals(request.getStateAction())) {
                event.setState(EventState.CANCELED);
            } else {
                throw new IllegalArgumentException("Unsupported stateAction: " + request.getStateAction());
            }
        }
        return toEventFullDto(eventRepository.save(event), getEventViews(event));
    }

    private List<EventShortDto> toShortDtos(List<Event> events, Boolean onlyAvailable, String sort, int from, int size) {
        Map<String, Long> stats = getEventViews(events);
        List<EventShortDto> dtos = events.stream()
                .map(event -> toEventShortDto(event, stats.getOrDefault(getEventUri(event), 0L)))
                .filter(eventShortDto -> !Boolean.TRUE.equals(onlyAvailable) || isAvailable(eventShortDto))
                .collect(Collectors.toList());
        if ("EVENT_DATE".equals(sort)) {
            dtos.sort((a, b) -> a.getEventDate().compareTo(b.getEventDate()));
        }
        return dtos.stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    private boolean isAvailable(EventShortDto eventShortDto) {
        if (eventShortDto == null) {
            return false;
        }
        Event event = eventRepository.findById(eventShortDto.getId()).orElse(null);
        if (event == null) {
            return false;
        }
        Integer participantLimit = event.getParticipantLimit();
        if (participantLimit == null || participantLimit == 0) {
            return true;
        }
        return eventShortDto.getConfirmedRequests() < participantLimit;
    }

    private String getEventUri(Event event) {
        return "/events/" + event.getId();
    }

    private EventFullDto toEventFullDto(Event event, Long views) {
        return EventMapper.toFullDto(event, getConfirmedRequests(event.getId()), views);
    }

    private EventShortDto toEventShortDto(Event event, Long views) {
        return EventMapper.toShortDto(event, getConfirmedRequests(event.getId()), views);
    }

    private Location toLocation(LocationDto dto) {
        return LocationMapper.toEntity(dto);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", categoryId)));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
    }

    private Long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private long getEventViews(Event event) {
        return getEventViews(List.of(event)).getOrDefault(getEventUri(event), 0L);
    }

    private Map<String, Long> getEventViews(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }
        List<String> uris = events.stream()
                .map(this::getEventUri)
                .collect(Collectors.toList());
        try {
            List<ViewStats> stats = statsClient.getStats(LocalDateTime.now().minusYears(5), LocalDateTime.now().plusYears(5), uris, true);
            return stats.stream().collect(Collectors.toMap(ViewStats::getUri, ViewStats::getHits, Long::sum));
        } catch (RuntimeException ex) {
            return new HashMap<>();
        }
    }

    private LocalDateTime parseDate(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTime, FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date-time format: " + dateTime);
        }
    }

    private List<EventState> parseStates(List<String> states) {
        if (states == null || states.isEmpty()) {
            return null;
        }
        List<EventState> eventStates = new ArrayList<>();
        for (String state : states) {
            try {
                eventStates.add(EventState.valueOf(state));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported state: " + state);
            }
        }
        return eventStates.isEmpty() ? null : eventStates;
    }

    private <T> List<T> emptyToNull(List<T> values) {
        return values == null || values.isEmpty() ? null : values;
    }
}
