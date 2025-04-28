package spring.tripmate.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.tripmate.domain.PostImage;

public interface PostImageDAO extends JpaRepository<PostImage, Long> {
    Page<PostImage> findByPostId(Long postId, PageRequest pageRequest);
}
