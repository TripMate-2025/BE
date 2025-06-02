package spring.tripmate.domain.mapping;

import jakarta.persistence.*;
import lombok.*;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.TravelRoom;
import spring.tripmate.domain.common.BaseEntity;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Table(name = "room_members")
public class RoomMember extends BaseEntity {
    @EmbeddedId
    private RoomMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("consumerId")
    @JoinColumn(name="consumer_id", nullable = false)
    private Consumer member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name="room_id", nullable = false)
    private TravelRoom room;
}
