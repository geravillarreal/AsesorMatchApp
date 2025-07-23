package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.entity.Notification;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.NotificationRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.config.AdvisorEmailProvider;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AdvisorEmailProvider emailProvider;
    public NotificationController(NotificationRepository notificationRepository,
                                  UserRepository userRepository,
                                  AdvisorEmailProvider emailProvider) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailProvider = emailProvider;
    }

    @PostMapping("/delete")
    public String deleteNotification(@AuthenticationPrincipal OidcUser oidcUser,
                                     @RequestParam Long id) {
        User user = userRepository.findByEmail(emailProvider.resolveEmail(oidcUser)).orElseThrow();
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n != null && n.getUser().getId().equals(user.getId())) {
            notificationRepository.delete(n);
        }
        return "redirect:/dashboard";
    }

}
