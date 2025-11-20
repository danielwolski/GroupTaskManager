package com.grouptaskmanager.auth.validator;

import com.grouptaskmanager.exception.EmailAlreadyExistsException;
import com.grouptaskmanager.repository.UserRepository;
import com.grouptaskmanager.auth.model.RegisterRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegistrationValidator {

    private final UserRepository userRepository;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";

    public void validate(RegisterRequest registerRequest) {
         validateUsernameUniqueness(registerRequest.getUsername());
         validateLoginUniqueness(registerRequest.getLogin());
    }

    private void validateUsernameUniqueness(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new EmailAlreadyExistsException("Username " + username + " is already in use");
        }
    }

    private void validateLoginUniqueness(String login) {
        if (userRepository.existsByLogin(login)) {
            throw new EmailAlreadyExistsException("Login " + login + " is already in use");
        }
    }
}
