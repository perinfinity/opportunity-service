package com.perinfinity.volunteering.opportunity.repository;

import com.perinfinity.volunteering.opportunity.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryRepository extends MongoRepository<Category, String> {
    boolean existsByName(String name);

    java.util.Optional<Category> findByName(String name);
}
