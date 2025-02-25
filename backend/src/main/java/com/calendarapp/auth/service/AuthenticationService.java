package com.calendarapp.auth.service;


import com.calendarapp.auth.model.AuthenticationRequest;
import com.calendarapp.auth.model.AuthenticationResponse;
import com.calendarapp.auth.model.Token;
import com.calendarapp.auth.model.TokenType;
import com.calendarapp.auth.validator.RegistrationValidator;
import com.calendarapp.model.Group;
import com.calendarapp.model.UserRole;
import com.calendarapp.repository.GroupRepository;
import com.calendarapp.repository.TokenRepository;
import com.calendarapp.repository.UserRepository;
import com.calendarapp.auth.model.RegisterRequest;
import com.calendarapp.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final TokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final RegistrationValidator registrationValidator;

  public AuthenticationResponse register(RegisterRequest registerRequest) {
    registrationValidator.validate(registerRequest);

    User user = new User();
    user.setUsername(registerRequest.getUsername());
    user.setLogin(registerRequest.getLogin());
    user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
    user.setUserRole(UserRole.USER);

    Optional<Group> optGroup = groupRepository.findByPasscode(registerRequest.getGroupPasscode());
    Group group;
    if (optGroup.isPresent()) {
      group = optGroup.get();
    } else {
      group = new Group();
      group.setPasscode(registerRequest.getGroupPasscode());
      group.setUsers(new ArrayList<>());
      group.setTasks(new ArrayList<>());
      group.setDailyTasks(new ArrayList<>());
      group.setEvents(new ArrayList<>());
      group = groupRepository.save(group);
    }
    user.setGroup(group);
    group.getUsers().add(user);

    var savedUser = userRepository.save(user);
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(savedUser, jwtToken);
    return AuthenticationResponse.builder()
            .accessToken(jwtToken)
            .refreshToken(refreshToken)
            .build();
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) {
    authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                      request.getEmail(),
                      request.getPassword()
              )
    );

    var user = userRepository.findByLogin(request.getEmail())
        .orElseThrow();
    var jwtToken = jwtService.generateToken(user);
    var refreshToken = jwtService.generateRefreshToken(user);
    revokeAllUserTokens(user);
    saveUserToken(user, jwtToken);
    return AuthenticationResponse.builder()
        .accessToken(jwtToken)
            .refreshToken(refreshToken)
            .username(user.getTrueUsername())
        .build();
  }

  private void saveUserToken(User user, String jwtToken) {
    var token = Token.builder()
        .user(user)
        .token(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenRepository.save(token);
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
    if (validUserTokens.isEmpty())
      return;

    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenRepository.saveAll(validUserTokens);
  }

  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String userEmail;

    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }

    refreshToken = authHeader.substring(7);
    userEmail = jwtService.extractUsername(refreshToken);

    if (userEmail != null) {
      var user = this.userRepository.findByLogin(userEmail)
              .orElseThrow();
      if (jwtService.isTokenValid(refreshToken, user)) {
        var accessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, accessToken);
        var authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }
}
