package com.grouptaskmanager.auth.validator;

import com.grouptaskmanager.auth.dto.RegisterRequest;
import com.grouptaskmanager.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RegistrationValidator {

    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public void validate(RegisterRequest request) {
        if (request.getLogin() == null || request.getLogin().isBlank()) {
            throw new RuntimeException("Login cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(request.getLogin()).matches()) {
            throw new RuntimeException("Invalid email format");
        }

        if (userRepository.existsByLogin(request.getLogin())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("Username cannot be empty");
        }

        if (request.getGroupPasscode() == null || request.getGroupPasscode().isBlank()) {
            throw new RuntimeException("Group passcode cannot be empty");
        }
    }
}

