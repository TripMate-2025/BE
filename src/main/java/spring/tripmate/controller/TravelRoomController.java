package spring.tripmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.tripmate.domain.PlaceComment;
import spring.tripmate.domain.apiPayload.ApiResponse;
import spring.tripmate.dto.*;
import spring.tripmate.service.PlaceCommentService;
import spring.tripmate.service.TravelPlanService;
import spring.tripmate.service.TravelRoomService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class TravelRoomController {

    private final TravelRoomService roomService;
    private final TravelPlanService planService;
    private final PlaceCommentService placeCommentService;

    @PostMapping
    public ApiResponse<TravelRoomResponseDTO.RoomDTO> createRoom(
            @RequestParam("planId") UUID planId,
            @RequestHeader("Authorization") String authHeader
    ) {
        System.out.println("[DEBUG] Long planId : " + planId);
        System.out.println("[DEBUG] 🔐 Received authHeader: " + authHeader);

        PlanResponseDTO.PlanDTO plan = planService.savePlan(planId, authHeader);
        TravelRoomResponseDTO.RoomDTO response = roomService.createRoom(plan.getPlanId(), authHeader);

        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/{roomId}/places/{placeId}")
    public ApiResponse<?> deletePlace(
            @PathVariable Long roomId,
            @PathVariable Long placeId,
            @RequestHeader("Authorization") String authHeader) {

        System.out.println("[DEBUG] 컨트롤러 deletePlace 호출 - roomId: " + roomId + ", placeId: " + placeId);
        roomService.deletePlace(roomId, placeId, authHeader);
        return ApiResponse.onSuccess("삭제 완료");
    }

    @PatchMapping("/{roomId}/places/{placeId}")
    public ApiResponse<PlanResponseDTO.UpdateDTO> updatePlace(
            @PathVariable Long roomId,
            @RequestBody PlanResponseDTO.UpdateDTO requestDto,
            @RequestHeader("Authorization") String authHeader) {

        Map<String, Object> updatedFields = roomService.updatePlaceInRoom(roomId, requestDto.getUpdatedFields(), authHeader);

        PlanResponseDTO.UpdateDTO responseDto = new PlanResponseDTO.UpdateDTO(updatedFields);

        return ApiResponse.onSuccess(responseDto);
    }

    @GetMapping("/{roomId}/places/{placeId}/comments")
    public ApiResponse<List<PlaceCommentDTO>> getComments(
            @PathVariable Long roomId,
            @PathVariable Long placeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<PlaceComment> comments = placeCommentService.getCommentsByPlaceId(placeId, page, size).getContent();

        List<PlaceCommentDTO> response = comments.stream()
                .map(comment -> new PlaceCommentDTO(
                        comment.getId(),
                        comment.getContent(),
                        comment.getWriter().getNickname(),
                        comment.getWriter().getProfile(),
                        comment.getCreatedAt()))
                .toList();

        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/{roomId}/places/{placeId}/comments")
    public ApiResponse<String> addComment(
            @PathVariable Long roomId,
            @PathVariable Long placeId,
            @RequestBody CommentCreateRequest request
    ) {
        placeCommentService.addComment(roomId, placeId, request.getConsumerId(), request.getContent());
        return ApiResponse.onSuccess("댓글 등록 완료");
    }

    @GetMapping("/{roomId}")
    public ApiResponse<TravelRoomResponseDTO.RoomDTO> getRoom(
            @PathVariable("roomId") Long roomId
    ) {
        TravelRoomResponseDTO.RoomDTO dto = roomService.getRoom(roomId);
        return ApiResponse.onSuccess(dto);
    }

    @PostMapping("/{roomId}/members")
    public ApiResponse<ConsumerResponseDTO.RoomMembersDTO> addMember(
            @PathVariable("roomId") Long roomId,
            @RequestHeader("Authorization") String authHeader
    ) {
        ConsumerResponseDTO.RoomMembersDTO members = roomService.addMember(roomId, authHeader);
        return ApiResponse.onSuccess(members);
    }

    @GetMapping
    public ApiResponse<List<TravelRoomResponseDTO.RoomDTO>> getMyRooms(
            @RequestHeader("Authorization") String authHeader
    ) {
        System.out.println("🔥🔥 GET /rooms 진입 성공");
        List<TravelRoomResponseDTO.RoomDTO> rooms = roomService.getRoomsForConsumer(authHeader);
        return ApiResponse.onSuccess(rooms);
    }
}
