package com.perinfinity.volunteering.opportunity.repository;


import com.perinfinity.volunteering.opportunity.model.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface OpportunityRepository extends MongoRepository<Opportunity, String>, OpportunityRepositoryCustom {
    Page<Opportunity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Opportunity> findByOrgId(Integer orgId, Pageable pageable);
}
