package ru.practicum.explorewithme.ewmmain.controller.privates;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.ewmmain.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.ewmmain.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.ewmmain.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.ewmmain.service.ParticipationRequestService;

import java.util.List;

@RestController
public class RequestPrivateController {
    private final ParticipationRequestService requestService;

    public RequestPrivateController(ParticipationRequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        return requestService.addParticipationRequest(userId, eventId);
    }

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsForEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return requestService.getRequestsForEvent(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        return requestService.changeRequestStatus(userId, eventId, request);
    }
}
