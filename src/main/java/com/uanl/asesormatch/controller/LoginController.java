package com.uanl.asesormatch.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final String advisorOverrideEmail;

    public LoginController(@Value("${advisor.override-email}") String advisorOverrideEmail) {
        this.advisorOverrideEmail = advisorOverrideEmail;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String as,
                            HttpSession session) {
        if ("advisor".equalsIgnoreCase(as) && advisorOverrideEmail != null && !advisorOverrideEmail.isBlank()) {
            session.setAttribute("overrideEmail", advisorOverrideEmail);
        } else {
            session.removeAttribute("overrideEmail");
        }
        return "login";
    }
}