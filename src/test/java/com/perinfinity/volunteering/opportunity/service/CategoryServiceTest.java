package com.perinfinity.volunteering.opportunity.service;

import com.perinfinity.volunteering.opportunity.model.Category;
import com.perinfinity.volunteering.opportunity.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_reusesExistingCategoryWithSameName() {
        Category existing = new Category("id-1", "Environnement");
        when(categoryRepository.findByName("Environnement")).thenReturn(Optional.of(existing));

        Category result = categoryService.createCategory(new Category("Environnement"));

        assertThat(result).isSameAs(existing);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void createCategory_savesWhenNameDoesNotExist() {
        Category fresh = new Category("Sport");
        when(categoryRepository.findByName("Sport")).thenReturn(Optional.empty());
        when(categoryRepository.save(fresh)).thenReturn(new Category("id-2", "Sport"));

        Category result = categoryService.createCategory(fresh);

        assertThat(result.getId()).isEqualTo("id-2");
    }

    @Test
    void createCategories_mixesExistingAndNewWithoutDuplicating() {
        Category existing = new Category("id-1", "Environnement");
        when(categoryRepository.findByName("Environnement")).thenReturn(Optional.of(existing));
        when(categoryRepository.findByName("Sport")).thenReturn(Optional.empty());
        when(categoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<Category> result = categoryService.createCategories(
                List.of(new Category("Environnement"), new Category("Sport")));

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isSameAs(existing);
        verify(categoryRepository, never()).saveAll(any());
    }
}
