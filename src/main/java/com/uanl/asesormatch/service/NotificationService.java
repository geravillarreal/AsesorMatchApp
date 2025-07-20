package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Notification;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {
    private final NotificationRepository repo;
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public void notify(User user, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setCreatedAt(LocalDateTime.now());
        n.setRead(false);
        repo.save(n);
        sendEvent(user.getId(), n);
    }

    public List<Notification> getNotificationsFor(User user) {
        return repo.findByUserOrderByCreatedAtDesc(user);
    }

    public SseEmitter register(Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        var list = emitters.get(userId);
        if (list != null) {
            list.remove(emitter);
        }
    }

    private void sendEvent(Long userId, Notification notification) {
        var list = emitters.get(userId);
        if (list != null) {
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("notification")
                            .data(new NotificationDTO(notification.getId(), notification.getMessage())));
                } catch (IOException ex) {
                    removeEmitter(userId, emitter);
                }
            }
        }
    }

    public record NotificationDTO(Long id, String message) {}
}
