package com.uanl.asesormatch.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> findAll() {
		return userRepository.findAll();
	}

	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	public User save(User user) {
		return userRepository.save(user);
	}

	public void delete(Long id) {
		userRepository.deleteById(id);
	}

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}
}