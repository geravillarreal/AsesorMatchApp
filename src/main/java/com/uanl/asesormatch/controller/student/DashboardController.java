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
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.service.MatchingService;
import com.uanl.asesormatch.service.NotificationService;
import com.uanl.asesormatch.config.AdvisorEmailProvider;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashboardController {

        private final UserRepository userRepository;
        private final MatchingEngineClient matchingEngineClient;
        private final MatchingService matchingService;
        private final ProjectRepository projectRepository;
        private final NotificationService notificationService;
        private final MatchRepository matchRepository;
        private final AdvisorEmailProvider emailProvider;

        public DashboardController(UserRepository userRepository, MatchingEngineClient matchingEngineClient,
                        MatchingService matchingService, ProjectRepository projectRepository,
                        NotificationService notificationService, MatchRepository matchRepository,
                        AdvisorEmailProvider emailProvider) {
                this.userRepository = userRepository;
                this.matchingEngineClient = matchingEngineClient;
                this.matchingService = matchingService;
                this.projectRepository = projectRepository;
                this.notificationService = notificationService;
                this.matchRepository = matchRepository;
                this.emailProvider = emailProvider;
        }

       @GetMapping("/api/recommendations")
       @ResponseBody
       public Map<String, Object> recommendations(@AuthenticationPrincipal OidcUser oidcUser) {
               User user = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();

               Map<String, Object> result = new HashMap<>();
               boolean hasDraft = projectRepository.existsByStudentAndStatus(user, ProjectStatus.DRAFT);
               result.put("noDraftProjects", !hasDraft);

               if (hasDraft && user.getProfile() != null) {
                       List<RecommendationDTO> recommendations = matchingEngineClient.getRecommendations(user.getId());
                       result.put("recommendations", recommendations);

                       List<Project> studentProjects = projectRepository.findByStudentAndDeletedFalse(user);
                       if (studentProjects != null) {
                               for (Project p : studentProjects) {
                                       if (p.getStatus() == ProjectStatus.REJECTED && p.getRejectedByAdvisor() != null) {
                                               List<RecommendationDTO> newRecs = matchingEngineClient.getRecommendations(user.getId());
                                               List<RecommendationDTO> filtered = newRecs.stream().filter(rec -> !rec.getAdvisorId().toString()
                                                               .equals(p.getRejectedByAdvisor().getId().toString())).toList();
                                               result.put("newRecommendations", filtered);
                                               break;
                                       }
                               }
                       }
               }
               return result;
       }

	@GetMapping("/dashboard")
        public String dashboard(@AuthenticationPrincipal OidcUser oidcUser, Model model,
                                @RequestParam(required = false) Long feedbackProjectId) {
                User user = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();

		if (user.getRole() == Role.ADVISOR) {
			return "redirect:/advisor-dashboard";
		}

                Profile profile = user.getProfile();
                List<Match> matchHistory = matchingService.getMatchesForStudent(user);
                List<Project> studentProjects = projectRepository.findByStudentAndDeletedFalse(user);
                var notifications = notificationService.getNotificationsFor(user);

                if (feedbackProjectId != null) {
                        var projectOpt = projectRepository.findById(feedbackProjectId);
                        if (projectOpt.isPresent() && projectOpt.get().getStudent().getId().equals(user.getId())) {
                                var project = projectOpt.get();
                                model.addAttribute("feedbackProjectId", feedbackProjectId);
                                model.addAttribute("feedbackOtherName",
                                        project.getAdvisor() != null ? project.getAdvisor().getFullName() : "");
                                model.addAttribute("feedbackProjectTitle", project.getTitle());
                        }
                }

		model.addAttribute("user", user);
		model.addAttribute("profile", profile);
		model.addAttribute("matches", matchHistory);
		model.addAttribute("studentProjects", studentProjects);
		model.addAttribute("notifications", notifications);

		return "dashboard";
	}
}