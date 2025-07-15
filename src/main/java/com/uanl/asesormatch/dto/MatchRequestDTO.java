package com.uanl.asesormatch.dto;

public class MatchRequestDTO {
	private Long studentId;

	public MatchRequestDTO() {
	}

	public MatchRequestDTO(Long studentId) {
		this.studentId = studentId;
	}

	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}
}
