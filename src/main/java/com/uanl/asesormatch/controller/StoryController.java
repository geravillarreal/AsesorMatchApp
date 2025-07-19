package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.config.AdvisorEmailProvider;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.service.StoryService;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/story")
public class StoryController {
    private final StoryService storyService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AdvisorEmailProvider emailProvider;

    public StoryController(StoryService storyService, ProjectRepository projectRepository,
                           UserRepository userRepository, AdvisorEmailProvider emailProvider) {
        this.storyService = storyService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.emailProvider = emailProvider;
    }

    @GetMapping("/list")
    public String list(@AuthenticationPrincipal OidcUser oidcUser, @RequestParam Long projectId, Model model) {
        User user = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
        Project project = projectRepository.findById(projectId).orElseThrow();
        if (!project.getStudent().getId().equals(user.getId()) &&
            (project.getAdvisor() == null || !project.getAdvisor().getId().equals(user.getId()))) {
            return "redirect:/dashboard";
        }
        model.addAttribute("project", project);
        model.addAttribute("stories", storyService.getStories(project));
        model.addAttribute("showActions", user.getId().equals(project.getStudent().getId()));
        return "project-stories";
    }

    @PostMapping("/add")
    public String add(@AuthenticationPrincipal OidcUser oidcUser,
                      @RequestParam Long projectId,
                      @RequestParam String title,
                      @RequestParam String description) {
        User user = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
        Project project = projectRepository.findById(projectId).orElseThrow();
        if (!project.getStudent().getId().equals(user.getId()) &&
            (project.getAdvisor() == null || !project.getAdvisor().getId().equals(user.getId()))) {
            return "redirect:/dashboard";
        }
        storyService.createStory(project, user, title, description);
        return "redirect:/story/list?projectId=" + projectId;
    }

    @PostMapping("/next")
    public String next(@AuthenticationPrincipal OidcUser oidcUser,
                       @RequestParam Long storyId) {
        User user = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
        storyService.advanceStatus(storyId, user);
        Long projectId = storyService.getStory(storyId).getProject().getId();
        return "redirect:/story/list?projectId=" + projectId;
    }
}
