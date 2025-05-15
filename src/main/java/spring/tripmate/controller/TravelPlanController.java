package spring.tripmate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import spring.tripmate.domain.apiPayload.ApiResponse;
import spring.tripmate.dto.PlanRequestDTO;
import spring.tripmate.dto.PlanResponseDTO;
import spring.tripmate.service.TravelPlanService;

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

}
