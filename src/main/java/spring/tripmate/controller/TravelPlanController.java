package spring.tripmate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.tripmate.domain.apiPayload.ApiResponse;
import spring.tripmate.dto.PlanRequestDTO;
import spring.tripmate.dto.PlanResponseDTO;
import spring.tripmate.service.TravelPlanService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plans")
public class TravelPlanController {
    private final TravelPlanService travelPlanService;

    @PostMapping
    public ApiResponse<PlanResponseDTO.CreatePlanDTO> createPlan(@Valid @RequestBody PlanRequestDTO.CreateDTO request){
        PlanResponseDTO.CreatePlanDTO response = travelPlanService.createPlan(request);
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/{planId}")
    public ApiResponse<PlanResponseDTO.UpdateDTO> updatePlan(@PathVariable("planId") Long planId,
                                                             @Valid @ModelAttribute PlanRequestDTO.UpdateDTO request){
        PlanResponseDTO.UpdateDTO response = travelPlanService.updatePlan(planId, request);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{planId}")
    public ApiResponse<PlanResponseDTO.PlanDTO> getPlanById(@PathVariable("planId") Long planId){
        PlanResponseDTO.PlanDTO response = travelPlanService.getPlan(planId);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping
    public ApiResponse<List<PlanResponseDTO.PlanDTO>> getPlansByTheme(@RequestParam("theme") String theme,
                                                                      @RequestParam(name = "page", defaultValue = "0") int page,
                                                                      @RequestParam(name = "size", defaultValue = "15") int size){
        List<PlanResponseDTO.PlanDTO> response = travelPlanService.getPlansByTheme(theme, page, size);
        return ApiResponse.onSuccess(response);
    }


    //여기수정, 선영
    @PostMapping("/save")
    public ApiResponse<PlanResponseDTO.PlanDTO> savePlanFromRedis(
            @RequestParam("planId") UUID planId,
            @RequestHeader("Authorization") String authHeader) {

        PlanResponseDTO.PlanDTO response = travelPlanService.savePlan(planId, authHeader);
        return ApiResponse.onSuccess(response);
    }


}
