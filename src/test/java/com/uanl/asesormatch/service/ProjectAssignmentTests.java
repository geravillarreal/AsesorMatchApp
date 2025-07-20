package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
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
class ProjectAssignmentTests {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MatchRepository matchRepository;

    private User student;
    private User advisor;

    @BeforeEach
    void setup() {
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

        Match match = new Match();
        match.setStudent(student);
        match.setAdvisor(advisor);
        match.setCompatibilityScore(0.8);
        match.setStatus(MatchStatus.ACCEPTED);
        matchRepository.save(match);
    }

    @Test
    void cannotAssignSecondProjectIfOneInProgress() {
        Project p1 = new Project();
        p1.setTitle("P1");
        p1.setDescription("D1");
        p1.setStudent(student);
        p1.setAdvisor(advisor);
        p1.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(p1);

        Project p2 = new Project();
        p2.setTitle("P2");
        p2.setDescription("D2");
        p2.setStudent(student);
        p2.setStatus(ProjectStatus.DRAFT);
        projectRepository.save(p2);

        boolean hasMatch = matchRepository.existsByStudentIdAndAdvisorIdAndStatus(student.getId(), advisor.getId(), MatchStatus.ACCEPTED);
        boolean hasActive = !projectRepository
                .findByStudentAndAdvisorAndStatus(student, advisor, ProjectStatus.IN_PROGRESS)
                .isEmpty();
        if (p2.getAdvisor() == null && hasMatch && !hasActive) {
            p2.setAdvisor(advisor);
            p2.setStatus(ProjectStatus.IN_PROGRESS);
            projectRepository.save(p2);
        }

        Project reloaded = projectRepository.findById(p2.getId()).orElseThrow();
        assertNull(reloaded.getAdvisor());
    }

    @Test
    void assignAllowedAfterCompletion() {
        Project p1 = new Project();
        p1.setTitle("P1");
        p1.setDescription("D1");
        p1.setStudent(student);
        p1.setAdvisor(advisor);
        p1.setStatus(ProjectStatus.COMPLETED);
        projectRepository.save(p1);

        Project p2 = new Project();
        p2.setTitle("P2");
        p2.setDescription("D2");
        p2.setStudent(student);
        p2.setStatus(ProjectStatus.DRAFT);
        projectRepository.save(p2);

        boolean hasMatch = matchRepository.existsByStudentIdAndAdvisorIdAndStatus(student.getId(), advisor.getId(), MatchStatus.ACCEPTED);
        boolean hasActive = !projectRepository
                .findByStudentAndAdvisorAndStatus(student, advisor, ProjectStatus.IN_PROGRESS)
                .isEmpty();
        if (p2.getAdvisor() == null && hasMatch && !hasActive) {
            p2.setAdvisor(advisor);
            p2.setStatus(ProjectStatus.IN_PROGRESS);
            projectRepository.save(p2);
        }

        Project reloaded = projectRepository.findById(p2.getId()).orElseThrow();
        assertEquals(advisor.getId(), reloaded.getAdvisor().getId());
        assertEquals(ProjectStatus.IN_PROGRESS, reloaded.getStatus());
    }
}
