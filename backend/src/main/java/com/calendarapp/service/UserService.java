package com.calendarapp.service;

import com.calendarapp.exception.UserWithoutGroupException;
import com.calendarapp.model.Group;
import com.calendarapp.model.User;
import com.calendarapp.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	public Group getCurrentUserGroup() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();

		User user = userRepository.findByLogin(email)
				.orElseThrow(() -> new RuntimeException("User not found"));
		Group group = user.getGroup();

		if (group != null) {
			return group;
		} else {
			throw new UserWithoutGroupException("User " + email + " does not belong to any group");
		}
	}

	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String email = authentication.getName();
		return userRepository.findByLogin(email)
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	public User getUserById(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User with id " + userId + " not found"));
	}

	public List<User> getUsersByGroup(Group group) {
		return userRepository.findByGroup(group);
	}
}
