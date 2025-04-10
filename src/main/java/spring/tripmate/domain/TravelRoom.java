package spring.tripmate.domain;


import jakarta.persistence.*;
import lombok.*;
import spring.tripmate.domain.common.BaseEntity;
import spring.tripmate.domain.mapping.RoomMember;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "room_rooms")
public class TravelRoom extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="plan_id", nullable = false)
    private TravelPlan plan;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomMember> members = new ArrayList<>();
}
