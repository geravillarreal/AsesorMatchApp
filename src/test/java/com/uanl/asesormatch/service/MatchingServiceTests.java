package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import com.uanl.asesormatch.repository.NotificationRepository;
import com.uanl.asesormatch.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class MatchingServiceTests {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private MatchingService matchingService;

    @BeforeEach
    void setUp() {
        NotificationService notificationService = new NotificationService(notificationRepository);
        matchingService = new MatchingService(matchRepository, userRepository, projectRepository, notificationService);
    }

    @Test
    void requestMatchPersistsPendingMatch() {
        User student = new User();
        student.setFullName("Student Test");
        student.setEmail("student@test.com");
        student.setRole(Role.STUDENT);
        userRepository.save(student);

        User advisor = new User();
        advisor.setFullName("Advisor Test");
        advisor.setEmail("advisor@test.com");
        advisor.setRole(Role.ADVISOR);
        userRepository.save(advisor);

        double score = 0.85;
        matchingService.requestMatch(student.getId(), advisor.getId(), score);

        List<Match> persisted = matchRepository.findAll();
        assertEquals(1, persisted.size());
        Match match = persisted.get(0);

        assertEquals(student.getId(), match.getStudent().getId());
        assertEquals(advisor.getId(), match.getAdvisor().getId());
        assertEquals(score, match.getCompatibilityScore());
        assertEquals(MatchStatus.PENDING, match.getStatus());
    }
}
