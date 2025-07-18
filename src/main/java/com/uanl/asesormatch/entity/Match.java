package com.uanl.asesormatch.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;

import com.uanl.asesormatch.enums.MatchStatus;

@Entity
@Table(name = "matches")
public class Match {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private User student;

	@ManyToOne
	private User advisor;

	private Double compatibilityScore;

	private LocalDateTime createdAt;

	@Enumerated(EnumType.STRING)
	private MatchStatus status;

        private String algorithmUsed;

        @Transient
        private boolean allProjectsCompleted;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getStudent() {
		return student;
	}

	public void setStudent(User student) {
		this.student = student;
	}

	public User getAdvisor() {
		return advisor;
	}

	public void setAdvisor(User advisor) {
		this.advisor = advisor;
	}

	public Double getCompatibilityScore() {
		return compatibilityScore;
	}

	public void setCompatibilityScore(Double compatibilityScore) {
		this.compatibilityScore = compatibilityScore;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public MatchStatus getStatus() {
		return status;
	}

	public void setStatus(MatchStatus status) {
		this.status = status;
	}

	public String getAlgorithmUsed() {
		return algorithmUsed;
	}

        public void setAlgorithmUsed(String algorithmUsed) {
                this.algorithmUsed = algorithmUsed;
        }

        public boolean isAllProjectsCompleted() {
                return allProjectsCompleted;
        }

        public void setAllProjectsCompleted(boolean allProjectsCompleted) {
                this.allProjectsCompleted = allProjectsCompleted;
        }
}