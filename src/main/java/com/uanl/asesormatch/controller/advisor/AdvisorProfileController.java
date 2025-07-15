package com.uanl.asesormatch.controller.advisor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.uanl.asesormatch.entity.User;
import com.uanl.asesormatch.repository.UserRepository;

@Controller
public class AdvisorProfileController {

    private final UserRepository userRepository;

    public AdvisorProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/advisor/{id}")
    public String viewAdvisor(@PathVariable Long id, Model model) {
        User advisor = userRepository.findById(id).orElseThrow();

        model.addAttribute("advisor", advisor);
        model.addAttribute("profile", advisor.getProfile());

        return "advisor-detail";
    }
}