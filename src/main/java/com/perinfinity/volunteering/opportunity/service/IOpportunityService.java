package com.perinfinity.volunteering.opportunity.service;

import com.perinfinity.volunteering.opportunity.model.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.perinfinity.volunteering.opportunity.dto.FeedResponseDto;
import com.perinfinity.volunteering.opportunity.model.OpportunityStatus;
import com.perinfinity.volunteering.opportunity.model.WorkType;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface IOpportunityService {
    Opportunity createOpportunity(Opportunity opportunity);
    Page<Opportunity> getAllOpportunities(Pageable pageable);
    Optional<Opportunity> getOpportunityById(String id);
    Opportunity updateOpportunity(String id, Opportunity opportunityDetails);
    Page<Opportunity> getByTitleContainingIgnoreCase(String title, Pageable pageable);
    void deleteOpportunity(String id);
    Page<Opportunity> search(String title, String category, String town, LocalDate startDate, String country,
                             WorkType workType, OpportunityStatus status, Pageable pageable);
    Page<Opportunity> searchByOrgId(Integer orgId, Pageable pageable);
    FeedResponseDto getFeed(List<String> preferredCategories);
}
