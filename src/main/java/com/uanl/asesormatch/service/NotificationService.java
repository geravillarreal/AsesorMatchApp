package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Notification;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service
public class NotificationService {
    private final NotificationRepository repo;
    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public void notify(User user, String message) {
        logger.info("Notifying user {}: {}", user.getId(), message);
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());
        n.setRead(false);
        repo.save(n);
    }

    public List<Notification> getNotificationsFor(User user) {
        return repo.findByUserOrderByCreatedAtDesc(user);
    }


}
