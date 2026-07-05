package ru.practicum.explorewithme.ewmmain.mapper;

import ru.practicum.explorewithme.ewmmain.dto.CategoryDto;
import ru.practicum.explorewithme.ewmmain.dto.EventFullDto;
import ru.practicum.explorewithme.ewmmain.dto.EventShortDto;
import ru.practicum.explorewithme.ewmmain.dto.LocationDto;
import ru.practicum.explorewithme.ewmmain.dto.UserShortDto;
import ru.practicum.explorewithme.ewmmain.model.Category;
import ru.practicum.explorewithme.ewmmain.model.Event;
import ru.practicum.explorewithme.ewmmain.model.Location;
import ru.practicum.explorewithme.ewmmain.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class EventMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private EventMapper() {
    }

    public static EventFullDto toFullDto(Event event, long confirmedRequests, long views) {
        return event == null ? null : new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                toCategoryDto(event.getCategory()),
                confirmedRequests,
                formatDate(event.getCreatedOn()),
                event.getDescription(),
                formatDate(event.getEventDate()),
                toUserShortDto(event.getInitiator()),
                toLocationDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                formatDate(event.getPublishedOn()),
                event.getRequestModeration(),
                event.getState() == null ? null : event.getState().name(),
                event.getTitle(),
                views
        );
    }

    public static EventShortDto toShortDto(Event event, long confirmedRequests, long views) {
        return event == null ? null : new EventShortDto(
                event.getId(),
                event.getAnnotation(),
                toCategoryDto(event.getCategory()),
                confirmedRequests,
                formatDate(event.getEventDate()),
                toUserShortDto(event.getInitiator()),
                event.getPaid(),
                event.getTitle(),
                views
        );
    }

    private static CategoryDto toCategoryDto(Category category) {
        return CategoryMapper.toDto(category);
    }

    private static UserShortDto toUserShortDto(User user) {
        return UserMapper.toShortDto(user);
    }

    private static LocationDto toLocationDto(Location location) {
        return LocationMapper.toDto(location);
    }

    private static String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(FORMATTER);
    }
}
