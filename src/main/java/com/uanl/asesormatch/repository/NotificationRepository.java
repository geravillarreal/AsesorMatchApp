package com.uanl.asesormatch.repository;

import com.uanl.asesormatch.entity.Notification;
import com.uanl.asesormatch.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndMessageContaining(User user, String message);
}
