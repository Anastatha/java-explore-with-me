package ru.practicum.explorewithme.ewmmain.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.ewmmain.dto.NewUserRequest;
import ru.practicum.explorewithme.ewmmain.dto.UserDto;
import ru.practicum.explorewithme.ewmmain.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class UserAdminController {
    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(value = "ids", required = false) List<Long> ids,
                                  @RequestParam(value = "from", defaultValue = "0") int from,
                                  @RequestParam(value = "size", defaultValue = "10") int size) {
        return userService.getUsers(ids, from, size);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Validated @RequestBody NewUserRequest request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
