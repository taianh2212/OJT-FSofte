package com.tourbooking.booking.backend.service;

import com.tourbooking.booking.backend.model.dto.request.CategoryRequest;
import com.tourbooking.booking.backend.model.dto.response.CategoryResponse;
import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
}
