package com.perinfinity.volunteering.opportunity.service;

import com.perinfinity.volunteering.opportunity.model.Category;

import java.util.List;

public interface ICategoryService {
    List<Category> getAllCategories();

    Category createCategory(Category category);

    List<Category> createCategories(List<Category> categories);
}
