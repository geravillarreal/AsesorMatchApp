package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.dto.ProjectFeedbackInfoDTO;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.repository.FeedbackRepository;
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
public class ProjectFeedbackInfoController {
	private final ProjectRepository projectRepo;
	private final UserRepository userRepo;
	private final FeedbackRepository feedbackRepo;
	private final AdvisorEmailProvider emailProvider;

	public ProjectFeedbackInfoController(ProjectRepository projectRepo, UserRepository userRepo,
			FeedbackRepository feedbackRepo, AdvisorEmailProvider emailProvider) {
		this.projectRepo = projectRepo;
		this.userRepo = userRepo;
		this.feedbackRepo = feedbackRepo;
		this.emailProvider = emailProvider;
	}

	@GetMapping("/{id}/feedback")
	public ResponseEntity<ProjectFeedbackInfoDTO> info(@AuthenticationPrincipal OidcUser oidcUser,
			@PathVariable Long id) {
		User current = userRepo.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
		Project project = projectRepo.findById(id).orElse(null);
		if (project == null) {
			return ResponseEntity.notFound().build();
		}

		if (project.getAdvisor() != null && !current.getId().equals(project.getStudent().getId())
				&& !current.getId().equals(project.getAdvisor().getId())) {
			return ResponseEntity.status(403).build();
		}

		User other = current.getId().equals(project.getStudent().getId()) ? project.getAdvisor() : project.getStudent();

		boolean myGiven = feedbackRepo.existsByProjectAndFromUser(project, current);
		boolean otherGiven = other != null && feedbackRepo.existsByProjectAndFromUser(project, other);

		ProjectFeedbackInfoDTO dto = new ProjectFeedbackInfoDTO(project.getId(),
				other != null ? other.getFullName() : "", project.getTitle(), myGiven, otherGiven);
		return ResponseEntity.ok(dto);
	}
}
