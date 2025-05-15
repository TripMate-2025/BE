package spring.tripmate.client;

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
import spring.tripmate.domain.apiPayload.exception.handler.InvalidGooglePlaceException;
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

    public GooglePlaceResponseDTO.Result getLocation(String query) {
        URI requestUrl = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParam("query", query)
                .queryParam("key", apiKey)
                .build(false)
                .toUri();

        try {
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
            GooglePlaceResponseDTO response = objectMapper.readValue(rawJson, GooglePlaceResponseDTO.class);

            // 3. 결과가 비어있으면 예외 발생
            if (response.getResults() == null || response.getResults().isEmpty()) {
                throw new InvalidGooglePlaceException(ErrorStatus.INVALID_GOOGLE_PLACE);
            }

            return response.getResults().get(0);

        } catch (Exception e) {
            e.printStackTrace();
            throw new InvalidGooglePlaceException(ErrorStatus.INVALID_GOOGLE_PLACE);
        }
    }
}

