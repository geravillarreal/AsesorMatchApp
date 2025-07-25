package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.dto.ProjectDTO;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.service.NotificationService;
import com.uanl.asesormatch.service.StoryService;
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
	private final StoryService storyService;

	public ProjectController(UserRepository userRepository, ProjectRepository projectRepository,
			MatchRepository matchRepository, NotificationService notificationService,
			AdvisorEmailProvider emailProvider, StoryService storyService) {
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		this.matchRepository = matchRepository;
		this.notificationService = notificationService;
		this.emailProvider = emailProvider;
		this.storyService = storyService;
	}

	@GetMapping("/{id}")
	public String viewProject(@AuthenticationPrincipal OidcUser oidcUser, @PathVariable Long id, Model model) {
		User user = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
		Project project = projectRepository.findById(id).orElseThrow();

		boolean allowed = project.getStudent().getId().equals(user.getId())
				|| (project.getAdvisor() != null && project.getAdvisor().getId().equals(user.getId()));
		if (!allowed) {
			String redirect = user.getRole() == Role.ADVISOR ? "/advisor-dashboard" : "/dashboard";
			return "redirect:" + redirect;
		}

		model.addAttribute("project", project);
		model.addAttribute("studentView", project.getStudent().getId().equals(user.getId()));
		return "project-detail";
	}

	@GetMapping("/new")
	public String newProjectForm(Model model) {
		model.addAttribute("project", new ProjectDTO());
		return "project-form";
	}

	@PostMapping("/new")
	public String submitProject(@AuthenticationPrincipal OidcUser oidcUser, @ModelAttribute("project") ProjectDTO dto) {

		User student = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();

		Project project = new Project();
		project.setTitle(dto.getTitle());
		project.setDescription(dto.getDescription());
		project.setStatus(ProjectStatus.DRAFT);
		project.setStudent(student);
		project.setStartDate(LocalDate.now());

		projectRepository.save(project);
		return "redirect:/dashboard?tab=projects";
	}

	@PostMapping("/assign")
	public String assignProject(@AuthenticationPrincipal OidcUser oidcUser, @RequestParam Long projectId) {

		User advisor = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
		Project project = projectRepository.findById(projectId).orElseThrow();

		boolean hasMatch = matchRepository.existsByStudentIdAndAdvisorIdAndStatus(project.getStudent().getId(),
				advisor.getId(), MatchStatus.ACCEPTED);

		boolean hasActive = !projectRepository.findByStudentAndAdvisorAndStatusAndDeletedFalse(project.getStudent(),
				advisor, ProjectStatus.IN_PROGRESS).isEmpty();

		if (project.getAdvisor() == null && hasMatch && !hasActive) {
			project.setAdvisor(advisor);
			project.setStatus(ProjectStatus.IN_PROGRESS);
			projectRepository.save(project);
		}

		return "redirect:/advisor-dashboard";
	}

	@PostMapping("/reject")
	public String rejectProject(@AuthenticationPrincipal OidcUser oidcUser, @RequestParam Long projectId) {

		User advisor = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
		Project project = projectRepository.findById(projectId).orElseThrow();

		boolean hasMatch = matchRepository.existsByStudentIdAndAdvisorIdAndStatus(project.getStudent().getId(),
				advisor.getId(), MatchStatus.ACCEPTED);

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

		var optProject = projectRepository.findByStudentAndAdvisorAndStatusAndDeletedFalse(match.getStudent(),
				match.getAdvisor(), ProjectStatus.IN_PROGRESS).stream().findFirst();

		Project completed = null;
		if (optProject.isPresent()) {
			var p = optProject.get();
			if (storyService.hasPendingStories(p)) {
				return "redirect:/advisor-dashboard?pendingStories";
			}
			completed = p;
			p.setStatus(ProjectStatus.COMPLETED);
			projectRepository.save(p);
			String url = "/dashboard?feedbackProjectId=" + p.getId();
			String msg = "Your project '" + p.getTitle()
					+ "' was marked as completed. Please provide your feedback <a href='" + url + "'>here</a>";
			notificationService.notify(match.getStudent(), msg);
		}

		return completed != null ? "redirect:/advisor-dashboard?feedbackProjectId=" + completed.getId()
				: "redirect:/advisor-dashboard";
	}

	@PostMapping("/complete-project")
	public String completeProjectById(@RequestParam Long projectId) {
		Project project = projectRepository.findById(projectId).orElseThrow();
		if (project.getAdvisor() != null && project.getStatus() == ProjectStatus.IN_PROGRESS) {
			if (storyService.hasPendingStories(project)) {
				return "redirect:/advisor-dashboard?pendingStories";
			}
			project.setStatus(ProjectStatus.COMPLETED);
			projectRepository.save(project);

			var matchOpt = matchRepository.findByAdvisorAndStatus(project.getAdvisor(), MatchStatus.ACCEPTED).stream()
					.filter(m -> m.getStudent().equals(project.getStudent())).findFirst();
			matchOpt.ifPresent(m -> {
				String url = "/dashboard?feedbackProjectId=" + project.getId();
				String msg = "Your project '" + project.getTitle()
						+ "' was marked as completed. Please provide your feedback <a href='" + url + "'>here</a>";
				notificationService.notify(m.getStudent(), msg);
			});
			return matchOpt.isPresent() ? "redirect:/advisor-dashboard?feedbackProjectId=" + project.getId()
					: "redirect:/advisor-dashboard";
		}

		return "redirect:/advisor-dashboard";
	}

	@PostMapping("/delete")
	public String deleteProject(@AuthenticationPrincipal OidcUser oidcUser, @RequestParam Long projectId) {
		User student = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
		Project project = projectRepository.findById(projectId).orElseThrow();

		if (!project.getStudent().getId().equals(student.getId())) {
			return "redirect:/dashboard?tab=projects";
		}

		if (project.getStatus() == ProjectStatus.IN_PROGRESS) {
			// cannot remove in progress or assigned projects
			return "redirect:/dashboard?tab=projects";
		}

		if (project.getStatus() == ProjectStatus.COMPLETED) {
			project.setDeleted(true);
			projectRepository.save(project);
		} else {
			projectRepository.delete(project);
		}

		return "redirect:/dashboard?tab=projects";
	}
}