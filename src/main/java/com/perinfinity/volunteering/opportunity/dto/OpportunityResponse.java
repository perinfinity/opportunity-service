package com.perinfinity.volunteering.opportunity.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OpportunityResponse {
    Integer currentPage;
    Integer itemsPerPage;
    Long totalItems;
    Integer totalPages;
    List<OpportunityResponseDto> opportunities;
}
