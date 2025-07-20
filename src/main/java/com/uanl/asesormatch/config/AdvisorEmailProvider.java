package com.uanl.asesormatch.config;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpSession;

@Component
public class AdvisorEmailProvider {
    public AdvisorEmailProvider() {
    }

    public String resolveEmail(OidcUser oidcUser) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpSession session = attrs.getRequest().getSession(false);
            if (session != null) {
                String override = (String) session.getAttribute("overrideEmail");
                if (override != null) {
                    return override;
                }
            }
        }
        return oidcUser.getEmail();
    }
}
