package ru.practicum.explorewithme.ewmmain.mapper;

import ru.practicum.explorewithme.ewmmain.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.ewmmain.model.ParticipationRequest;

import java.time.format.DateTimeFormatter;

public final class ParticipationRequestMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ParticipationRequestMapper() {
    }

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return request == null ? null : new ParticipationRequestDto(
                request.getId(),
                request.getCreated() == null ? null : request.getCreated().format(FORMATTER),
                request.getEvent() == null ? null : request.getEvent().getId(),
                request.getRequester() == null ? null : request.getRequester().getId(),
                request.getStatus() == null ? null : request.getStatus().name()
        );
    }
}
