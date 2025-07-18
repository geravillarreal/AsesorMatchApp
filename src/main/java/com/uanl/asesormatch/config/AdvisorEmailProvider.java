package com.uanl.asesormatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class AdvisorEmailProvider {
    private final String advisorEmail;

    public AdvisorEmailProvider(@Value("${advisor.override-email:}") String advisorEmail) {
        this.advisorEmail = advisorEmail;
    }

    public String resolveEmail(OidcUser oidcUser) {
        if (advisorEmail != null && !advisorEmail.isBlank()) {
            return advisorEmail;
        }
        return oidcUser.getEmail();
    }
}
