package com.uanl.asesormatch.entity;

import java.util.ArrayList;
import java.util.List;

import com.uanl.asesormatch.dto.BookDTO;
import com.uanl.asesormatch.dto.ProfileDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id = null;

	@ElementCollection
	private List<String> interests = new ArrayList<>();

	@ElementCollection
	private List<String> areas = new ArrayList<>();

	@ElementCollection
	private List<String> availability = new ArrayList<>();

	@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Book> books = new ArrayList<>();

	private String level = null;
	private String modality = null;
	private String language = null;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user = null;

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

	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ProfileDTO getDTO() {
		ProfileDTO dto = new ProfileDTO();
		dto.setAreas(areas);
		dto.setAvailability(availability);
		dto.setId(id);
		dto.setInterests(interests);
		dto.setLanguage(language);
		dto.setLevel(level);
		dto.setModality(modality);
		dto.setUser(user);
		
		for (Book book : this.getBooks()) {
			BookDTO dtoBook = new BookDTO();
			dtoBook.setTitle(book.getTitle());
			dtoBook.setDescription(book.getDescription());
			dto.getBooks().add(dtoBook);
		}
		
		return dto;
	}
}