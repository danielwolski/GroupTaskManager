package com.calendarapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.calendarapp.model.Group;
import com.calendarapp.model.User;
import com.calendarapp.rest.user.RestUser;
import com.calendarapp.service.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/group")
    public ResponseEntity<List<RestUser>> getUsersByGroup() {
        log.info("Received get users by group request");
        Group currentUserGroup = userService.getCurrentUserGroup();
        List<User> users = userService.getUsersByGroup(currentUserGroup);
        List<RestUser> restUsers = users.stream()
                .map(user -> {
                    RestUser restUser = new RestUser();
                    restUser.setId(user.getId());
                    restUser.setUsername(user.getUsername());
                    return restUser;
                })
                .toList();
        return ResponseEntity.ok(restUsers);
    }
}

