package com.uanl.asesormatch.client;

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

import java.util.List;

@Component
public class MatchingEngineClient {

	public List<RecommendationDTO> getRecommendations(Long studentId) {
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://localhost:8000/match/calculate";

		MatchRequestDTO requestDTO = new MatchRequestDTO(studentId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<MatchRequestDTO> request = new HttpEntity<>(requestDTO, headers);

		ResponseEntity<List<RecommendationDTO>> response = restTemplate.exchange(url, HttpMethod.POST, request,
				new ParameterizedTypeReference<List<RecommendationDTO>>() {
				});

		return response.getBody();
	}
}