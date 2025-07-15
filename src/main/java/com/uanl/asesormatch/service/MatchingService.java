package com.uanl.asesormatch.service;

import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;
import com.uanl.asesormatch.repository.MatchRepository;
import com.uanl.asesormatch.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MatchingService {

	private final MatchRepository matchRepository;
	private final UserRepository userRepository;

	public MatchingService(MatchRepository matchRepository, UserRepository userRepository) {
		this.matchRepository = matchRepository;
		this.userRepository = userRepository;
	}

	public List<Match> createMatches(User student, List<Map<String, Object>> recommendations) {
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
				}
			}
		}

		return matches;
	}

	public List<Match> getMatchesForStudent(User student) {
		return matchRepository.findByStudent(student);
	}

	public void updateMatchStatus(Long matchId, MatchStatus status) {
		var match = matchRepository.findById(matchId);
		match.ifPresent(m -> {
			m.setStatus(status);
			matchRepository.save(m);
		});
	}

	public void requestMatch(Long studentId, Long advisorId, Double score) {
		User student = userRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("student"));
		User advisor = userRepository.findById(advisorId).orElseThrow(() -> new IllegalArgumentException("advisor"));

		Match match = new Match();
		match.setStudent(student);
		match.setAdvisor(advisor);
		match.setCompatibilityScore(score);
		match.setStatus(MatchStatus.PENDING);
		match.setCreatedAt(LocalDateTime.now());

		matchRepository.save(match);
	}
}