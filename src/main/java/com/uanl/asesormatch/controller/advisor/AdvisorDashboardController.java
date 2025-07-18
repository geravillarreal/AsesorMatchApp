package com.uanl.asesormatch.controller.advisor;

import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.config.AdvisorEmailProvider;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdvisorDashboardController {

    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ProjectRepository projectRepository;
    private final AdvisorEmailProvider emailProvider;

    public AdvisorDashboardController(UserRepository userRepository, MatchRepository matchRepository, ProjectRepository projectRepository,
                                     AdvisorEmailProvider emailProvider) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.projectRepository = projectRepository;
        this.emailProvider = emailProvider;
    }

    @GetMapping("/advisor-dashboard")
    public String advisorDashboard(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        User advisor = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
        long completedProjectCount =
                projectRepository.countByAdvisorAndStatus(advisor, ProjectStatus.COMPLETED);

        int matchCount = matchRepository.findByAdvisor(advisor).size();
        int projectCount = projectRepository.findByAdvisor(advisor).size();

        var matches = matchRepository.findByAdvisor(advisor);
        for (var m : matches) {
                boolean noneActive = projectRepository
                                .findByStudentAndAdvisorAndStatus(m.getStudent(), advisor,
                                                ProjectStatus.IN_PROGRESS)
                                .isEmpty();
                m.setAllProjectsCompleted(noneActive);
        }

        var completedProjects = projectRepository.findByAdvisorAndStatus(advisor, ProjectStatus.COMPLETED);

        var assignedProjects = projectRepository.findByAdvisor(advisor).stream()
                        .filter(p -> p.getStatus() != ProjectStatus.COMPLETED)
                        .toList();

        var acceptedMatches = matchRepository.findByAdvisorAndStatus(advisor, MatchStatus.ACCEPTED);
        java.util.List<com.uanl.asesormatch.entity.Project> available = new java.util.ArrayList<>();
        java.util.Set<Long> blocked = new java.util.HashSet<>();
        for (var p : assignedProjects) {
                if (p.getStatus() == ProjectStatus.IN_PROGRESS) {
                        blocked.add(p.getStudent().getId());
                }
        }
        for (var m : acceptedMatches) {
                if (projectRepository
                                .findByStudentAndAdvisorAndStatus(m.getStudent(), advisor,
                                                ProjectStatus.IN_PROGRESS)
                                .isEmpty()) {
                        var studentProjects = projectRepository.findByStudent(m.getStudent());
                        for (var p : studentProjects) {
                                if (p.getStatus() != ProjectStatus.COMPLETED && p.getAdvisor() == null) {
                                        available.add(p);
                                }
                        }
                }
        }

        model.addAttribute("matchCount", matchCount);
        model.addAttribute("projectCount", projectCount);
        model.addAttribute("completedProjectCount", completedProjectCount);
        model.addAttribute("advisor", advisor);
        model.addAttribute("matches", matches);
        model.addAttribute("projects", assignedProjects);
        model.addAttribute("availableProjects", available);
        model.addAttribute("blockedStudentIds", blocked);
        model.addAttribute("completedProjects", completedProjects);

        return "advisor-dashboard";
    }
}