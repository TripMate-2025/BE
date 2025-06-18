package spring.tripmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.tripmate.dao.ConsumerDAO;
import spring.tripmate.dao.PlaceCommentDAO;
import spring.tripmate.dao.TravelPlaceDAO;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.PlaceComment;
import spring.tripmate.domain.TravelPlace;
import spring.tripmate.dto.CommentCreateRequest;

@Service
@RequiredArgsConstructor
public class PlaceCommentService {

    private final PlaceCommentDAO placeCommentDAO;
    private final ConsumerDAO consumerDAO;       // 사용자 조회용
    private final TravelPlaceDAO travelPlaceDAO; // 장소 조회용

    @Transactional(readOnly = true)
    public Page<PlaceComment> getCommentsByPlaceId(Long placeId, int page, int size) {
        return placeCommentDAO.findByPlaceId(placeId, PageRequest.of(page, size));
    }

    @Transactional
    public PlaceComment addComment(Long roomId, Long placeId, Long consumerId, String content) {
        Consumer writer = consumerDAO.findById(consumerId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        TravelPlace place = travelPlaceDAO.findById(placeId)
                .orElseThrow(() -> new RuntimeException("장소 없음"));

        PlaceComment comment = PlaceComment.builder()
                .content(content)
                .writer(writer)
                .place(place)
                .build();

        return placeCommentDAO.save(comment);
    }

}
