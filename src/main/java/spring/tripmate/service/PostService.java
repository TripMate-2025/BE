package spring.tripmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring.tripmate.converter.PostConverter;
import spring.tripmate.dao.*;
import spring.tripmate.domain.*;
import spring.tripmate.domain.apiPayload.code.status.ErrorStatus;
import spring.tripmate.domain.apiPayload.exception.handler.PlanHandler;
import spring.tripmate.domain.apiPayload.exception.handler.UnauthorizedException;
import spring.tripmate.dto.PostRequestDTO;
import spring.tripmate.dto.PostResponseDTO;
import spring.tripmate.security.JwtProvider;
import spring.tripmate.util.FileUtil;

@Service
@RequiredArgsConstructor
public class PostService {

    private final JwtProvider jwtProvider;
    private final FileUtil fileUtil;

    private final ConsumerDAO consumerDAO;
    private final PostDAO postDAO;
    private final PostImageDAO postImageDAO;
    private final TravelPlanDAO planDAO;

    public PostResponseDTO.CreateDTO createPost(String authHeader, PostRequestDTO.CreateDTO request) {
        //토큰 추출 및 로그인 한 사용자 찾기
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException(ErrorStatus.INVALID_AUTH_HEADER);
        }
        String token = authHeader.replace("Bearer ", "");
        String email = jwtProvider.getEmailFromToken(token);
        Consumer writer = consumerDAO.findByEmail(email);
        if (writer == null) {
            throw new UnauthorizedException(ErrorStatus.CONSUMER_NOT_FOUND);
        }

        Long planId = request.getPlanId();
        TravelPlan plan = planDAO.findById(planId)
                .orElseThrow(() -> new PlanHandler(ErrorStatus.PLAN_NOT_FOUND));

        Post post = PostConverter.toPost(writer, plan, request);
        postDAO.save(post);

        //이미지 처리
        if (request.getImages() != null) {
            for (MultipartFile file : request.getImages()) {
                String storedPath = fileUtil.saveFile(file, "post"); // post 폴더에 저장
                String originalFilename = file.getOriginalFilename();

                PostImage image = PostConverter.toPostImage(originalFilename, storedPath, post);
                postImageDAO.save(image);
            }
        }

        return PostConverter.toCreatePostDTO(post);
    }
}
