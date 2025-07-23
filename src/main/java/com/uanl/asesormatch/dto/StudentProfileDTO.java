package com.uanl.asesormatch.dto;

import java.util.List;

public record StudentProfileDTO(Long id, String fullName, String email, String faculty, ProfileDTO profile,
		List<ProjectDTO> projects) {
}
