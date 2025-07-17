package com.uanl.asesormatch.controller.advisor;

import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;


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

    public AdvisorDashboardController(UserRepository userRepository, MatchRepository matchRepository, ProjectRepository projectRepository) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.projectRepository = projectRepository;
    }

    @GetMapping("/advisor-dashboard")
    public String advisorDashboard(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        //User advisor = userRepository.findByEmail(oidcUser.getEmail()).orElseThrow();
        User advisor = userRepository.findByEmail("advisor.john.doe@uanl.edu.mx").orElseThrow();
        long completedProjectCount =
                projectRepository.countByAdvisorAndStatus(advisor, ProjectStatus.COMPLETED);

        int matchCount = matchRepository.findByAdvisor(advisor).size();
        int projectCount = projectRepository.findByAdvisor(advisor).size();

        model.addAttribute("matchCount", matchCount);
        model.addAttribute("projectCount", projectCount);
        model.addAttribute("completedProjectCount", completedProjectCount);
        model.addAttribute("advisor", advisor);
        model.addAttribute("matches", matchRepository.findByAdvisor(advisor));
        model.addAttribute("projects", projectRepository.findByAdvisor(advisor));

        return "advisor-dashboard";
    }
}