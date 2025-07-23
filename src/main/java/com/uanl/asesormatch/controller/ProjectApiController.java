package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.service.StoryService;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.config.AdvisorEmailProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectApiController {
	private final ProjectRepository projectRepository;
	private final UserRepository userRepository;
	private final StoryService storyService;
	private final AdvisorEmailProvider emailProvider;

	public ProjectApiController(ProjectRepository projectRepository, UserRepository userRepository,
			StoryService storyService, AdvisorEmailProvider emailProvider) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.storyService = storyService;
		this.emailProvider = emailProvider;
	}

	@GetMapping("/{id}/pending-stories")
	public ResponseEntity<Boolean> pendingStories(@AuthenticationPrincipal OidcUser oidcUser, @PathVariable Long id) {
		User current = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
		Project project = projectRepository.findById(id).orElse(null);
		if (project == null) {
			return ResponseEntity.notFound().build();
		}
		if (project.getAdvisor() != null && !current.getId().equals(project.getStudent().getId())
				&& !current.getId().equals(project.getAdvisor().getId())) {
			return ResponseEntity.status(403).build();
		}
		boolean pending = storyService.hasPendingStories(project);
		return ResponseEntity.ok(pending);
	}
}
