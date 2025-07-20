package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.entity.Story;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.enums.StoryStatus;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.StoryRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.uanl.asesormatch.config.TestEntityScanConfig;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import(TestEntityScanConfig.class)
class StoryServiceTests {
    @Autowired
    private StoryRepository storyRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;

    private StoryService storyService;

    private User student;
    private User advisor;
    private Project project;

    @BeforeEach
    void setup() {
        storyService = new StoryService(storyRepository);

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
        project.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(project);
    }

    @Test
    void studentAdvancesStorySequentially() {
        Story s = storyService.createStory(project, student, "T", "D");
        storyService.advanceStatus(s.getId(), student);
        Story updated = storyRepository.findById(s.getId()).orElseThrow();
        assertEquals(StoryStatus.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void advisorCannotAdvanceStory() {
        Story s = storyService.createStory(project, student, "T", "D");
        storyService.advanceStatus(s.getId(), advisor);
        Story same = storyRepository.findById(s.getId()).orElseThrow();
        assertEquals(StoryStatus.TO_DO, same.getStatus());
    }

    @Test
    void cannotAdvancePastDone() {
        Story s = storyService.createStory(project, student, "T", "D");
        storyService.advanceStatus(s.getId(), student); // IN_PROGRESS
        storyService.advanceStatus(s.getId(), student); // TESTING
        storyService.advanceStatus(s.getId(), student); // DONE
        storyService.advanceStatus(s.getId(), student); // no change
        Story done = storyRepository.findById(s.getId()).orElseThrow();
        assertEquals(StoryStatus.DONE, done.getStatus());
    }
}
