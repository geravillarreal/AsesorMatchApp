package com.uanl.asesormatch.entity;

import com.uanl.asesormatch.enums.StoryStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stories")
public class Story {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	@Column(length = 1000)
	private String description;

	@Enumerated(EnumType.STRING)
	private StoryStatus status;

	@ManyToOne
	private User author;

	@ManyToOne
	private Project project;

	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public StoryStatus getStatus() {
		return status;
	}

	public void setStatus(StoryStatus status) {
		this.status = status;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
