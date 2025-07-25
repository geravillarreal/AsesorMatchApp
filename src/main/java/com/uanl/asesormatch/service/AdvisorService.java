package com.uanl.asesormatch.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.uanl.asesormatch.dto.AdvisorProfileDTO;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.UserRepository;

@Service
public class AdvisorService {
	private final UserRepository userRepo;

	public AdvisorService(UserRepository userRepo) {
		this.userRepo = userRepo;
	}

	public Optional<AdvisorProfileDTO> getProfile(Long id) {
		return userRepo.findById(id).filter(u -> u.getRole() == Role.ADVISOR)
				.map(u -> new AdvisorProfileDTO(u.getId(), u.getFullName(), u.getEmail(), u.getFaculty(),
						u.getProfile() != null ? u.getProfile().getDTO() : null));
	}
}