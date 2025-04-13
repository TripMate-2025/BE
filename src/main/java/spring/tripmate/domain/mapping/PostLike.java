package spring.tripmate.domain.mapping;

import jakarta.persistence.*;
import lombok.*;
import spring.tripmate.domain.*;
import spring.tripmate.domain.common.BaseEntity;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "post_likes")
public class PostLike extends BaseEntity {
    @EmbeddedId
    private PostLikeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("consumerId")
    @JoinColumn(name="consumer_id", nullable = false)
    private Consumer consumer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name="post_id", nullable = false)
    private Post post;
}
