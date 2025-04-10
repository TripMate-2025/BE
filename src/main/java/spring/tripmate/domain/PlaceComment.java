package spring.tripmate.domain;

import jakarta.persistence.*;
import lombok.*;
import spring.tripmate.domain.common.BaseEntity;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "place_comments")
public class PlaceComment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "consumer_id", nullable = false)
    private Consumer writer;

    @ManyToOne
    @JoinColumn(name = "place_id", nullable = false)
    private TravelPlace place;
}
