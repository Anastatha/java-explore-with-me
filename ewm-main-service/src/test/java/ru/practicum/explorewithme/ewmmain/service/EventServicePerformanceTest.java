package ru.practicum.explorewithme.ewmmain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.ewmmain.client.StatsClient;
import ru.practicum.explorewithme.ewmmain.dto.EventShortDto;
import ru.practicum.explorewithme.ewmmain.model.Event;
import ru.practicum.explorewithme.ewmmain.model.EventState;
import ru.practicum.explorewithme.ewmmain.model.RequestStatus;
import ru.practicum.explorewithme.ewmmain.repository.CategoryRepository;
import ru.practicum.explorewithme.ewmmain.repository.EventRepository;
import ru.practicum.explorewithme.ewmmain.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.ewmmain.repository.UserRepository;
import ru.practicum.explorewithme.ewmmain.util.EventValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServicePerformanceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private StatsClient statsClient;

    @Mock
    private EventValidator eventValidator;

    @Mock
    private EventServiceSupport support;

    @InjectMocks
    private EventService eventService;

    @Test
    void getUserEvents_batchesConfirmedRequestCounts() {
        Event event = new Event();
        event.setId(1L);
        event.setState(EventState.PENDING);
        event.setParticipantLimit(10);
        event.setCreatedOn(LocalDateTime.now());
        event.setEventDate(LocalDateTime.now().plusDays(1));

        ru.practicum.explorewithme.ewmmain.model.User user = new ru.practicum.explorewithme.ewmmain.model.User("u@example.com", "u");

        doNothing().when(eventValidator).validatePaging(any(Integer.class), any(Integer.class));
        when(support.findUser(1L)).thenReturn(user);
        when(eventRepository.findByInitiatorId(any(), any())).thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(event)));
        when(support.getConfirmedRequests(anyList())).thenReturn(Map.of(1L, 3L));
        when(support.toEventShortDto(any(Event.class), any(Long.class), any(Long.class)))
                .thenReturn(new EventShortDto());

        eventService.getUserEvents(1L, 0, 10);

        verify(support).getConfirmedRequests(anyList());
    }
}
