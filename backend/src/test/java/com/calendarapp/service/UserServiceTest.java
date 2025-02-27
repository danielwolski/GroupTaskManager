package com.calendarapp.service;

import com.calendarapp.exception.UserWithoutGroupException;
import com.calendarapp.model.Group;
import com.calendarapp.model.User;
import com.calendarapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private Group sampleGroup;

    @BeforeEach
    void setUp() {
        sampleGroup = new Group();
        sampleUser = new User();
        sampleUser.setLogin("test@example.com");
        sampleUser.setGroup(sampleGroup);
    }

    @Test
    void shouldReturnCurrentUserGroup() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByLogin("test@example.com")).thenReturn(Optional.of(sampleUser));

        Group result = userService.getCurrentUserGroup();

        assertNotNull(result);
        assertEquals(sampleGroup, result);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("notfound@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByLogin("notfound@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, userService::getCurrentUserGroup);
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserHasNoGroup() {
        sampleUser.setGroup(null);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByLogin("test@example.com")).thenReturn(Optional.of(sampleUser));

        Exception exception = assertThrows(UserWithoutGroupException.class, userService::getCurrentUserGroup);
        assertEquals("User test@example.com does not belong to any group", exception.getMessage());
    }
}
