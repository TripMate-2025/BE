package spring.tripmate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring.tripmate.converter.PostConverter;
import spring.tripmate.dao.*;
import spring.tripmate.domain.*;
import spring.tripmate.domain.apiPayload.code.status.ErrorStatus;
import spring.tripmate.domain.apiPayload.exception.handler.PlanHandler;
import spring.tripmate.domain.apiPayload.exception.handler.PostHandler;
import spring.tripmate.domain.apiPayload.exception.handler.UnauthorizedException;
import spring.tripmate.dto.PostRequestDTO;
import spring.tripmate.dto.PostResponseDTO;
import spring.tripmate.security.JwtProvider;
import spring.tripmate.util.FileUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final JwtProvider jwtProvider;
    private final FileUtil fileUtil;

    private final ConsumerDAO consumerDAO;
    private final PostDAO postDAO;
    private final PostImageDAO postImageDAO;
    private final PostLikeDAO postLikeDAO;
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

    public PostResponseDTO.UpdateDTO updatePost(Long postId, PostRequestDTO.UpdateDTO request){
        Post post = postDAO.findById(postId)
                .orElseThrow(() -> new PostHandler(ErrorStatus.POST_NOT_FOUND));

        Map<String, Object> updatedFields = new HashMap<>();

        //title
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            post.setTitle(request.getTitle());
            updatedFields.put("title", request.getTitle());
        }

        //content
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            post.setContent(request.getContent());
            updatedFields.put("content", request.getContent());
        }

        //planId
        if (request.getPlanId() != null) {
            TravelPlan plan = planDAO.findById(request.getPlanId())
                    .orElseThrow(() -> new PlanHandler(ErrorStatus.PLAN_NOT_FOUND));
            post.setPlan(plan);
            updatedFields.put("planId", request.getPlanId());
        }

        //deleteImage
        if (request.getDeleteImageIds() != null) {
            for (Long imageId : request.getDeleteImageIds()) {
                PostImage image = postImageDAO.findById(imageId)
                        .orElseThrow(() -> new PostHandler(ErrorStatus.POST_IMAGE_NOT_FOUND));
                postImageDAO.delete(image);
                fileUtil.deleteFile(image.getStoredPath());
            }
        }

        //newImage
        if (request.getNewImages() != null) {
            for (MultipartFile file : request.getNewImages()) {
                String path = fileUtil.saveFile(file, "post");
                PostImage newImage = new PostImage(null, file.getOriginalFilename(), path, post);
                postImageDAO.save(newImage);
            }
        }

        // 최종 저장
        postDAO.save(post);

        return PostResponseDTO.UpdateDTO.builder()
                .updatedFields(updatedFields)
                .build();
    }

    public PostResponseDTO.SummaryDTO getPostsByCountry(String authHeader, String country, int page, int size){
        Long consumerId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtProvider.getEmailFromToken(token);
            Consumer writer = consumerDAO.findByEmail(email);
            if (writer != null) {
                consumerId = writer.getId();
            }
        }

        Pageable pageable = PageRequest.of(page, size);

        List<Post> posts;

        if (country == null || country.isBlank()) {
            // 전체 게시글 중 최신순으로
            posts = postDAO.findAllByOrderByUpdatedAtDesc(pageable).getContent();
        }else {
            Page<TravelPlan> plans = planDAO.findByCountry(country, pageable);
            List<TravelPlan> plansList = plans.getContent();
            posts = plansList.stream()
                    .flatMap(plan -> plan.getPosts().stream()) // 각 plan의 posts를 평탄화
                    .toList();
        }

        final Long finalConsumerId = consumerId;
        List<PostResponseDTO.SummaryDTO.SummaryPostDTO> postDTOs = posts.stream()
                .map(post -> {
                    Boolean liked = false;
                    if (finalConsumerId != null && postLikeDAO.existsByPostIdAndConsumerId(post.getId(), finalConsumerId)){
                        liked = true;
                    }
                    PostResponseDTO.SummaryDTO.SummaryPostDTO dto = new PostResponseDTO.SummaryDTO.SummaryPostDTO();
                    dto.setPostId(post.getId());
                    dto.setWriterId(post.getWriter().getId());
                    dto.setNickname(post.getWriter().getNickname());
                    dto.setProfile(post.getWriter().getProfile());
                    dto.setTitle(post.getTitle());
                    dto.setContent(post.getContent());
                    dto.setLiked(liked);

                    List<String> images = Optional.ofNullable(post.getPostImages())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(PostImage::getStoredPath)
                            .collect(Collectors.toList());

                    dto.setImages(images);

                    return dto;
                })
                .toList();

        return PostResponseDTO.SummaryDTO.builder()
                .posts(postDTOs)
                .build();
    }
}
