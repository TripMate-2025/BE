package spring.tripmate.domain.enums;

public enum CategoryType {
    SIGHTSEEING("관광명소"),
    FOOD("음식"),
    CAFE("카페"),
    SHOPPING("쇼핑"),
    NATURE("자연"),
    CULTURE("문화"),
    ACTIVITY("체험"),
    RELAX("휴식"),
    NIGHT("야경/밤"),
    OTHER("기타");

    private final String displayName;

    CategoryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
