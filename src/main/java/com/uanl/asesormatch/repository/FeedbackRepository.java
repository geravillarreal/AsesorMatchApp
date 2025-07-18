package com.uanl.asesormatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uanl.asesormatch.entity.Feedback;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsByProjectAndFromUser(Project project, User fromUser);

    java.util.Optional<Feedback> findByProjectAndFromUser(Project project, User fromUser);
}
