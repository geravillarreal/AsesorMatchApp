package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.entity.Feedback;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.FeedbackRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.repository.NotificationRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {
    private final ProjectRepository projectRepo;
    private final FeedbackRepository feedbackRepo;
    private final UserRepository userRepo;
    private final NotificationRepository notificationRepo;

    public FeedbackController(ProjectRepository projectRepo, FeedbackRepository feedbackRepo, UserRepository userRepo,
                              NotificationRepository notificationRepo) {
        this.projectRepo = projectRepo;
        this.feedbackRepo = feedbackRepo;
        this.userRepo = userRepo;
        this.notificationRepo = notificationRepo;
    }

    @PostMapping("/submit")
    public String submitFeedback(@AuthenticationPrincipal OidcUser oidcUser,
                                 @RequestParam Long projectId,
                                 @RequestParam Integer rating,
                                 @RequestParam String comment) {
        User user = userRepo.findByEmail(oidcUser.getEmail()).orElseThrow();
        Project project = projectRepo.findById(projectId).orElseThrow();

        if (!feedbackRepo.existsByProjectAndFromUser(project, user)) {
            Feedback fb = new Feedback();
            fb.setProject(project);
            fb.setFromUser(user);
            fb.setToUser(user.getId().equals(project.getStudent().getId()) ? project.getAdvisor() : project.getStudent());
            fb.setCreatedAt(java.time.LocalDateTime.now());
            fb.setRating(rating);
            fb.setComment(comment);
            feedbackRepo.save(fb);
        }

        String projectIdToken = "feedbackProjectId=" + projectId;
        var notifications = notificationRepo.findByUserAndMessageContaining(user, projectIdToken);
        if (!notifications.isEmpty()) {
            notificationRepo.deleteAll(notifications);
        }

        String redirect = user.getRole() == Role.ADVISOR ? "/advisor-dashboard" : "/dashboard";
        return "redirect:" + redirect;
    }
}
