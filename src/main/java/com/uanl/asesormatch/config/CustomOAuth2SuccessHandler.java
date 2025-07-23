package com.uanl.asesormatch.config;

import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component

public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

	private final UserRepository userRepository;
	private final AdvisorEmailProvider emailProvider;

	public CustomOAuth2SuccessHandler(UserRepository userRepository, AdvisorEmailProvider emailProvider) {
		this.userRepository = userRepository;
		this.emailProvider = emailProvider;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
		String email = emailProvider.resolveEmail(oidcUser);
		String name = oidcUser.getFullName();
		String universityId = oidcUser.getPreferredUsername();

		Optional<User> existingUser = userRepository.findByEmail(email);

		if (existingUser.isEmpty()) {
			User user = new User();
			user.setEmail(email);
			user.setFullName(name);
			user.setUniversityId(universityId);
			user.setRole(Role.STUDENT);
			user.setFaculty("Unknown");
			user.setLastLogin(LocalDateTime.now());
			userRepository.save(user);
		} else {
			User user = existingUser.get();
			user.setLastLogin(LocalDateTime.now());
			userRepository.save(user);
		}

		HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
		SavedRequest savedRequest = requestCache.getRequest(request, response);
		String targetUrl = savedRequest != null ? savedRequest.getRedirectUrl() : "/dashboard";
		if (savedRequest != null) {
			requestCache.removeRequest(request, response);
		}
		response.sendRedirect(targetUrl);
	}
}