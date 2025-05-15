package spring.tripmate.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import spring.tripmate.domain.apiPayload.code.status.ErrorStatus;
import spring.tripmate.domain.apiPayload.exception.handler.InvalidGoogleResponseException;
import spring.tripmate.dto.GooglePlaceResponseDTO;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class GooglePlaceClient {

    @Value("${google.places.api.url}")
    private String apiUrl;

    @Value("${google.places.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public GooglePlaceResponseDTO getLocation(String query) {
        URI requestUrl = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParam("query", query)
                .queryParam("key", apiKey)
                .build(false)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("Accept-Language", "ko");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> raw = restTemplate.exchange(
                requestUrl, HttpMethod.GET, entity, String.class
        );

        // 응답 바디 추출
        String rawJson = raw.getBody();

        // JSON 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        GooglePlaceResponseDTO response = null;
        try {
            response = objectMapper.readValue(rawJson, GooglePlaceResponseDTO.class);
        } catch (JsonProcessingException e) {
            throw new InvalidGoogleResponseException(ErrorStatus.INVALID_GOOGLE_RESPONSE);
        }

        return response;

    }
}

