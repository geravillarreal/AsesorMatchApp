package com.uanl.asesormatch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.User;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByAdvisor(User advisor);

	List<Project> findByStudent(User user);
}