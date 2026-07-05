package ru.practicum.explorewithme.ewmmain.controller.publics;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.ewmmain.client.StatsClient;
import ru.practicum.explorewithme.ewmmain.dto.EventFullDto;
import ru.practicum.explorewithme.ewmmain.dto.EventShortDto;
import ru.practicum.explorewithme.ewmmain.dto.stats.EndpointHit;
import ru.practicum.explorewithme.ewmmain.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventPublicController {
    private final EventService eventService;
    private final StatsClient statsClient;

    public EventPublicController(EventService eventService, StatsClient statsClient) {
        this.eventService = eventService;
        this.statsClient = statsClient;
    }

    @GetMapping
    public List<EventShortDto> getEvents(@RequestParam(value = "text", required = false) String text,
                                         @RequestParam(value = "categories", required = false) List<Long> categories,
                                         @RequestParam(value = "paid", required = false) Boolean paid,
                                         @RequestParam(value = "rangeStart", required = false) String rangeStart,
                                         @RequestParam(value = "rangeEnd", required = false) String rangeEnd,
                                         @RequestParam(value = "onlyAvailable", required = false, defaultValue = "false") Boolean onlyAvailable,
                                         @RequestParam(value = "sort", required = false, defaultValue = "EVENT_DATE") String sort,
                                         @RequestParam(value = "from", defaultValue = "0") int from,
                                         @RequestParam(value = "size", defaultValue = "10") int size,
                                         HttpServletRequest request) {
        List<EventShortDto> response = eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        statsClient.sendHit(new EndpointHit("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now()));
        return response;
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        EventFullDto response = eventService.getPublicEvent(id);
        statsClient.sendHit(new EndpointHit("ewm-main-service", request.getRequestURI(), request.getRemoteAddr(), LocalDateTime.now()));
        return response;
    }
}
