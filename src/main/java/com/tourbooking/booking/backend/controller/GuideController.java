package com.tourbooking.booking.backend.controller;

import com.tourbooking.booking.backend.model.dto.response.ApiResponse;
import com.tourbooking.booking.backend.model.dto.response.TourScheduleResponse;
import com.tourbooking.booking.backend.model.dto.response.UserResponse;
import com.tourbooking.booking.backend.service.GuideService;
import com.tourbooking.booking.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/guides")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;
    private final UserService userService;

    @GetMapping("/assigned-tours")
    @PreAuthorize("hasRole('GUIDE')")
    public ApiResponse<List<TourScheduleResponse>> getAssignedTours(Authentication authentication) {
        String email = authentication.getName();
        UserResponse user = userService.getUserByEmail(email);
        Long guideId = user.getId();
        
        List<TourScheduleResponse> assignedTours = guideService.getAssignedTours(guideId);
        
        return ApiResponse.<List<TourScheduleResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Successfully retrieved assigned tours")
                .data(assignedTours)
                .build();
    }
}
