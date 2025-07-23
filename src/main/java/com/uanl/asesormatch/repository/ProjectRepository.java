package com.uanl.asesormatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.enums.ProjectStatus;

public interface ProjectRepository extends JpaRepository<Project, Long> {
	List<Project> findByAdvisor(User advisor);

	List<Project> findByStudent(User user);

	List<Project> findByStudentAndDeletedFalse(User user);

	long countByAdvisorAndStatus(User advisor, ProjectStatus status);

	List<Project> findByStudentAndAdvisorAndStatus(User student, User advisor, ProjectStatus status);

	List<Project> findByStudentAndAdvisorAndStatusAndDeletedFalse(User student, User advisor, ProjectStatus status);

	List<Project> findByAdvisorAndStatus(User advisor, ProjectStatus status);

	List<Project> findByAdvisorAndStatusAndDeletedFalse(User advisor, ProjectStatus status);

	boolean existsByStudentAndStatus(User student, ProjectStatus status);

	boolean existsByStudentAndStatusAndDeletedFalse(User student, ProjectStatus status);
}