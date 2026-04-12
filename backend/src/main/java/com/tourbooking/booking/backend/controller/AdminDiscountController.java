package com.tourbooking.booking.backend.controller;

import com.tourbooking.booking.backend.model.dto.request.DiscountRequest;
import com.tourbooking.booking.backend.model.dto.response.ApiResponse;
import com.tourbooking.booking.backend.model.dto.response.DiscountResponse;
import com.tourbooking.booking.backend.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/discounts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminDiscountController {

    private final DiscountService discountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> getAllDiscounts() {
        return ResponseEntity.ok(ApiResponse.success(discountService.getAllDiscounts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> getDiscountById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(discountService.getDiscountById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DiscountResponse>> createDiscount(@RequestBody DiscountRequest request) {
        return ResponseEntity.ok(ApiResponse.success(discountService.createDiscount(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DiscountResponse>> updateDiscount(@PathVariable Long id, @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(ApiResponse.success(discountService.updateDiscount(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(@PathVariable Long id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
