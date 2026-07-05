package ru.practicum.explorewithme.ewmmain.service;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.ewmmain.client.StatsClient;
import ru.practicum.explorewithme.ewmmain.dto.EventFullDto;
import ru.practicum.explorewithme.ewmmain.dto.EventShortDto;
import ru.practicum.explorewithme.ewmmain.dto.LocationDto;
import ru.practicum.explorewithme.ewmmain.dto.NewEventDto;
import ru.practicum.explorewithme.ewmmain.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.ewmmain.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.ewmmain.dto.stats.ViewStats;
import ru.practicum.explorewithme.ewmmain.exception.ConflictException;
import ru.practicum.explorewithme.ewmmain.exception.NotFoundException;
import ru.practicum.explorewithme.ewmmain.mapper.EventMapper;
import ru.practicum.explorewithme.ewmmain.mapper.LocationMapper;
import ru.practicum.explorewithme.ewmmain.model.Category;
import ru.practicum.explorewithme.ewmmain.model.Event;
import ru.practicum.explorewithme.ewmmain.model.EventSort;
import ru.practicum.explorewithme.ewmmain.model.EventState;
import ru.practicum.explorewithme.ewmmain.model.Location;
import ru.practicum.explorewithme.ewmmain.model.RequestStatus;
import ru.practicum.explorewithme.ewmmain.model.User;
import ru.practicum.explorewithme.ewmmain.model.UserAction;
import ru.practicum.explorewithme.ewmmain.repository.CategoryRepository;
import ru.practicum.explorewithme.ewmmain.repository.EventRepository;
import ru.practicum.explorewithme.ewmmain.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.ewmmain.repository.UserRepository;
import ru.practicum.explorewithme.ewmmain.util.EventValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class EventServiceSupport {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ParticipationRequestRepository requestRepository;
    private final StatsClient statsClient;
    private final EventValidator eventValidator;

    public EventServiceSupport(EventRepository eventRepository,
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

    public Event getEventOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
    }

    public void validateEventDate(LocalDateTime eventDate, String rawDate) {
        if (eventDate == null || eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalArgumentException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: " + rawDate);
        }
    }

    public Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException(String.format("Category with id=%d was not found", categoryId)));
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
    }

    public Event createEvent(User user, NewEventDto request) {
        LocalDateTime eventDate = eventValidator.parseDate(request.getEventDate());
        validateEventDate(eventDate, request.getEventDate());
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
        return event;
    }

    public void applyAdminUpdate(Event event, UpdateEventAdminRequest request) {
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
            LocalDateTime eventDate = eventValidator.parseDate(request.getEventDate());
            validateEventDate(eventDate, request.getEventDate());
            if (event.getPublishedOn() != null && eventDate.isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictException("The event date must be at least one hour after publication time");
            }
            event.setEventDate(eventDate);
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (event.getState() != EventState.PENDING) {
                        throw new ConflictException(String.format(
                                "Cannot publish the event because it's not in the right state: %s",
                                event.getState()
                        ));
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState() == EventState.PUBLISHED) {
                        throw new ConflictException("Cannot reject the event because it is already published");
                    }
                    event.setState(EventState.CANCELED);
                }
                default -> throw new IllegalArgumentException(
                        "Unsupported stateAction: " + request.getStateAction()
                );
            }
        }
    }

    public void applyUserUpdate(Event event, UpdateEventUserRequest request) {
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
            LocalDateTime eventDate = eventValidator.parseDate(request.getEventDate());
            validateEventDate(eventDate, request.getEventDate());
            event.setEventDate(eventDate);
        }
        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                default -> throw new IllegalArgumentException("Unsupported stateAction: " + request.getStateAction());
            }
        }
    }

    public EventFullDto toEventFullDto(Event event, Long views) {
        return toEventFullDto(event, getConfirmedRequests(event.getId()), views);
    }

    public EventFullDto toEventFullDto(Event event, long confirmedRequests, Long views) {
        return EventMapper.toFullDto(event, confirmedRequests, views);
    }

    public EventShortDto toEventShortDto(Event event, Long views) {
        return toEventShortDto(event, getConfirmedRequests(event.getId()), views);
    }

    public EventShortDto toEventShortDto(Event event, long confirmedRequests, Long views) {
        return EventMapper.toShortDto(event, confirmedRequests, views);
    }

    public List<EventShortDto> toShortDtos(List<Event> events, Boolean onlyAvailable, EventSort sort, int from, int size) {
        Map<Long, Long> confirmedRequests = getConfirmedRequests(events);
        Map<String, Long> stats = getEventViews(events);
        List<EventShortDto> dtos = events.stream()
                .map(event -> {
                    long requestCount = confirmedRequests.getOrDefault(event.getId(), 0L);
                    EventShortDto dto = toEventShortDto(event, requestCount, stats.getOrDefault(getEventUri(event), 0L));
                    return Boolean.TRUE.equals(onlyAvailable) && !isAvailable(event, requestCount) ? null : dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (sort != null) {
            switch (sort) {
                case EVENT_DATE -> dtos.sort((a, b) -> a.getEventDate().compareTo(b.getEventDate()));
                case VIEWS -> dtos.sort((a, b) -> Long.compare(b.getViews(), a.getViews()));
            }
        }
        return dtos.stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    public long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdsAndStatus(List.of(eventId), RequestStatus.CONFIRMED).stream()
                .findFirst()
                .map(ParticipationRequestRepository.ParticipationRequestCount::getCount)
                .orElse(0L);
    }

    public Map<Long, Long> getConfirmedRequests(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (eventIds.isEmpty()) {
            return Map.of();
        }
        return requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(
                        ParticipationRequestRepository.ParticipationRequestCount::getEventId,
                        ParticipationRequestRepository.ParticipationRequestCount::getCount,
                        Long::sum
                ));
    }

    public long getEventViews(Event event) {
        return getEventViews(List.of(event)).getOrDefault(getEventUri(event), 0L);
    }

    public Map<String, Long> getEventViews(List<Event> events) {
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

    public String getEventUri(Event event) {
        return "/events/" + event.getId();
    }

    private boolean isAvailable(Event event, long confirmedRequests) {
        if (event == null) {
            return false;
        }
        Integer participantLimit = event.getParticipantLimit();
        if (participantLimit == null || participantLimit == 0) {
            return true;
        }
        return confirmedRequests < participantLimit;
    }

    private Location toLocation(LocationDto dto) {
        return LocationMapper.toEntity(dto);
    }
}
