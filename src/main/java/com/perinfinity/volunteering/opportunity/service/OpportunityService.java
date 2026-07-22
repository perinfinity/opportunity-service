package com.perinfinity.volunteering.opportunity.service;

import com.perinfinity.volunteering.opportunity.dto.FeedResponseDto;
import com.perinfinity.volunteering.opportunity.dto.OpportunityResponseDto;
import com.perinfinity.volunteering.opportunity.exception.ResourceNotFoundException;
import com.perinfinity.volunteering.opportunity.model.Category;
import com.perinfinity.volunteering.opportunity.model.Opportunity;
import com.perinfinity.volunteering.opportunity.model.Skill;
import com.perinfinity.volunteering.opportunity.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.perinfinity.volunteering.opportunity.model.OpportunityStatus;
import com.perinfinity.volunteering.opportunity.model.WorkType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OpportunityService implements IOpportunityService {

    private final OpportunityRepository opportunityRepository;

    public Opportunity createOpportunity(Opportunity opportunity) {
        return opportunityRepository.save(opportunity);
    }

    public Page<Opportunity> getAllOpportunities(Pageable pageable) {
        return opportunityRepository.findAll(pageable);
    }

    @Override
    public Page<Opportunity> getByTitleContainingIgnoreCase(String title, Pageable pageable) {
        return opportunityRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    public Optional<Opportunity> getOpportunityById(String id) {
        return opportunityRepository.findById(id);
    }

    public Opportunity updateOpportunity(String id, Opportunity opportunityDetails) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found for this id :: " + id));

        opportunity.setTitle(opportunityDetails.getTitle());
        opportunity.setDescription(opportunityDetails.getDescription());
        opportunity.setLocation(opportunityDetails.getLocation());
        opportunity.setStartDate(opportunityDetails.getStartDate());
        opportunity.setEndDate(opportunityDetails.getEndDate());
        opportunity.setRequirements(opportunityDetails.getRequirements());
        opportunity.setOrgId(opportunityDetails.getOrgId());
        opportunity.setStatus(opportunityDetails.getStatus());
        opportunity.setVolunteersNeeded(opportunityDetails.getVolunteersNeeded());
        opportunity.setWorkType(opportunityDetails.getWorkType());
        opportunity.setImageUrls(opportunityDetails.getImageUrls());
        opportunity.setUpdatedAt(LocalDateTime.now());

        return opportunityRepository.save(opportunity);
    }

    public void deleteOpportunity(String id) {
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found for this id :: " + id));
        opportunityRepository.delete(opportunity);
    }

    @Override
    public Page<Opportunity> search(String title, String category, String town, LocalDate startDate, String country,
                                    WorkType workType, OpportunityStatus status, Pageable pageable) {
        return opportunityRepository.search(title, category, town, startDate, country, workType, status, pageable);
    }

    @Override
    public Page<Opportunity> searchByOrgId(Integer orgId, Pageable pageable) {
        return opportunityRepository.findByOrgId(orgId, pageable);
    }

    @Override
    public FeedResponseDto getFeed(List<String> preferredCategories) {
        List<Opportunity> preferredOpps = (preferredCategories == null || preferredCategories.isEmpty())
                ? List.of()
                : opportunityRepository.findOpenByCategories(preferredCategories, 5);

        List<Opportunity> recentOpps = opportunityRepository.findRecentOpen(10);

        return FeedResponseDto.builder()
                .preferred(preferredOpps.stream().map(this::toResponseDto).toList())
                .recent(recentOpps.stream().map(this::toResponseDto).toList())
                .build();
    }

    // --- Mapping helpers ---

    private OpportunityResponseDto toResponseDto(Opportunity o) {
        List<String> categoryNames = o.getCategories() == null ? List.of() :
                o.getCategories().stream().map(Category::getName).toList();
        List<String> skillNames = o.getSkillsRequired() == null ? List.of() :
                o.getSkillsRequired().stream().map(Skill::getName).toList();

        return OpportunityResponseDto.builder()
                .id(o.getId())
                .title(o.getTitle())
                .description(o.getDescription())
                .location(o.getLocation())
                .town(o.getTown())
                .startDate(o.getStartDate())
                .endDate(o.getEndDate())
                .requirements(o.getRequirements())
                .orgId(o.getOrgId())
                .country(o.getCountry())
                .targetCountries(o.getTargetCountries())
                .categoryNames(categoryNames)
                .skillNames(skillNames)
                .status(o.getStatus())
                .volunteersNeeded(o.getVolunteersNeeded())
                .workType(o.getWorkType())
                .imageUrls(o.getImageUrls())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .tags(computeTags(o))
                .build();
    }

    private Set<String> computeTags(Opportunity o) {
        Set<String> tags = new HashSet<>();
        LocalDate today = LocalDate.now();
        if (o.getCreatedAt() != null && o.getCreatedAt().toLocalDate().isAfter(today.minusDays(7))) {
            tags.add("NEW");
        }
        if (OpportunityStatus.OPEN.equals(o.getStatus())
                && o.getStartDate() != null
                && !o.getStartDate().isAfter(today.plusDays(7))
                && !o.getStartDate().isBefore(today)) {
            tags.add("URGENT");
        }
        return tags;
    }
}
