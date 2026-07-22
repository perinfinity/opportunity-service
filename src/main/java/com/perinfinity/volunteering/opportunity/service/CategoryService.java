package com.perinfinity.volunteering.opportunity.service;

import com.perinfinity.volunteering.opportunity.model.Category;
import com.perinfinity.volunteering.opportunity.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService implements ICategoryService{

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }


    @Override
    public List<Category> getAllCategories() {

        return this.categoryRepository.findAll();
    }

    @Override
    public Category createCategory(Category category) {
        return this.categoryRepository.findByName(category.getName())
                .orElseGet(() -> this.categoryRepository.save(category));
    }

    /**
     * Réutilise les catégories existantes (recherche par nom) et ne crée que
     * les nouvelles — sinon chaque création d'opportunité duplique ses catégories.
     */
    @Override
    public List<Category> createCategories(List<Category> categories) {
        List<Category> result = new ArrayList<>();
        for (Category category : categories) {
            result.add(createCategory(category));
        }
        return result;
    }
}
