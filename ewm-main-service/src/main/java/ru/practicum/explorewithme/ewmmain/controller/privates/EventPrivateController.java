package ru.practicum.explorewithme.ewmmain.controller.privates;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.ewmmain.dto.EventFullDto;
import ru.practicum.explorewithme.ewmmain.dto.EventShortDto;
import ru.practicum.explorewithme.ewmmain.dto.NewEventDto;
import ru.practicum.explorewithme.ewmmain.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.ewmmain.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
public class EventPrivateController {
    private final EventService eventService;

    public EventPrivateController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                             @RequestParam(value = "from", defaultValue = "0") int from,
                                             @RequestParam(value = "size", defaultValue = "10") int size) {
        return eventService.getUserEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId, @Validated @RequestBody NewEventDto request) {
        return eventService.createUserEvent(userId, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody UpdateEventUserRequest request) {
        return eventService.updateUserEvent(userId, eventId, request);
    }
}
