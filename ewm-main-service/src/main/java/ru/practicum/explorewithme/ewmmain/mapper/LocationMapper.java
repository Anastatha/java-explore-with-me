package ru.practicum.explorewithme.ewmmain.mapper;

import ru.practicum.explorewithme.ewmmain.dto.LocationDto;
import ru.practicum.explorewithme.ewmmain.model.Location;

public final class LocationMapper {
    private LocationMapper() {
    }

    public static LocationDto toDto(Location location) {
        return location == null ? null : new LocationDto(location.getLat(), location.getLon());
    }

    public static Location toEntity(LocationDto dto) {
        return dto == null ? null : new Location(dto.getLat(), dto.getLon());
    }
}
