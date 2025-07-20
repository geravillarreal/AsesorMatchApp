# AsesorMatchApp

This project uses Spring Security with OIDC. When obtaining the authenticated user's email, always use `AdvisorEmailProvider.resolveEmail(OidcUser)` instead of calling `OidcUser#getEmail()` directly. This ensures that any session override logic is respected.
