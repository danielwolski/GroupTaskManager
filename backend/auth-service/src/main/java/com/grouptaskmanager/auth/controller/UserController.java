package com.grouptaskmanager.auth.controller;

import com.grouptaskmanager.auth.dto.UserDto;
import com.grouptaskmanager.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        log.info("Get current user request");
        return ResponseEntity.ok(userService.getCurrentUserDto());
    }

    @GetMapping("/group")
    public ResponseEntity<List<UserDto>> getAllUsersInGroup() {
        log.info("Get all users in group request");
        return ResponseEntity.ok(userService.getAllUsersInCurrentGroup());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Get user by id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/by-login/{login}")
    public ResponseEntity<UserDto> getUserByLogin(@PathVariable String login) {
        log.info("Get user by login: {}", login);
        return ResponseEntity.ok(userService.getUserByLogin(login));
    }
}

