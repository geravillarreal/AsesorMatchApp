package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.enums.ProjectStatus;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MatchingService {

	private final MatchRepository matchRepository;
	private final UserRepository userRepository;
	private final ProjectRepository projectRepository;
	private final NotificationService notificationService;
	private static final Logger logger = LogManager.getLogger(MatchingService.class);

	public MatchingService(MatchRepository matchRepository, UserRepository userRepository,
			ProjectRepository projectRepository, NotificationService notificationService) {
		this.matchRepository = matchRepository;
		this.userRepository = userRepository;
		this.projectRepository = projectRepository;
		this.notificationService = notificationService;
	}

	public List<Match> createMatches(User student, List<Map<String, Object>> recommendations) {
		logger.info("Creating matches for student {}", student.getId());
		List<Match> matches = new ArrayList<>();

		for (Map<String, Object> rec : recommendations) {
			Long advisorId = ((Number) rec.get("advisorId")).longValue();
			Double score = ((Number) rec.get("score")).doubleValue();
			User advisor = userRepository.findById(advisorId).orElse(null);

			if (advisor != null) {
				boolean alreadyExists = matchRepository.existsByStudentIdAndAdvisorId(student.getId(), advisorId);

				if (!alreadyExists) {
					Match match = new Match();
					match.setStudent(student);
					match.setAdvisor(advisor);
					match.setCompatibilityScore(score);
					match.setCreatedAt(LocalDateTime.now());
					match.setStatus(MatchStatus.PENDING);
					match.setAlgorithmUsed("KNN-MOCK");

					matches.add(matchRepository.save(match));
					logger.debug("Created match between student {} and advisor {}", student.getId(), advisorId);
				}
			}
		}

		logger.info("Created {} matches for student {}", matches.size(), student.getId());
		return matches;
	}

        public List<Match> getMatchesForStudent(User student) {
                return matchRepository.findByStudentAndStatusNot(student, MatchStatus.REJECTED);
        }

        public double computeCompatibility(Set<String> studentInterests, Set<String> advisorAreas) {
                if (studentInterests == null || studentInterests.isEmpty()) {
                        return 0.0;
                }
                if (advisorAreas == null) {
                        advisorAreas = Set.of();
                }
                long matches = studentInterests.stream().filter(advisorAreas::contains).count();
                return (double) matches / studentInterests.size();
        }

	public void updateMatchStatus(Long matchId, MatchStatus status) {
		logger.info("Updating match {} to status {}", matchId, status);
		var matchOpt = matchRepository.findById(matchId);
		matchOpt.ifPresent(m -> {
			m.setStatus(status);
			matchRepository.save(m);

			if (status == MatchStatus.ACCEPTED) {
				// Cancel other matches for this student
				var allMatches = matchRepository.findByStudent(m.getStudent());
				for (var other : allMatches) {
					if (!other.getId().equals(m.getId()) && other.getStatus() != MatchStatus.REJECTED) {
						other.setStatus(MatchStatus.REJECTED);
						matchRepository.save(other);
					}
				}

				String msg = "Advisor " + m.getAdvisor().getFullName() + " agreed to be your mentor.";
				notificationService.notify(m.getStudent(), msg);
			} else if (status == MatchStatus.REJECTED) {
				String msg = "Advisor " + m.getAdvisor().getFullName() + " declined to be your mentor.";
				notificationService.notify(m.getStudent(), msg);
			}
			logger.info("Match {} updated to {}", m.getId(), status);
		});
	}

	public void requestMatch(Long studentId, Long advisorId, Double score) {
		logger.info("Student {} requesting advisor {} with score {}", studentId, advisorId, score);
		User student = userRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("student"));
		User advisor = userRepository.findById(advisorId).orElseThrow(() -> new IllegalArgumentException("advisor"));

		boolean hasDraft = projectRepository.existsByStudentAndStatus(student, ProjectStatus.DRAFT);
		if (!hasDraft) {
			throw new IllegalStateException("student has no draft projects");
		}

		boolean ongoingProject = projectRepository.existsByStudentAndStatus(student, ProjectStatus.IN_PROGRESS);
		if (ongoingProject) {
			throw new IllegalStateException("student already has an accepted match");
		}

		Match match = matchRepository.findByStudentIdAndAdvisorId(studentId, advisorId).orElse(null);
		if (match == null) {
			match = new Match();
			match.setStudent(student);
			match.setAdvisor(advisor);
		}

		match.setCompatibilityScore(score);
		match.setStatus(MatchStatus.PENDING);
		match.setCreatedAt(LocalDateTime.now());

		matchRepository.save(match);

		String msg = "Student " + student.getFullName() + " requested you to be their mentor.";
		notificationService.notify(advisor, msg);
		logger.info("Match request saved: student {} -> advisor {}", student.getId(), advisor.getId());
	}
}