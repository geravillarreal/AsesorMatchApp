package com.uanl.asesormatch.controller.advisor;

import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

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
        List<Match> acceptedMatches = matchRepository.findByAdvisorAndStatus(advisor, MatchStatus.ACCEPTED);

        List<Project> availableProjects = new ArrayList<>();
        for (Match m : acceptedMatches) {
            List<Project> studentProjects = projectRepository.findByStudent(m.getStudent());
            for (Project p : studentProjects) {
                if (p.getAdvisor() == null) {
                    availableProjects.add(p);
                }
            }
        }

        model.addAttribute("availableProjects", availableProjects);
        model.addAttribute("advisor", advisor);
        model.addAttribute("matches", matchRepository.findByAdvisor(advisor));
        model.addAttribute("projects", projectRepository.findByAdvisor(advisor));

        return "advisor-dashboard";
    }
}