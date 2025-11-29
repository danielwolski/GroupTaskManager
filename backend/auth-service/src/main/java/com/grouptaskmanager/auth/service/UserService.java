package com.grouptaskmanager.auth.service;

import com.grouptaskmanager.auth.dto.UserDto;
import com.grouptaskmanager.auth.model.User;
import com.grouptaskmanager.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDto getCurrentUserDto() {
        User user = getCurrentUser();
        return mapToDto(user);
    }

    public UserDto getUserByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found: " + login));
        return mapToDto(user);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
        return mapToDto(user);
    }

    public List<UserDto> getUsersByGroupId(Long groupId) {
        return userRepository.findAllByGroupId(groupId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getAllUsersInCurrentGroup() {
        User currentUser = getCurrentUser();
        if (currentUser.getGroup() == null) {
            throw new RuntimeException("User is not in any group");
        }
        return getUsersByGroupId(currentUser.getGroup().getId());
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getTrueUsername())
                .login(user.getLogin())
                .groupId(user.getGroup() != null ? user.getGroup().getId() : null)
                .groupPasscode(user.getGroup() != null ? user.getGroup().getPasscode() : null)
                .userRole(user.getUserRole().name())
                .build();
    }
}

