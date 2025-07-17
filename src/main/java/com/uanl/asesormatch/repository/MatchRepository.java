package com.uanl.asesormatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;

public interface MatchRepository extends JpaRepository<Match, Long> {
	List<Match> findByStudent(User student);

	boolean existsByStudentIdAndAdvisorIdAndStatus(Long studentId, Long advisorId, MatchStatus status);
	
        boolean existsByStudentIdAndAdvisorId(Long studentId, Long advisorId);

        List<Match> findByAdvisor(User advisor);

        List<Match> findByAdvisorAndStatus(User advisor, MatchStatus accepted);

        boolean existsByStudentIdAndStatus(Long studentId, MatchStatus status);
}