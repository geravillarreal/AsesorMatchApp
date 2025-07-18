package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.dto.MatchInfoDTO;
import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
public class MatchInfoController {
    private final MatchRepository matchRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;

    public MatchInfoController(MatchRepository matchRepo, ProjectRepository projectRepo, UserRepository userRepo) {
        this.matchRepo = matchRepo;
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchInfoDTO> info(@AuthenticationPrincipal OidcUser oidcUser, @PathVariable Long id) {
        User current = userRepo.findByEmail(oidcUser.getEmail()).orElseThrow();
        Match match = matchRepo.findById(id).orElse(null);
        if (match == null) {
            return ResponseEntity.notFound().build();
        }
        User other = current.getId().equals(match.getStudent().getId()) ? match.getAdvisor() : match.getStudent();
        Project project = projectRepo
                .findByStudentAndAdvisorAndStatus(match.getStudent(), match.getAdvisor(), ProjectStatus.COMPLETED)
                .orElse(null);
        String title = project != null ? project.getTitle() : "";
        MatchInfoDTO dto = new MatchInfoDTO(match.getId(), other.getFullName(), title);
        return ResponseEntity.ok(dto);
    }
}
