package com.uanl.asesormatch.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.uanl.asesormatch.dto.MatchRequestDTO;
import com.uanl.asesormatch.dto.RecommendationDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MatchingEngineClient {

    private final String baseUrl;

    public MatchingEngineClient(@Value("${matching.engine.base-url:http://localhost:8000}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<RecommendationDTO> getRecommendations(Long studentId) {
        RestTemplate restTemplate = new RestTemplate();

        // Login to obtain JWT token
        String loginUrl = baseUrl + "/login";
        Map<String, Long> loginBody = new HashMap<>();
        loginBody.put("userId", studentId);
        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Long>> loginRequest = new HttpEntity<>(loginBody, loginHeaders);
        ResponseEntity<Map<String, Object>> loginResponse = restTemplate.exchange(
            loginUrl,
            HttpMethod.POST,
            loginRequest,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        String token = loginResponse.getBody() != null ? (String) loginResponse.getBody().get("token") : null;

        String url = baseUrl + "/match/calculate";
        MatchRequestDTO requestDTO = new MatchRequestDTO(studentId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        HttpEntity<MatchRequestDTO> request = new HttpEntity<>(requestDTO, headers);

        ResponseEntity<List<RecommendationDTO>> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            new ParameterizedTypeReference<List<RecommendationDTO>>() {}
        );

        return response.getBody();
    }
}
