package spring.tripmate.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.tripmate.domain.mapping.PostLike;
import spring.tripmate.domain.mapping.PostLikeId;

import java.util.List;

public interface PostLikeDAO extends JpaRepository<PostLike, PostLikeId> {
    PostLike findByIdConsumerIdAndPostId(Long postId, Long consumerId);
    List<PostLike> findByPostId(Long postId);
    List<PostLike> findByConsumerId(Long consumerId);
    Boolean existsByPostIdAndConsumerId(Long postId, Long consumerId);
    Integer countByPostId(Long postId);

    void deleteById_ConsumerIdAndId_PostId(Long consumerId, Long postId);
}
