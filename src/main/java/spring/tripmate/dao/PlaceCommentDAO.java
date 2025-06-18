package spring.tripmate.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.tripmate.domain.PlaceComment;

import java.util.List;

public interface PlaceCommentDAO extends JpaRepository<PlaceComment, Long> {
    Page<PlaceComment> findByPlaceId(Long placeId, PageRequest pageRequest);
    List<PlaceComment> findByPlaceIdOrderByCreatedAtAsc(Long placeId);
}

