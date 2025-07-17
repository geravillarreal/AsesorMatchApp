package com.uanl.asesormatch.controller;

import com.uanl.asesormatch.entity.Notification;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.NotificationRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationController(NotificationRepository notificationRepository,
                                  UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/delete")
    public String deleteNotification(@AuthenticationPrincipal OidcUser oidcUser,
                                     @RequestParam Long id) {
        User user = userRepository.findByEmail(oidcUser.getEmail()).orElseThrow();
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n != null && n.getUser().getId().equals(user.getId())) {
            notificationRepository.delete(n);
        }
        return "redirect:/dashboard";
    }
}
