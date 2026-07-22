package com.perinfinity.volunteering.opportunity.repository;

import com.perinfinity.volunteering.opportunity.model.Opportunity;
import com.perinfinity.volunteering.opportunity.model.OpportunityStatus;
import com.perinfinity.volunteering.opportunity.model.WorkType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Sort;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OpportunityRepositoryImpl implements OpportunityRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Opportunity> search(String title, String category, String town, LocalDate startDate, String country,
                                    WorkType workType, OpportunityStatus status, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            criteriaList.add(Criteria.where("title").regex(".*" + java.util.regex.Pattern.quote(title) + ".*", "i"));
        }
        if (town != null && !town.isBlank()) {
            criteriaList.add(Criteria.where("town").regex(".*" + java.util.regex.Pattern.quote(town) + ".*", "i"));
        }
        if (startDate != null) {
            criteriaList.add(Criteria.where("startDate").gte(startDate));
        }
        if (category != null && !category.isBlank()) {
            criteriaList.add(Criteria.where("categories.name").is(category));
        }
        if (country != null && !country.isBlank()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("country").is(country),
                    Criteria.where("targetCountries").is(country)
            ));
        }
        if (workType != null) {
            criteriaList.add(Criteria.where("workType").is(workType));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Opportunity.class);
        query.with(pageable);

        List<Opportunity> results = mongoTemplate.find(query, Opportunity.class);
        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public List<Opportunity> findOpenByCategories(List<String> categoryNames, int limit) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return List.of();
        }
        Query query = new Query(
                new Criteria().andOperator(
                        Criteria.where("status").is(OpportunityStatus.OPEN),
                        Criteria.where("categories.name").in(categoryNames)
                )
        ).with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .limit(limit);
        return mongoTemplate.find(query, Opportunity.class);
    }

    @Override
    public List<Opportunity> findRecentOpen(int limit) {
        Query query = new Query(Criteria.where("status").is(OpportunityStatus.OPEN))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .limit(limit);
        return mongoTemplate.find(query, Opportunity.class);
    }
}
