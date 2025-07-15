package com.uanl.asesormatch.controller.advisor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uanl.asesormatch.dto.AdvisorProfileDTO;
import com.uanl.asesormatch.service.AdvisorService;

@RestController
@RequestMapping("/api/advisors")
@CrossOrigin(origins = "*")
public class AdvisorController {

	private final AdvisorService advisorService;

	public AdvisorController(AdvisorService advisorService) {
		this.advisorService = advisorService;
	}

	@GetMapping("/profile/{id}")
	public ResponseEntity<AdvisorProfileDTO> profile(@PathVariable Long id) {
		return advisorService.getProfile(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}
}