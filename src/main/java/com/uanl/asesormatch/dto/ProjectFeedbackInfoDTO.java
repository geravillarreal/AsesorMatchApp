package com.uanl.asesormatch.dto;

public record ProjectFeedbackInfoDTO(Long id, String otherUserName, String projectTitle,
                                     boolean myFeedbackGiven, boolean otherFeedbackGiven) {}
