package com.uanl.asesormatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uanl.asesormatch.entity.Feedback;
import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.User;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    boolean existsByMatchAndFromUser(Match match, User fromUser);
}
