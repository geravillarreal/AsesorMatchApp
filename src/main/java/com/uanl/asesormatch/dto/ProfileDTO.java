package com.uanl.asesormatch.dto;

import java.util.ArrayList;
import java.util.List;


import jakarta.validation.constraints.NotEmpty;

public class ProfileDTO {
	private Long id = null;
	
	@NotEmpty(message = "Interests must not be empty")
	private List<String> interests = new ArrayList<>();
	
	@NotEmpty(message = "Areas must not be empty")
	private List<String> areas = new ArrayList<>();
	
	@NotEmpty(message = "Availability must not be empty")
	private List<String> availability = new ArrayList<>();
	private List<BookDTO> books = new ArrayList<>();
	private String level = null;
	private String modality = null;
        private String language = null;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<String> getInterests() {
		return interests;
	}

	public void setInterests(List<String> interests) {
		this.interests = interests;
	}

	public List<String> getAreas() {
		return areas;
	}

	public void setAreas(List<String> areas) {
		this.areas = areas;
	}

	public List<String> getAvailability() {
		return availability;
	}

	public void setAvailability(List<String> availability) {
		this.availability = availability;
	}

	public List<BookDTO> getBooks() {
		return books;
	}

	public void setBooks(List<BookDTO> books) {
		this.books = books;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getModality() {
		return modality;
	}

	public void setModality(String modality) {
		this.modality = modality;
	}

        public String getLanguage() {
                return language;
        }

	public void setLanguage(String language) {
		this.language = language;
	}

        @Override
        public String toString() {
                return "ProfileDTO [id=" + id + ", interests=" + interests + ", areas=" + areas + ", availability="
                                + availability + ", books=" + books + ", level=" + level + ", modality=" + modality
                                + ", language=" + language + "]";
        }
}
