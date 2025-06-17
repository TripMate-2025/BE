package spring.tripmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.tripmate.domain.apiPayload.ApiResponse;
import spring.tripmate.dto.ConsumerResponseDTO;
import spring.tripmate.dto.PlanResponseDTO;
import spring.tripmate.dto.TravelRoomResponseDTO;
import spring.tripmate.service.TravelPlanService;
import spring.tripmate.service.TravelRoomService;

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
}
