package com.uanl.asesormatch.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.uanl.asesormatch.dto.BookDTO;
import com.uanl.asesormatch.dto.ProfileDTO;
import com.uanl.asesormatch.dto.RecommendationDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MatchingEngineClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<RecommendationDTO> getRecommendations(ProfileDTO studentVector) {
        String url = "http://localhost:5000/recommend";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();

        payload.put("interests", studentVector.getInterests());
        payload.put("areas", studentVector.getAreas());
        payload.put("availability", studentVector.getAvailability());
        payload.put("level", studentVector.getLevel());
        payload.put("modality", studentVector.getModality());
        payload.put("language", studentVector.getLanguage());

        if (studentVector.getUserId() != null) {
            payload.put("userId", studentVector.getUserId());
        }

        if (studentVector.getBooks() != null) {
            List<Map<String, Object>> booksList = new ArrayList<>();
            for (BookDTO book : studentVector.getBooks()) {
                Map<String, Object> bookMap = new HashMap<>();
                bookMap.put("title", book.getTitle());
                bookMap.put("description", book.getDescription());
                booksList.add(bookMap);
            }
            payload.put("books", booksList);
        }

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<List<RecommendationDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }
}