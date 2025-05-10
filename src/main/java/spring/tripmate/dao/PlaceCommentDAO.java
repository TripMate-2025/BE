package spring.tripmate.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.tripmate.domain.PlaceComment;

public interface PlaceCommentDAO extends JpaRepository<PlaceComment, Long> {
    Page<PlaceComment> findByPlaceId(Long placeId, PageRequest pageRequest);
}

