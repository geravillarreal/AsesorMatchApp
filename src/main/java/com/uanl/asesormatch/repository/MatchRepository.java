package com.uanl.asesormatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uanl.asesormatch.entity.Match;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.MatchStatus;

public interface MatchRepository extends JpaRepository<Match, Long> {
	List<Match> findByStudent(User student);

        boolean existsByStudentIdAndAdvisorIdAndStatus(Long studentId, Long advisorId, MatchStatus status);

        java.util.Optional<Match> findByStudentIdAndAdvisorId(Long studentId, Long advisorId);
	
        boolean existsByStudentIdAndAdvisorId(Long studentId, Long advisorId);

        List<Match> findByAdvisor(User advisor);

        List<Match> findByAdvisorAndStatus(User advisor, MatchStatus accepted);

        List<Match> findTop5ByAdvisorAndStatusOrderByCreatedAtDesc(User advisor, MatchStatus status);

        boolean existsByStudentIdAndStatus(Long studentId, MatchStatus status);
}