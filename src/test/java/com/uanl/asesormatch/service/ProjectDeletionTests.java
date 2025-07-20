package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.uanl.asesormatch.config.TestEntityScanConfig;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestEntityScanConfig.class)
class ProjectDeletionTests {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void completedProjectIsSoftDeleted() {
        User student = new User();
        student.setFullName("Student");
        student.setEmail("s@test.com");
        student.setRole(Role.STUDENT);
        userRepository.save(student);

        User advisor = new User();
        advisor.setFullName("Advisor");
        advisor.setEmail("a@test.com");
        advisor.setRole(Role.ADVISOR);
        userRepository.save(advisor);

        Project project = new Project();
        project.setTitle("P1");
        project.setDescription("D1");
        project.setStudent(student);
        project.setAdvisor(advisor);
        project.setStatus(ProjectStatus.COMPLETED);
        projectRepository.save(project);

        project.setDeleted(true);
        projectRepository.save(project);

        Project stored = projectRepository.findById(project.getId()).orElseThrow();
        assertTrue(stored.isDeleted());

        assertTrue(projectRepository.findByStudentAndDeletedFalse(student).isEmpty());
        long count = projectRepository.countByAdvisorAndStatus(advisor, ProjectStatus.COMPLETED);
        assertEquals(1, count);
    }
}
