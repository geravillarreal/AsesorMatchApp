package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.FeedbackRepository;
import com.uanl.asesormatch.repository.NotificationRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.uanl.asesormatch.config.TestEntityScanConfig;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestEntityScanConfig.class)
class FeedbackServiceTests {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    private FeedbackService feedbackService;

    private User student;
    private User advisor;
    private Project project;

    @BeforeEach
    void setUp() {
        NotificationService notificationService = new NotificationService(notificationRepository);
        feedbackService = new FeedbackService(projectRepository, feedbackRepository,
                userRepository, notificationRepository, notificationService);

        student = new User();
        student.setFullName("Student");
        student.setEmail("s@test.com");
        student.setRole(Role.STUDENT);
        userRepository.save(student);

        advisor = new User();
        advisor.setFullName("Advisor");
        advisor.setEmail("a@test.com");
        advisor.setRole(Role.ADVISOR);
        userRepository.save(advisor);

        project = new Project();
        project.setTitle("P1");
        project.setDescription("D1");
        project.setStudent(student);
        project.setAdvisor(advisor);
        project.setStatus(ProjectStatus.COMPLETED);
        projectRepository.save(project);
    }

    @Test
    void otherUserGetsNotificationIfNoFeedback() {
        feedbackService.submitFeedback(student, project.getId(), 5, "Great");

        assertTrue(feedbackRepository.existsByProjectAndFromUser(project, student));
        var notes = notificationRepository.findByUserOrderByCreatedAtDesc(advisor);
        assertEquals(1, notes.size());
    }

    @Test
    void notificationRemovedWhenOtherGivesFeedback() {
        feedbackService.submitFeedback(student, project.getId(), 5, "Great");
        feedbackService.submitFeedback(advisor, project.getId(), 4, "Ok");

        assertTrue(feedbackRepository.existsByProjectAndFromUser(project, advisor));
        var notes = notificationRepository.findByUserOrderByCreatedAtDesc(advisor);
        assertTrue(notes.isEmpty());
    }
}
