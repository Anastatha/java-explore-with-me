package ru.practicum.explorewithme.ewmmain.mapper;

import ru.practicum.explorewithme.ewmmain.dto.UserDto;
import ru.practicum.explorewithme.ewmmain.dto.UserShortDto;
import ru.practicum.explorewithme.ewmmain.model.User;

public final class UserMapper {
    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return user == null ? null : new UserDto(user.getId(), user.getEmail(), user.getName());
    }

    public static UserShortDto toShortDto(User user) {
        return user == null ? null : new UserShortDto(user.getId(), user.getName());
    }
}
