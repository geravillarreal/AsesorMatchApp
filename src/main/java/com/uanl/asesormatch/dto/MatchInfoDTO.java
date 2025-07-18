package com.uanl.asesormatch.dto;

public record MatchInfoDTO(Long id, String otherUserName, String projectTitle,
                           Integer myRating, String myComment,
                           Integer otherRating, String otherComment) {
}
