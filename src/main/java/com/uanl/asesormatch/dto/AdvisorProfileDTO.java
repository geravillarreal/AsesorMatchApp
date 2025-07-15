package com.uanl.asesormatch.dto;

import java.util.List;

public record AdvisorProfileDTO(
        Long   id,
        String fullName,
        String email,
        String faculty,
        List<String> areas,
        List<String> interests,
        String language
) {}
