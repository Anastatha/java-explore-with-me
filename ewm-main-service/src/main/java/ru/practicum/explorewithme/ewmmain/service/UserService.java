package ru.practicum.explorewithme.ewmmain.service;

import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.ewmmain.dto.NewUserRequest;
import ru.practicum.explorewithme.ewmmain.dto.UserDto;
import ru.practicum.explorewithme.ewmmain.exception.ConflictException;
import ru.practicum.explorewithme.ewmmain.exception.NotFoundException;
import ru.practicum.explorewithme.ewmmain.mapper.UserMapper;
import ru.practicum.explorewithme.ewmmain.model.User;
import ru.practicum.explorewithme.ewmmain.repository.UserRepository;
import ru.practicum.explorewithme.ewmmain.util.EventValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final EventValidator eventValidator;

    public UserService(UserRepository userRepository,
                       EventValidator eventValidator) {
        this.userRepository = userRepository;
        this.eventValidator = eventValidator;
    }

    public UserDto createUser(NewUserRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            throw new ConflictException("Email is already registered");
        });
        User user = new User(request.getEmail(), request.getName());
        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        eventValidator.validatePaging(from, size);
        List<User> users = ids == null || ids.isEmpty()
                ? userRepository.findAll()
                : userRepository.findAllById(ids);
        return users.stream()
                .skip(from)
                .limit(size)
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with id=%d was not found", userId));
        }
        userRepository.deleteById(userId);
    }

}
