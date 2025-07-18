package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.dto.ProjectDTO;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.service.NotificationService;
import com.uanl.asesormatch.config.AdvisorEmailProvider;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/project")
public class ProjectController {

        private final UserRepository userRepository;
        private final ProjectRepository projectRepository;
    private final MatchRepository matchRepository;
    private final NotificationService notificationService;
    private final AdvisorEmailProvider emailProvider;

    public ProjectController(UserRepository userRepository, ProjectRepository projectRepository,
                        MatchRepository matchRepository, NotificationService notificationService,
                        AdvisorEmailProvider emailProvider) {
                this.userRepository = userRepository;
                this.projectRepository = projectRepository;
                this.matchRepository = matchRepository;
                this.notificationService = notificationService;
                this.emailProvider = emailProvider;
        }

	@GetMapping("/new")
	public String newProjectForm(Model model) {
		model.addAttribute("project", new ProjectDTO());
		return "project-form";
	}

	@PostMapping("/new")
	public String submitProject(@AuthenticationPrincipal OidcUser oidcUser, @ModelAttribute("project") ProjectDTO dto) {

		User student = userRepository.findByEmail(oidcUser.getEmail()).orElseThrow();

		Project project = new Project();
		project.setTitle(dto.getTitle());
		project.setDescription(dto.getDescription());
		project.setStatus(ProjectStatus.DRAFT);
		project.setStudent(student);
		project.setStartDate(LocalDate.now());

		projectRepository.save(project);
		return "redirect:/dashboard";
	}

	@PostMapping("/assign")
	public String assignProject(@AuthenticationPrincipal OidcUser oidcUser, @RequestParam Long projectId) {

                User advisor = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
		Project project = projectRepository.findById(projectId).orElseThrow();

                boolean hasMatch = matchRepository.existsByStudentIdAndAdvisorIdAndStatus(
                                project.getStudent().getId(), advisor.getId(), MatchStatus.ACCEPTED);

                boolean hasActive = projectRepository
                                .findByStudentAndAdvisorAndStatusAndDeletedFalse(project.getStudent(), advisor, ProjectStatus.IN_PROGRESS)
                                .isPresent();

                if (project.getAdvisor() == null && hasMatch && !hasActive) {
                        project.setAdvisor(advisor);
                        project.setStatus(ProjectStatus.IN_PROGRESS);
                        projectRepository.save(project);
                }

		return "redirect:/advisor-dashboard";
	}
	
        @PostMapping("/reject")
        public String rejectProject(@AuthenticationPrincipal OidcUser oidcUser,
                                    @RequestParam Long projectId) {

            User advisor = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
	    Project project = projectRepository.findById(projectId).orElseThrow();

	    boolean hasMatch = matchRepository.existsByStudentIdAndAdvisorIdAndStatus(
	            project.getStudent().getId(), advisor.getId(), MatchStatus.ACCEPTED);

	    if (hasMatch && project.getAdvisor() == null) {
	        project.setStatus(ProjectStatus.REJECTED);
	        project.setRejectedByAdvisor(advisor);
	        projectRepository.save(project);
	    }

            return "redirect:/advisor-dashboard";
        }

        @PostMapping("/complete")
        public String completeProject(@RequestParam Long matchId) {
                var match = matchRepository.findById(matchId).orElseThrow();
                match.setStatus(MatchStatus.COMPLETED);
                matchRepository.save(match);

                var optProject = projectRepository.findByStudentAndAdvisorAndStatusAndDeletedFalse(
                                match.getStudent(), match.getAdvisor(), ProjectStatus.IN_PROGRESS);

                optProject.ifPresent(p -> {
                        p.setStatus(ProjectStatus.COMPLETED);
                        projectRepository.save(p);
                        String url = "/dashboard?feedbackMatchId=" + matchId;
                        String msg = "Tu proyecto '" + p.getTitle()
                                        + "' fue marcado como completado. Por favor da tu feedback. <a href='"
                                        + url + "'>aquí</a>";
                        notificationService.notify(match.getStudent(), msg);
                });

                return "redirect:/advisor-dashboard?feedbackMatchId=" + matchId;
        }

        @PostMapping("/complete-project")
        public String completeProjectById(@RequestParam Long projectId) {
                Project project = projectRepository.findById(projectId).orElseThrow();
                if (project.getAdvisor() != null && project.getStatus() == ProjectStatus.IN_PROGRESS) {
                        project.setStatus(ProjectStatus.COMPLETED);
                        projectRepository.save(project);

                        var matchOpt = matchRepository.findByAdvisorAndStatus(project.getAdvisor(), MatchStatus.ACCEPTED)
                                        .stream()
                                        .filter(m -> m.getStudent().equals(project.getStudent()))
                                        .findFirst();
                        matchOpt.ifPresent(m -> {
                                String url = "/dashboard?feedbackMatchId=" + m.getId();
                                String msg = "Tu proyecto '" + project.getTitle()
                                                + "' fue marcado como completado. Por favor da tu feedback. <a href='"
                                                + url + "'>aquí</a>";
                                notificationService.notify(m.getStudent(), msg);
                        });
                        return matchOpt.map(m -> "redirect:/advisor-dashboard?feedbackMatchId=" + m.getId())
                                        .orElse("redirect:/advisor-dashboard");
                }

                return "redirect:/advisor-dashboard";
        }

        @PostMapping("/delete")
        public String deleteProject(@AuthenticationPrincipal OidcUser oidcUser,
                                    @RequestParam Long projectId) {
                User student = userRepository.findByEmail(oidcUser.getEmail()).orElseThrow();
                Project project = projectRepository.findById(projectId).orElseThrow();

                if (!project.getStudent().getId().equals(student.getId())) {
                        return "redirect:/dashboard";
                }

                if (project.getStatus() == ProjectStatus.IN_PROGRESS) {
                        // cannot remove in progress or assigned projects
                        return "redirect:/dashboard";
                }

                if (project.getStatus() == ProjectStatus.COMPLETED) {
                        project.setDeleted(true);
                        projectRepository.save(project);
                } else {
                        projectRepository.delete(project);
                }

                return "redirect:/dashboard";
        }
}