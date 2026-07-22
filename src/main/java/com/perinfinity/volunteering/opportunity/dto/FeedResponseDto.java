package com.perinfinity.volunteering.opportunity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponseDto {

    /** Opportunities matching the volunteer's preferred categories (max 5, status=OPEN). */
    private List<OpportunityResponseDto> preferred;

    /** Most recent OPEN opportunities (max 10, ordered by createdAt desc). */
    private List<OpportunityResponseDto> recent;
}
