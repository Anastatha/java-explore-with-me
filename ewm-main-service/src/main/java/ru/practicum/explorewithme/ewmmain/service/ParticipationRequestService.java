package ru.practicum.explorewithme.ewmmain.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.ewmmain.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.ewmmain.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.ewmmain.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.ewmmain.exception.ConflictException;
import ru.practicum.explorewithme.ewmmain.exception.NotFoundException;
import ru.practicum.explorewithme.ewmmain.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.ewmmain.model.Event;
import ru.practicum.explorewithme.ewmmain.model.EventState;
import ru.practicum.explorewithme.ewmmain.model.ParticipationRequest;
import ru.practicum.explorewithme.ewmmain.model.RequestStatus;
import ru.practicum.explorewithme.ewmmain.model.User;
import ru.practicum.explorewithme.ewmmain.repository.EventRepository;
import ru.practicum.explorewithme.ewmmain.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.ewmmain.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParticipationRequestService {
    private final ParticipationRequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ParticipationRequestService(ParticipationRequestRepository requestRepository,
                                       EventRepository eventRepository,
                                       UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = findUser(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        if (Objects.equals(event.getInitiator().getId(), user.getId())) {
            throw new ConflictException("The initiator of the event cannot add a participation request");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in an unpublished event");
        }
        requestRepository.findByEventIdAndRequesterId(eventId, userId).ifPresent(request -> {
            throw new ConflictException("Repeated requests are not allowed");
        });
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }
        RequestStatus status = event.getRequestModeration() && event.getParticipantLimit() != 0 ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        ParticipationRequest participationRequest = new ParticipationRequest(LocalDateTime.now(), event, user, status);
        ParticipationRequest saved = requestRepository.save(participationRequest);
        return ParticipationRequestMapper.toDto(saved);
    }

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        findUser(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id=%d was not found", requestId)));
        if (!Objects.equals(request.getRequester().getId(), userId)) {
            throw new NotFoundException(String.format("Request with id=%d was not found", requestId));
        }
        request.setStatus(RequestStatus.CANCELED);
        return ParticipationRequestMapper.toDto(requestRepository.save(request));
    }

    public List<ParticipationRequestDto> getRequestsForEvent(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        return requestRepository.findByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id=%d was not found", eventId)));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new NotFoundException(String.format("Event with id=%d was not found", eventId));
        }
        RequestStatus desiredStatus;
        try {
            desiredStatus = RequestStatus.valueOf(request.getStatus());
        } catch (Exception ex) {
            throw new ConflictException("Request must have status PENDING");
        }
        if (request.getRequestIds() == null || request.getRequestIds().isEmpty()) {
            return new EventRequestStatusUpdateResult(List.of(), List.of());
        }
        List<ParticipationRequest> requests = requestRepository.findAllById(request.getRequestIds());
        if (requests.size() != request.getRequestIds().size()) {
            throw new NotFoundException("One or more requests were not found");
        }
        for (ParticipationRequest participationRequest : requests) {
            if (participationRequest.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }
        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();
        if (desiredStatus == RequestStatus.CONFIRMED) {
            if (event.getParticipantLimit() != 0 && confirmedCount + requests.size() > event.getParticipantLimit()) {
                throw new ConflictException("The participant limit has been reached");
            }
            for (ParticipationRequest participationRequest : requests) {
                participationRequest.setStatus(RequestStatus.CONFIRMED);
                confirmed.add(ParticipationRequestMapper.toDto(requestRepository.save(participationRequest)));
            }
            confirmedCount += confirmed.size();
            if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
                List<ParticipationRequest> pending = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
                for (ParticipationRequest pendingRequest : pending) {
                    pendingRequest.setStatus(RequestStatus.REJECTED);
                    rejected.add(ParticipationRequestMapper.toDto(requestRepository.save(pendingRequest)));
                }
            }
        } else if (desiredStatus == RequestStatus.REJECTED) {
            for (ParticipationRequest participationRequest : requests) {
                participationRequest.setStatus(RequestStatus.REJECTED);
                rejected.add(ParticipationRequestMapper.toDto(requestRepository.save(participationRequest)));
            }
        } else {
            throw new ConflictException("Only CONFIRMED or REJECTED request status changes are supported");
        }
        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d was not found", userId)));
    }

}
