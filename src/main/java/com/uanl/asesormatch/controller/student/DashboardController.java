package com.uanl.asesormatch.controller.student;

import com.uanl.asesormatch.client.MatchingEngineClient;
import com.uanl.asesormatch.dto.RecommendationDTO;
import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.Profile;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.service.MatchingService;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

	private final UserRepository userRepository;
	private final MatchingEngineClient matchingEngineClient;
	private final MatchingService matchingService;
	private final ProjectRepository projectRepository;

	public DashboardController(UserRepository userRepository, MatchingEngineClient matchingEngineClient,
			MatchingService matchingService, ProjectRepository projectRepository) {
		this.userRepository = userRepository;
		this.matchingEngineClient = matchingEngineClient;
		this.matchingService = matchingService;
		this.projectRepository = projectRepository;
	}

	@GetMapping("/dashboard")
	public String dashboard(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
		User user = userRepository.findByEmail(oidcUser.getEmail()).orElseThrow();

		if (user.getRole() == Role.ADVISOR) {
			return "redirect:/advisor-dashboard";
		}

		Profile profile = user.getProfile();
		List<Match> matchHistory = matchingService.getMatchesForStudent(user);
		List<Project> studentProjects = projectRepository.findByStudent(user);

		if (profile != null) {			
			List<RecommendationDTO> recommendations = matchingEngineClient.getRecommendations(profile.getDTO());
			model.addAttribute("recommendations", recommendations);
			//matchingService.createMatches(user, recommendations);
			
			if (studentProjects != null) {
				for (Project p : studentProjects) {
					if (p.getStatus() == ProjectStatus.REJECTED && p.getRejectedByAdvisor() != null) {
						List<RecommendationDTO> newRecs = matchingEngineClient.getRecommendations(profile.getDTO());
						List<RecommendationDTO> filtered = newRecs.stream().filter(
								rec -> !rec.getAdvisorId().toString().equals(p.getRejectedByAdvisor().getId().toString()))
								.toList();
						model.addAttribute("newRecommendations", filtered);
						break;
					}
				}
			}
		}

		model.addAttribute("user", user);
		model.addAttribute("profile", profile);
		model.addAttribute("matches", matchHistory);
		model.addAttribute("studentProjects", studentProjects);

		return "dashboard";
	}
}