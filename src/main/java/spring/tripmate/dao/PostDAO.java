package spring.tripmate.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.tripmate.domain.Post;

public interface PostDAO extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    Page<Post> findByPlanId(Long planId, Pageable pageable);
    Page<Post> findByWriterIdOrderByUpdatedAtDesc(Long writerId, Pageable pageable);
}
