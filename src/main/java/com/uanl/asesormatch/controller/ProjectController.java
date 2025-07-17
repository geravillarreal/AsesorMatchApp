package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.dto.ProjectDTO;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
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

	public ProjectController(UserRepository userRepository, ProjectRepository projectRepository,
			MatchRepository matchRepository) {
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		this.matchRepository = matchRepository;
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

		User advisor = userRepository.findByEmail(oidcUser.getEmail()).orElseThrow();
		Project project = projectRepository.findById(projectId).orElseThrow();

		boolean hasMatch = matchRepository.existsByStudentIdAndAdvisorIdAndStatus(project.getStudent().getId(),
				advisor.getId(), MatchStatus.ACCEPTED);

		if (project.getAdvisor() == null && hasMatch) {
			project.setAdvisor(advisor);
			project.setStatus(ProjectStatus.IN_PROGRESS);
			projectRepository.save(project);
		}

		return "redirect:/advisor-dashboard";
	}
	
        @PostMapping("/reject")
        public String rejectProject(@AuthenticationPrincipal OidcUser oidcUser,
                                    @RequestParam Long projectId) {

	    User advisor = userRepository.findByEmail(oidcUser.getEmail()).orElseThrow();
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
                var optProject = projectRepository.findByStudentAndAdvisorAndStatus(
                                match.getStudent(), match.getAdvisor(), ProjectStatus.IN_PROGRESS);
                optProject.ifPresent(p -> {
                        p.setStatus(ProjectStatus.COMPLETED);
                        projectRepository.save(p);
                });

                return "redirect:/advisor-dashboard";
        }

        @PostMapping("/delete")
        public String deleteProject(@AuthenticationPrincipal OidcUser oidcUser,
                                    @RequestParam Long projectId) {
                User student = userRepository.findByEmail(oidcUser.getEmail()).orElseThrow();
                Project project = projectRepository.findById(projectId).orElseThrow();

                if (project.getStudent().getId().equals(student.getId())) {
                        projectRepository.delete(project);
                }

                return "redirect:/dashboard";
        }
}