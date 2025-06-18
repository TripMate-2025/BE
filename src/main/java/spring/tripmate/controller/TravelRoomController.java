package spring.tripmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.tripmate.domain.apiPayload.ApiResponse;
import spring.tripmate.dto.*;
import spring.tripmate.service.TravelPlanService;
import spring.tripmate.service.TravelRoomService;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class TravelRoomController {

    private final TravelRoomService roomService;
    private final TravelPlanService planService;

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

    @PatchMapping("/{roomId}")
    public ApiResponse<?> updateRoom(
            @PathVariable Long roomId,
            @RequestBody TravelRoomRequestDTO.UpdateDTO request,
            @RequestHeader("Authorization") String authHeader) {

        return ApiResponse.onSuccess(roomService.updateRoom(roomId, request, authHeader));
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

    @GetMapping("/simple")
    public ApiResponse<List<TravelRoomResponseDTO.SimpleRoomDTO>> getSimpleRooms(
            @RequestHeader("Authorization") String authHeader
    ) {
        List<TravelRoomResponseDTO.SimpleRoomDTO> result = roomService.getSimpleRoomList(authHeader);
        return ApiResponse.onSuccess(result);
    }

}
