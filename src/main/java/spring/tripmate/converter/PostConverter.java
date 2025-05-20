package spring.tripmate.converter;

import spring.tripmate.domain.Consumer;
import spring.tripmate.domain.Post;
import spring.tripmate.domain.PostImage;
import spring.tripmate.domain.TravelPlan;
import spring.tripmate.dto.PostRequestDTO;
import spring.tripmate.dto.PostResponseDTO;

import java.time.LocalDateTime;

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
}
