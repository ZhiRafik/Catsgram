package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
        for (User existingUser : users.values()) {
            if (existingUser.getEmail().equals(user.getEmail())) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
        }
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (user.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        for (User existingUser : users.values()) {
            if (existingUser.getEmail().equals(user.getEmail())
                    && existingUser.getId() != user.getId()) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }
        }
        User oldUser = users.get(user.getId());
        if (!(user.getEmail() == null || user.getEmail().isBlank())) {
            oldUser.setEmail(user.getEmail());
        }
        if (!(user.getPassword() == null || user.getPassword().isBlank())) {
            oldUser.setPassword(user.getPassword());
        }
        if (!(user.getUsername() == null || user.getUsername().isBlank())) {
            oldUser.setUsername(user.getUsername());
        }
        return oldUser;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
