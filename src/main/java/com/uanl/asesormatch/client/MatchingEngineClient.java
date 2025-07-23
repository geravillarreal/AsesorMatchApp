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

import com.uanl.asesormatch.dto.RecommendationDTO;

import net.minidev.json.JSONObject;

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
		JSONObject json = new JSONObject();
		json.put("userId", String.valueOf(studentId));

		HttpHeaders loginHeaders = new HttpHeaders();
		loginHeaders.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> loginRequest = new HttpEntity<>(json.toString(), loginHeaders);

		ResponseEntity<Map<String, Object>> loginResponse = restTemplate.exchange(loginUrl, HttpMethod.POST,
				loginRequest, new ParameterizedTypeReference<Map<String, Object>>() {
				});
		String token = loginResponse.getBody() != null ? (String) loginResponse.getBody().get("token") : null;

		String url = baseUrl + "/match/calculate";
		json = new JSONObject();
		json.put("studentId", String.valueOf(studentId));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (token != null) {
			headers.setBearerAuth(token);
		}
		HttpEntity<String> request = new HttpEntity<>(json.toString(), headers);
		ResponseEntity<List<RecommendationDTO>> response = restTemplate.exchange(url, HttpMethod.POST, request,
				new ParameterizedTypeReference<List<RecommendationDTO>>() {
				});

		return response.getBody();
	}
}
