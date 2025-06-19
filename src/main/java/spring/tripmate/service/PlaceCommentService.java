package spring.tripmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring.tripmate.dao.ConsumerDAO;
import spring.tripmate.dao.PlaceCommentDAO;
import spring.tripmate.dao.TravelPlaceDAO;
import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.PlaceComment;
import spring.tripmate.domain.TravelPlace;
import spring.tripmate.dto.PlaceCommentRequestDTO;
import spring.tripmate.dto.PlaceCommentResponseDTO;
import spring.tripmate.security.JwtProvider;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceCommentService {

    private final PlaceCommentDAO commentRepository;
    private final TravelPlaceDAO placeRepository;
    private final ConsumerDAO consumerDAO;
    private final JwtProvider jwtProvider;

    // 댓글 등록
    public void addComment(String token, PlaceCommentRequestDTO request) {
        // 1. 토큰에서 이메일 추출
        String email = jwtProvider.getEmailFromToken(token);

        // 2. Consumer 찾기
        Consumer consumer = consumerDAO.findByEmail(email);
        if (consumer == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 3. 장소 찾기
        TravelPlace place = placeRepository.findById(request.getPlaceId())
                .orElseThrow(() -> new IllegalArgumentException("장소가 존재하지 않습니다."));

        // 4. 댓글 저장
        PlaceComment comment = PlaceComment.builder()
                .content(request.getContent())
                .writer(consumer)
                .place(place)
                .build();

        commentRepository.save(comment);
    }

    // 댓글 조회
    public List<PlaceCommentResponseDTO> getComments(Long placeId) {
        return commentRepository.findByPlaceIdOrderByCreatedAtAsc(placeId)
                .stream()
                .map(c -> PlaceCommentResponseDTO.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .nickname(c.getWriter().getNickname())
                        .profileImg(c.getWriter().getProfile())
                        .createdAt(c.getCreatedAt().toString())
                        .build())
                .toList();
    }
}
