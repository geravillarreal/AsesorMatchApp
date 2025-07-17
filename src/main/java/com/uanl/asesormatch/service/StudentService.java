package com.uanl.asesormatch.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.uanl.asesormatch.dto.ProjectDTO;
import com.uanl.asesormatch.dto.StudentProfileDTO;
import com.uanl.asesormatch.enums.Role;
import com.uanl.asesormatch.repository.ProjectRepository;
import com.uanl.asesormatch.repository.UserRepository;

@Service
public class StudentService {
    private final UserRepository userRepo;
    private final ProjectRepository projectRepo;

    public StudentService(UserRepository userRepo, ProjectRepository projectRepo) {
        this.userRepo = userRepo;
        this.projectRepo = projectRepo;
    }

    public Optional<StudentProfileDTO> getProfile(Long id) {
        return userRepo.findById(id)
                .filter(u -> u.getRole() == Role.STUDENT)
                .map(u -> {
                    List<ProjectDTO> projects = projectRepo.findByStudent(u).stream().map(p -> {
                        ProjectDTO dto = new ProjectDTO();
                        dto.setId(p.getId());
                        dto.setTitle(p.getTitle());
                        dto.setDescription(p.getDescription());
                        dto.setStatus(p.getStatus().name());
                        return dto;
                    }).collect(Collectors.toList());
                    return new StudentProfileDTO(
                        u.getId(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getFaculty(),
                        u.getProfile() != null ? u.getProfile().getDTO() : null,
                        projects
                    );
                });
    }
}
