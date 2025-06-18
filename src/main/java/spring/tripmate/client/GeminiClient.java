package spring.tripmate.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import spring.tripmate.dto.GeminiRequestDTO;
import spring.tripmate.dto.GeminiResponseDTO;

@Service
@RequiredArgsConstructor
public class GeminiClient {

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String requestGemini(String prompt) {
        // Gemini에 요청 전송
        String requestUrl = UriComponentsBuilder
                .fromHttpUrl(apiUrl)
                .queryParam("key", apiKey)
                .toUriString();

        GeminiRequestDTO request = new GeminiRequestDTO(prompt);
        GeminiResponseDTO response = restTemplate.postForObject(requestUrl, request, GeminiResponseDTO.class);

        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new IllegalStateException("Gemini 응답이 비어 있습니다.");
        }

        return response.getCandidates().get(0).getContent().getParts().get(0).getText();
    }
}

