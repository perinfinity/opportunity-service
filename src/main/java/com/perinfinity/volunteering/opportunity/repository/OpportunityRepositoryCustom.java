package com.perinfinity.volunteering.opportunity.repository;

import com.perinfinity.volunteering.opportunity.model.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.perinfinity.volunteering.opportunity.model.OpportunityStatus;
import com.perinfinity.volunteering.opportunity.model.WorkType;

import java.time.LocalDate;
import java.util.List;

public interface OpportunityRepositoryCustom {
    Page<Opportunity> search(String title, String category, String town, LocalDate startDate, String country,
                             WorkType workType, OpportunityStatus status, Pageable pageable);

    List<Opportunity> findOpenByCategories(List<String> categoryNames, int limit);

    List<Opportunity> findRecentOpen(int limit);
}


