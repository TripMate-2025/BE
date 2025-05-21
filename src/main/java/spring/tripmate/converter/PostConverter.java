package spring.tripmate.converter;

import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.Post;
import spring.tripmate.domain.PostImage;
import spring.tripmate.domain.TravelPlan;
import spring.tripmate.dto.PlanResponseDTO;
import spring.tripmate.dto.PostRequestDTO;
import spring.tripmate.dto.PostResponseDTO;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PostConverter {
    public static Post toPost(Consumer writer, TravelPlan plan, PostRequestDTO.CreateDTO request){
        return Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(writer)
                .plan(plan)
                .build();
    }

    public static PostImage toPostImage(String originalFilename, String storedPath, Post post){
        return PostImage.builder()
                .originalFilename(originalFilename)
                .storedPath(storedPath)
                .post(post)
                .build();
    }

    public static PostResponseDTO.CreateDTO toCreatePostDTO(Post post){
        return PostResponseDTO.CreateDTO.builder()
                .postId(post.getId())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static PostResponseDTO.SummaryDTO toSummaryDTO(List<Post> posts){
        List<PostResponseDTO.SummaryDTO.SummaryPostDTO> summaryPosts = posts.stream()
                .map(post -> {
                    PostResponseDTO.SummaryDTO.SummaryPostDTO dto = new PostResponseDTO.SummaryDTO.SummaryPostDTO();
                    dto.setPostId(post.getId());
                    dto.setWriterId(post.getWriter().getId());
                    dto.setNickname(post.getWriter().getNickname());
                    dto.setProfile(post.getWriter().getProfile());
                    dto.setTitle(post.getTitle());
                    dto.setContent(post.getContent());

                    // 이미지 경로 리스트 추출
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
                .posts(summaryPosts)
                .build();
    }

    public static PostResponseDTO.DetailDTO toDetailDTO(Consumer writer, Post post, List<PostImage> images, PlanResponseDTO.PlanDTO plan){
        return PostResponseDTO.DetailDTO.builder()
                .postId(post.getId())
                .writerId(writer.getId())
                .nickname(writer.getNickname())
                .profile(writer.getProfile())
                .title(post.getTitle())
                .images(images)
                .content(post.getContent())
                .plan(plan)
                .build();
    }
}
