package com.perinfinity.volunteering.opportunity.dto;

import com.perinfinity.volunteering.opportunity.model.OpportunityStatus;
import com.perinfinity.volunteering.opportunity.model.WorkType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Builder
public class OpportunityResponseDto {

    private String id;
    private String title;
    private String description;
    private String location;
    private String town;
    private LocalDate startDate;
    private LocalDate endDate;
    private String requirements;
    private Integer orgId;
    private String country;
    private List<String> targetCountries;
    private List<String> categoryNames;
    private List<String> skillNames;
    private OpportunityStatus status;
    private Integer volunteersNeeded;
    private WorkType workType;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /** Computed urgency tags: NEW, URGENT */
    private Set<String> tags;
}
