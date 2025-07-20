package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.service.FeedbackService;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.config.AdvisorEmailProvider;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {
    private final FeedbackService feedbackService;
    private final UserRepository userRepo;
    private final AdvisorEmailProvider emailProvider;

    public FeedbackController(FeedbackService feedbackService, UserRepository userRepo,
                              AdvisorEmailProvider emailProvider) {
        this.feedbackService = feedbackService;
        this.userRepo = userRepo;
        this.emailProvider = emailProvider;
    }

    @PostMapping("/submit")
    public String submitFeedback(@AuthenticationPrincipal OidcUser oidcUser,
                                 @RequestParam Long projectId,
                                 @RequestParam Integer rating,
                                 @RequestParam String comment) {
        User user = userRepo.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
        feedbackService.submitFeedback(user, projectId, rating, comment);

        String redirect = user.getRole() == Role.ADVISOR ? "/advisor-dashboard" : "/dashboard";
        return "redirect:" + redirect;
    }
}
