package spring.tripmate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import spring.tripmate.client.GeminiClient;
import spring.tripmate.client.GooglePlaceClient;
import spring.tripmate.converter.TravelPlaceConverter;
import spring.tripmate.converter.TravelPlanConverter;
import spring.tripmate.dao.ConsumerDAO;
import spring.tripmate.dao.TravelPlaceDAO;
import spring.tripmate.dao.TravelPlanDAO;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.TravelPlace;
import spring.tripmate.domain.TravelPlan;
import spring.tripmate.domain.apiPayload.code.status.ErrorStatus;
import spring.tripmate.domain.apiPayload.exception.handler.GeminiCallFailedException;
import spring.tripmate.domain.apiPayload.exception.handler.InvalidGeminiResponseException;
import spring.tripmate.domain.apiPayload.exception.handler.InvalidGooglePlaceException;
import spring.tripmate.domain.apiPayload.exception.handler.PlanHandler;
import spring.tripmate.domain.enums.StyleType;
import spring.tripmate.dto.GooglePlaceResponseDTO;
import spring.tripmate.dto.PlanRequestDTO;
import spring.tripmate.dto.PlanResponseDTO;
import spring.tripmate.security.JwtProvider;
import spring.tripmate.util.RedisUtil;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelPlanService {

    private final JwtProvider jwtProvider;
    private final GeminiClient geminiClient;
    private final GooglePlaceClient googlePlaceClient;
    private final ObjectMapper objectMapper;
    private final RedisUtil redisUtil;

    private final ConsumerDAO consumerDAO;
    private final TravelPlanDAO planDAO;
    private final TravelPlaceDAO placeDAO;

    public PlanResponseDTO.CreatePlanDTO createPlan(PlanRequestDTO.CreateDTO request){
        String prompt = buildPrompt(request);
        String result;

        try {
            result = geminiClient.getTravelPlan(prompt);
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new GeminiCallFailedException(ErrorStatus.GEMINI_API_CALL_FAILED);
        }

        String cleanJson = cleanMarkdownJson(result);

        try {
            PlanResponseDTO.CreatePlanDTO plan = objectMapper.readValue(cleanJson, PlanResponseDTO.CreatePlanDTO.class);

            //좌표 및 주소 보정
            plan.getPlaces().forEach(placeDto -> {
                GooglePlaceResponseDTO response = googlePlaceClient.getLocation(placeDto.getName());

                //검색 결과가 없는 경우->도로명 주소로 검색
                if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                    response = googlePlaceClient.getLocation(placeDto.getAddress());
                }

                //도로명 주소로 검색 후에도 검색 결과가 없는 경우->예외 발생
                if (response == null || response.getResults() == null || response.getResults().isEmpty()){
                    throw new InvalidGooglePlaceException(ErrorStatus.INVALID_GOOGLE_PLACE);
                }

                GooglePlaceResponseDTO.Result location = response.getResults().get(0);
                if (location != null) {
                    placeDto.setLatitude(location.getGeometry().getLocation().getLat());
                    placeDto.setLongitude(location.getGeometry().getLocation().getLng());
                    placeDto.setAddress(location.getFormatted_address());
                }
            });

            UUID planId = UUID.randomUUID();
            plan.setPlanId(planId);
            String key = "plan:" + planId;

            String json = objectMapper.writeValueAsString(plan);
            redisUtil.setDataExpire(key, json, 30 * 60);

            return plan;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new InvalidGeminiResponseException(ErrorStatus.INVALID_GEMINI_RESPONSE);
        }
    }

    //room에서 호출
    //@RequestHeader("Authorization") String authHeader, String token = authHeader.replace("Bearer ", "");로 token 추출해서 전달받기
    public PlanResponseDTO.PlanDTO savePlan(UUID planId, String token) {

        String email = jwtProvider.getEmailFromToken(token);

        String key = "plan:" + planId;
        String json = redisUtil.getData(key);

        PlanResponseDTO.CreatePlanDTO createPlan;
        try {
            createPlan = objectMapper.readValue(json, PlanResponseDTO.CreatePlanDTO.class);
        } catch (JsonProcessingException e) {
            throw new InvalidGeminiResponseException(ErrorStatus.INVALID_GEMINI_RESPONSE);
        }

        Consumer consumer = consumerDAO.findByEmail(email);

        TravelPlan plan = TravelPlanConverter.toPlan(consumer, createPlan);
        planDAO.save(plan);

        List<TravelPlace> places = createPlan.getPlaces().stream()
                .map(placeDto -> TravelPlaceConverter.toPlace(plan, placeDto))
                .map(placeDAO::save)
                .toList();

        return TravelPlanConverter.toPlanDTO(plan, places);
    }

    //room 기능
    public PlanResponseDTO.UpdateDTO updatePlan(Long planId, PlanRequestDTO.UpdateDTO request){

        return null;
    }

    public PlanResponseDTO.PlanDTO getPlan(Long planId){
        TravelPlan plan = planDAO.findById(planId)
                .orElseThrow(() -> new PlanHandler(ErrorStatus.PLAN_NOT_FOUND));
        List<TravelPlace> places = plan.getPlaces();

        return TravelPlanConverter.toPlanDTO(plan, places);
    }

    public List<PlanResponseDTO.PlanDTO> getPlansByTheme(String theme, int page, int size){

        Pageable pageable = PageRequest.of(page, size);

        Page<TravelPlan> plans = planDAO.findByThemeContaining(theme, pageable);

        return plans.stream()
                .map(plan -> {
                    List<TravelPlace> places = plan.getPlaces();

                    //PlanDTO로 변환
                    return TravelPlanConverter.toPlanDTO(plan, places);
                })
                .collect(Collectors.toList());
    }

    private String cleanMarkdownJson(String raw) {
        if (raw == null) return "";

        String cleaned = raw
                .replaceAll("^```json\\s*", "")  // 앞에 붙는 ```json 제거
                .replaceAll("\\s*```$", "")      // 뒤에 붙는 ``` 제거
                .trim();

        // 소수점 뒤 숫자 없이 끝나는 경우 .0으로 보정
        cleaned = cleaned.replaceAll("(\\d+)\\.(\\s|,|\\})", "$1.0$2");

        return cleaned;
    }

    private String buildPrompt(PlanRequestDTO.CreateDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append("다음 정보를 기반으로 여행 일정을 계획해줘. ");
        sb.append("여행을 계획할때는 제공한 여행 스타일이나, 추가한 요청사항을 고려해서 계획해줘. 여행 스타일 리스트가 달라지면 이를 기반으로 여행 계획도 바뀌도록 구성해줘.");
        sb.append("여행 계획 시에는 거리와 동선을 고려해서 효율적으로 계획해줘. 즉, 여행 동선의 길이가 가장 짧도록(경로 거리가 최적이도록) 장소의 순서(여행 시간)를 계획해줘.");
        sb.append("장소 정보를 제공할 때 정확한 정보만 제공해줘.(실제로 존재하는 장소, 정확한 도로명 주소/위도/경도). 특히 위도 경도 정보는 해당 장소에 맞는 정확한 정보를 제공해줘.(소숫점은 6자리까지 모두 표시)");
        sb.append("여행을 계획할때는 하루안에 이동할 수 있는 장소들을 계획하고, 장소 간의 이동시간을 고려해서 시간과 장소를 제공해줘.");
        sb.append("다음 구조로 JSON 문자열을 생성해줘");
        sb.append("출력은 반드시 JSON 형식이며, 마크다운 문법을 쓰지 마.");
        sb.append("companion, style, category는 enum 타입이기 때문에 해당될 수 있는 값들만 명확하게 구분해서 구조를 맞춰줘.\n\n");

        sb.append("국가: ").append(dto.getCountry()).append("\n");
        sb.append("도시: ").append(dto.getCity()).append("\n");
        sb.append("여행 시작일: ").append(dto.getStartDate()).append("\n");
        sb.append("여행 종료일: ").append(dto.getEndDate()).append("\n");
        sb.append("동행자 유형: ").append(dto.getCompanion()).append("\n");

        if (dto.getStyle() != null && !dto.getStyle().isEmpty()) {
            sb.append("여행 스타일: ");
            for (StyleType style : dto.getStyle()) {
                sb.append(style.name()).append(" ");
            }
            sb.append("\n");
        }

        if (dto.getCustomizing() != null && !dto.getCustomizing().isBlank()) {
            sb.append("추가 요청사항: ").append(dto.getCustomizing()).append("\n");
        }

        sb.append("DTO 구조");
        sb.append("""
                - title (string): 여행 제목
                - startDate (string): 여행 시작일, 예: "2025-06-01"
                - endDate (string): 여행 종료일, 예: "2025-06-04"
                - country (string): 여행 국가, 예: "일본"
                - city (string): 여행 도시, 예: "도쿄"
                - companion (string): 여행 동행자 유형. 다음 중 하나여야 해(enum 타입이므로 다음 {}안의 타입만 값으로 올 수 있음):{
                  - ALONE ("혼자")
                  - FRIEND ("친구와")
                  - COUPLE ("연인과")
                  - SPOUSE ("배우자와")
                  - CHILD ("아이와")
                  - PARENTS ("부모님과")
                  - FAMILY ("가족과")
                  - OTHER ("기타")
                }
                - style (array of strings): 여행 스타일. 다음 중 여러 개 선택 가능(enum 타입이므로 다음 {}안의 타입만 값으로 올 수 있음):{
                  - HEALING ("힐링 여행")
                  - HOT_PLACE ("SNS 핫플레이스")
                  - FOOD_TOUR ("맛집 탐방")
                  - LOCAL_FESTIVAL ("지역 축제")
                  - ACTIVITY ("액티비티 여행")
                  - CITY_TOUR ("쇼핑ㆍ도시 여행")
                  - MARINE_SPORTS ("해양 스포츠")
                  - ROAD_TRIP ("로드 트립")
                  - NATURE ("자연 탐험")
                  - CULTURE_HISTORY ("문화ㆍ역사")
                  - TOURIST_SPOT ("유명 관광지")
                }
                - places (array of object): 여행 일정. 각 장소는 다음 정보를 포함해야 해:
                  - name (string): 장소 이름. 부연 설명 없이 정확한 이름만 제공해줘.
                  - category (string): 장소 유형. 다음 중 하나여야 해(enum 타입이므로 다음 {}안의 타입만 값으로 올 수 있음. 특히 LOCAL_FESTIVAL은 category의 enum이 아니라 style의 enum임.):{
                    - SIGHTSEEING ("관광명소")
                    - FOOD ("음식")
                    - CAFE ("카페")
                    - SHOPPING ("쇼핑")
                    - NATURE ("자연")
                    - CULTURE ("문화")
                    - ACTIVITY ("체험")
                    - RELAX ("휴식")
                    - NIGHT ("야경/밤")
                    - OTHER ("기타")
                  }
                  - description (string): 장소에 대한 간단한 설명
                  - time (string): 방문 시간. ISO-8601 형식 (예: "2025-06-01T10:00:00")
                  - address (string): 장소 도로명 주소
                  - latitude (number): 위도
                  - longitude (number): 경도
                  - dayNumber (number): 여행 몇 번째 날인지 (예: 1)
                """);

        return sb.toString();
    }
}
