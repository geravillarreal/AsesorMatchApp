package com.uanl.asesormatch.config;

import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component

public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final String advisorOverrideEmail;

    public CustomOAuth2SuccessHandler(UserRepository userRepository,
                                      @Value("${advisor.override-email:}") String advisorOverrideEmail) {
        this.userRepository = userRepository;
        this.advisorOverrideEmail = advisorOverrideEmail;
    }

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();
        boolean incognito = false;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("incognito".equals(c.getName()) && "true".equals(c.getValue())) {
                    incognito = true;
                    break;
                }
            }
        }
        if (incognito && advisorOverrideEmail != null && !advisorOverrideEmail.isBlank()) {
            request.getSession().setAttribute("overrideEmail", advisorOverrideEmail);
            email = advisorOverrideEmail;
        } else {
            request.getSession().removeAttribute("overrideEmail");
        }
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

		response.sendRedirect("/dashboard");
	}
}