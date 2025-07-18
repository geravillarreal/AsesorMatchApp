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
import com.uanl.asesormatch.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

	@Test
	void updateMatchStatusRejectedNotifiesStudent() {
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

		Match match = new Match();
		match.setStudent(student);
		match.setAdvisor(advisor);
		match.setCompatibilityScore(0.5);
		match.setStatus(MatchStatus.PENDING);
		matchRepository.save(match);

		matchingService.updateMatchStatus(match.getId(), MatchStatus.REJECTED);

		Match updated = matchRepository.findById(match.getId()).orElseThrow();
		assertEquals(MatchStatus.REJECTED, updated.getStatus());

		var notifications = notificationRepository.findByUserOrderByCreatedAtDesc(student);
		assertEquals(1, notifications.size());
		String msg = notifications.get(0).getMessage();
		assertEquals("El maestro " + advisor.getFullName() + " no acept\u00F3 ser tu tutor.", msg);
	}

	@Test
	void acceptingMatchCancelsOtherMatchesForStudent() {
		User student = new User();
		student.setFullName("Student Test");
		student.setEmail("student@test.com");
		student.setRole(Role.STUDENT);
		userRepository.save(student);

		User advisor1 = new User();
		advisor1.setFullName("Advisor One");
		advisor1.setEmail("advisor1@test.com");
		advisor1.setRole(Role.ADVISOR);
		userRepository.save(advisor1);

		User advisor2 = new User();
		advisor2.setFullName("Advisor Two");
		advisor2.setEmail("advisor2@test.com");
		advisor2.setRole(Role.ADVISOR);
		userRepository.save(advisor2);

		Match match1 = new Match();
		match1.setStudent(student);
		match1.setAdvisor(advisor1);
		match1.setCompatibilityScore(0.6);
		match1.setStatus(MatchStatus.PENDING);
		matchRepository.save(match1);

		Match match2 = new Match();
		match2.setStudent(student);
		match2.setAdvisor(advisor2);
		match2.setCompatibilityScore(0.7);
		match2.setStatus(MatchStatus.PENDING);
		matchRepository.save(match2);

		matchingService.updateMatchStatus(match1.getId(), MatchStatus.ACCEPTED);

		Match updated1 = matchRepository.findById(match1.getId()).orElseThrow();
		Match updated2 = matchRepository.findById(match2.getId()).orElseThrow();

		assertEquals(MatchStatus.ACCEPTED, updated1.getStatus());
		assertEquals(MatchStatus.REJECTED, updated2.getStatus());

		var notifications = notificationRepository.findByUserOrderByCreatedAtDesc(student);
		assertEquals(1, notifications.size());
		String msg = notifications.get(0).getMessage();
		assertEquals("El maestro " + advisor1.getFullName() + " aprob\u00F3 ser tu tutor.", msg);
	}

	@Test
        void requestMatchFailsIfAcceptedMatchExists() {
		User student = new User();
		student.setFullName("Student Test");
		student.setEmail("student2@test.com");
		student.setRole(Role.STUDENT);
		userRepository.save(student);

		User advisor1 = new User();
		advisor1.setFullName("Advisor One");
		advisor1.setEmail("advisor1@test.com");
		advisor1.setRole(Role.ADVISOR);
		userRepository.save(advisor1);

		User advisor2 = new User();
		advisor2.setFullName("Advisor Two");
		advisor2.setEmail("advisor2@test.com");
		advisor2.setRole(Role.ADVISOR);
		userRepository.save(advisor2);

                Match match = new Match();
                match.setStudent(student);
                match.setAdvisor(advisor1);
                match.setCompatibilityScore(0.9);
                match.setStatus(MatchStatus.ACCEPTED);
                matchRepository.save(match);

                Project project = new Project();
                project.setTitle("P1");
                project.setDescription("D1");
                project.setStatus(ProjectStatus.IN_PROGRESS);
                project.setStudent(student);
                project.setAdvisor(advisor1);
                projectRepository.save(project);

                assertThrows(IllegalStateException.class,
                                () -> matchingService.requestMatch(student.getId(), advisor2.getId(), 0.8));
        }

        @Test
        void requestMatchAllowedAfterProjectCompleted() {
                User student = new User();
                student.setFullName("Student Test");
                student.setEmail("student3@test.com");
                student.setRole(Role.STUDENT);
                userRepository.save(student);

                User advisor1 = new User();
                advisor1.setFullName("Advisor One");
                advisor1.setEmail("advisor1b@test.com");
                advisor1.setRole(Role.ADVISOR);
                userRepository.save(advisor1);

                User advisor2 = new User();
                advisor2.setFullName("Advisor Two");
                advisor2.setEmail("advisor2b@test.com");
                advisor2.setRole(Role.ADVISOR);
                userRepository.save(advisor2);

                Match match = new Match();
                match.setStudent(student);
                match.setAdvisor(advisor1);
                match.setCompatibilityScore(0.9);
                match.setStatus(MatchStatus.ACCEPTED);
                matchRepository.save(match);

                Project project = new Project();
                project.setTitle("P1");
                project.setDescription("D1");
                project.setStatus(ProjectStatus.COMPLETED);
                project.setStudent(student);
                project.setAdvisor(advisor1);
                projectRepository.save(project);

                matchingService.requestMatch(student.getId(), advisor2.getId(), 0.8);

                List<Match> matches = matchRepository.findAll();
                assertEquals(2, matches.size());
        }

        @Test
        void acceptingMatchDoesNotAssignProjectAutomatically() {
                User student = new User();
                student.setFullName("Student Test");
                student.setEmail("student4@test.com");
                student.setRole(Role.STUDENT);
                userRepository.save(student);

                User advisor = new User();
                advisor.setFullName("Advisor");
                advisor.setEmail("advisor4@test.com");
                advisor.setRole(Role.ADVISOR);
                userRepository.save(advisor);

                Project project = new Project();
                project.setTitle("P1");
                project.setDescription("D1");
                project.setStatus(ProjectStatus.DRAFT);
                project.setStudent(student);
                projectRepository.save(project);

                Match match = new Match();
                match.setStudent(student);
                match.setAdvisor(advisor);
                match.setCompatibilityScore(0.5);
                match.setStatus(MatchStatus.PENDING);
                matchRepository.save(match);

                matchingService.updateMatchStatus(match.getId(), MatchStatus.ACCEPTED);

                Project reloaded = projectRepository.findById(project.getId()).orElseThrow();
                assertNull(reloaded.getAdvisor());
                assertEquals(ProjectStatus.DRAFT, reloaded.getStatus());
        }
}
