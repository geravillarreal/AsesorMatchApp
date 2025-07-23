package com.uanl.asesormatch.repository;

import com.uanl.asesormatch.entity.Project;
import com.uanl.asesormatch.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
	List<Story> findByProjectOrderByCreatedAtAsc(Project project);

	long countByProjectAndStatusNot(Project project, com.uanl.asesormatch.enums.StoryStatus status);
}
