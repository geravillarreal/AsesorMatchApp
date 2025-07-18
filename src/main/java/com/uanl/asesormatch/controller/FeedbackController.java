package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.entity.Feedback;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.FeedbackRepository;
import com.uanl.asesormatch.repository.MatchRepository;
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
    private final MatchRepository matchRepo;
    private final FeedbackRepository feedbackRepo;
    private final UserRepository userRepo;
    private final NotificationRepository notificationRepo;

    public FeedbackController(MatchRepository matchRepo, FeedbackRepository feedbackRepo, UserRepository userRepo,
                              NotificationRepository notificationRepo) {
        this.matchRepo = matchRepo;
        this.feedbackRepo = feedbackRepo;
        this.userRepo = userRepo;
        this.notificationRepo = notificationRepo;
    }

    @PostMapping("/submit")
    public String submitFeedback(@AuthenticationPrincipal OidcUser oidcUser,
                                 @RequestParam Long matchId,
                                 @RequestParam Integer rating,
                                 @RequestParam String comment) {
        User user = userRepo.findByEmail(oidcUser.getEmail()).orElseThrow();
        Match match = matchRepo.findById(matchId).orElseThrow();

        if (!feedbackRepo.existsByMatchAndFromUser(match, user)) {
            Feedback fb = new Feedback();
            fb.setMatch(match);
            fb.setFromUser(user);
            fb.setRating(rating);
            fb.setComment(comment);
            feedbackRepo.save(fb);
        }

        String matchIdToken = "feedbackMatchId=" + matchId;
        var notifications = notificationRepo.findByUserAndMessageContaining(user, matchIdToken);
        if (!notifications.isEmpty()) {
            notificationRepo.deleteAll(notifications);
        }

        String redirect = user.getRole() == Role.ADVISOR ? "/advisor-dashboard" : "/dashboard";
        return "redirect:" + redirect;
    }
}
