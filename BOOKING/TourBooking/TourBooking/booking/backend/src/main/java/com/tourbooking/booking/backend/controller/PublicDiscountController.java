package com.tourbooking.booking.backend.controller;

import com.tourbooking.booking.backend.model.dto.response.ApiResponse;
import com.tourbooking.booking.backend.model.dto.response.DiscountResponse;
import com.tourbooking.booking.backend.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicDiscountController {

    private final DiscountService discountService;

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<DiscountResponse>> validateCode(
            @RequestParam String code,
            @RequestParam BigDecimal amount) {
        try {
            return ResponseEntity.ok(ApiResponse.success(discountService.validateCode(code, amount)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
