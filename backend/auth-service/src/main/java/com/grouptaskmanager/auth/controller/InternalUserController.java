package com.grouptaskmanager.auth.controller;

import com.grouptaskmanager.auth.dto.UserDto;
import com.grouptaskmanager.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Internal API for inter-service communication.
 * Should only be accessible from other services, not from the gateway.
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Internal: Get user by id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/by-login/{login}")
    public ResponseEntity<UserDto> getUserByLogin(@PathVariable String login) {
        log.info("Internal: Get user by login: {}", login);
        return ResponseEntity.ok(userService.getUserByLogin(login));
    }

    @GetMapping("/by-group/{groupId}")
    public ResponseEntity<List<UserDto>> getUsersByGroupId(@PathVariable Long groupId) {
        log.info("Internal: Get users by group id: {}", groupId);
        return ResponseEntity.ok(userService.getUsersByGroupId(groupId));
    }
}

