package spring.tripmate.domain.enums;

public enum StyleType {
    HEALING("힐링 여행"),
    HOT_PLACE("SNS 핫플레이스"),
    FOOD_TOUR("맛집 탐방"),
    LOCAL_FESTIVAL("지역 축제"),
    ACTIVITY("액티비티 여행"),
    CITY_TOUR("쇼핑ㆍ도시 여행"),
    MARINE_SPORTS("해양 스포츠"),
    ROAD_TRIP("로드 트립"),
    NATURE("자연 탐험"),
    CULTURE_HISTORY("문화ㆍ역사"),
    TOURIST_SPOT("유명 관광지");

    private final String description;

    StyleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
