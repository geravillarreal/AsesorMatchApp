package com.uanl.asesormatch.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;

public interface MatchRepository extends JpaRepository<Match, Long> {
	List<Match> findByStudent(User student);

	List<Match> findByStudentAndStatusNot(User student, MatchStatus status);

	boolean existsByStudentIdAndAdvisorIdAndStatus(Long studentId, Long advisorId, MatchStatus status);

	Optional<Match> findByStudentIdAndAdvisorId(Long studentId, Long advisorId);

	boolean existsByStudentIdAndAdvisorId(Long studentId, Long advisorId);

	List<Match> findByAdvisor(User advisor);

	@Query("SELECT m FROM Match m WHERE m.advisor = :advisor AND m.status NOT IN (:excludedStatuses)")
	List<Match> findByAdvisorAndStatusNotIn(@Param("advisor") User advisor,
			@Param("excludedStatuses") List<MatchStatus> excludedStatuses);

	List<Match> findByAdvisorAndStatus(User advisor, MatchStatus accepted);

	List<Match> findTop5ByAdvisorAndStatusOrderByCreatedAtDesc(User advisor, MatchStatus status);

	boolean existsByStudentIdAndStatus(Long studentId, MatchStatus status);
}