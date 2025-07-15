package com.uanl.asesormatch.controller.student;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.uanl.asesormatch.dto.BookDTO;
import com.uanl.asesormatch.dto.ProfileDTO;
import com.uanl.asesormatch.entity.Book;
import com.uanl.asesormatch.entity.Profile;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.ProfileRepository;
import com.uanl.asesormatch.repository.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class StudentProfileController {

	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;

	public StudentProfileController(UserRepository userRepository, ProfileRepository profileRepository) {
		this.userRepository = userRepository;
		this.profileRepository = profileRepository;
	}

	@GetMapping("/edit")
	public String editProfileForm(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
		User user = userRepository.findByEmail(oidcUser.getEmail()).orElseThrow();
		model.addAttribute("profile", user.getProfile() == null ? new Profile() : user.getProfile());
		return "edit-profile";
	}

	@PostMapping("/edit")
	public String updateProfile(@AuthenticationPrincipal OidcUser oidcUser,
			@Valid @ModelAttribute("profile") ProfileDTO dto, BindingResult result, Model model) {

		if (result.hasErrors()) {
			model.addAttribute("profile", dto);
			return "edit-profile";
		}

		User user = userRepository.findByEmail(oidcUser.getEmail()).orElseThrow();
		Profile profile = user.getProfile() == null ? new Profile() : user.getProfile();

		profile.setAreas(dto.getAreas());
		profile.setAvailability(dto.getAvailability());
		profile.setInterests(dto.getInterests());
		profile.setLanguage(dto.getLanguage());
		profile.setModality(dto.getModality());
		profile.setLevel(dto.getLevel());
		profile.setUser(user);

		profile.getBooks().clear();
		for (BookDTO bDto : dto.getBooks()) {
			Book book = new Book();
			book.setTitle(bDto.getTitle());
			book.setDescription(bDto.getDescription());
			book.setProfile(profile);
			profile.getBooks().add(book);
		}

		profileRepository.save(profile);
		return "redirect:/dashboard";
	}

}