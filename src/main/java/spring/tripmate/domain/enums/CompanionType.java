package spring.tripmate.domain.enums;

public enum CompanionType {
    ALONE("혼자"),
    FRIEND("친구와"),
    COUPLE("연인과"),
    SPOUSE("배우자와"),
    CHILD("아이와"),
    PARENTS("부모님과"),
    FAMILY("가족과"),
    OTHER("기타");

    private final String description;

    CompanionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
