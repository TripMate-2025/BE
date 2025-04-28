package spring.tripmate.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.tripmate.domain.Post;

public interface PostDAO extends JpaRepository<Post, Long> {
    Page<Post> findAll(PageRequest pageRequest);
    Page<Post> findByPlanId(Long planId, PageRequest pageRequest);
    Page<Post> findByConsumerId(Long consumerId, PageRequest pageRequest);
}
