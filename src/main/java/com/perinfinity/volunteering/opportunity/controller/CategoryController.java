package com.perinfinity.volunteering.opportunity.controller;

import com.perinfinity.volunteering.opportunity.model.Category;
import com.perinfinity.volunteering.opportunity.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.perinfinity.volunteering.opportunity.utils.URIUtils.entityWithLocation;

@RestController
@RequestMapping("api/v1/categories")
public class CategoryController {

    CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> getCategories () {
        return categoryService.getAllCategories();
    }

    @PostMapping
    public ResponseEntity<Void> createCategory(@RequestBody Category category) {
        Category newCategory = categoryService.createCategory(category);
        URI uri = entityWithLocation(newCategory.getId());
        return ResponseEntity.created(uri).build();
    }
}
