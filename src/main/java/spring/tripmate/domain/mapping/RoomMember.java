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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "room_members")
public class RoomMember extends BaseEntity {
    @EmbeddedId
    private RoomMemberId id;

    @ManyToOne
    @MapsId("consumerId")
    @JoinColumn(name="consumer_id", nullable = false)
    private Consumer member;

    @ManyToOne
    @MapsId("roomId")
    @JoinColumn(name="room_id", nullable = false)
    private TravelRoom room;
}
