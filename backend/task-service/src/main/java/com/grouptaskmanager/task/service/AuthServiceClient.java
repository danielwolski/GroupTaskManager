package com.grouptaskmanager.task.service;

import com.grouptaskmanager.task.dto.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class AuthServiceClient {

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public AuthServiceClient(@Value("${services.auth-service.url}") String authServiceUrl) {
        this.authServiceUrl = authServiceUrl;
        this.restTemplate = new RestTemplate();
    }

    public UserDto getUserByLogin(String login) {
        try {
            String url = authServiceUrl + "/api/internal/users/by-login/" + login;
            return restTemplate.getForObject(url, UserDto.class);
        } catch (Exception e) {
            log.error("Failed to get user by login: {}", login, e);
            throw new RuntimeException("Failed to get user from auth service", e);
        }
    }

    public UserDto getUserById(Long id) {
        try {
            String url = authServiceUrl + "/api/internal/users/" + id;
            return restTemplate.getForObject(url, UserDto.class);
        } catch (Exception e) {
            log.error("Failed to get user by id: {}", id, e);
            throw new RuntimeException("Failed to get user from auth service", e);
        }
    }

    public List<UserDto> getUsersByGroupId(Long groupId) {
        try {
            String url = authServiceUrl + "/api/internal/users/by-group/" + groupId;
            UserDto[] users = restTemplate.getForObject(url, UserDto[].class);
            return users != null ? Arrays.asList(users) : Collections.emptyList();
        } catch (Exception e) {
            log.error("Failed to get users by group id: {}", groupId, e);
            throw new RuntimeException("Failed to get users from auth service", e);
        }
    }
}

