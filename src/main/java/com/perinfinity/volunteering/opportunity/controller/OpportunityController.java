package com.perinfinity.volunteering.opportunity.controller;

import com.perinfinity.volunteering.opportunity.dto.FeedResponseDto;
import com.perinfinity.volunteering.opportunity.dto.OpportunityRequest;
import com.perinfinity.volunteering.opportunity.dto.OpportunityResponse;
import com.perinfinity.volunteering.opportunity.dto.OpportunityResponseDto;
import com.perinfinity.volunteering.opportunity.exception.ResourceNotFoundException;
import com.perinfinity.volunteering.opportunity.model.Category;
import com.perinfinity.volunteering.opportunity.model.Opportunity;
import com.perinfinity.volunteering.opportunity.model.OpportunityStatus;
import com.perinfinity.volunteering.opportunity.model.Skill;
import com.perinfinity.volunteering.opportunity.model.WorkType;
import com.perinfinity.volunteering.opportunity.service.ICategoryService;
import com.perinfinity.volunteering.opportunity.service.IOpportunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.perinfinity.volunteering.opportunity.utils.URIUtils.entityWithLocation;

@RestController
@RequestMapping("api/v1/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final IOpportunityService opportunityService;
    private final ICategoryService categoryService;

    @PostMapping
    public ResponseEntity<Void> createOpportunity(@Valid @RequestBody OpportunityRequest request) {
        List<Category> categories = toCategories(request.getCategoryNames());
        List<Category> savedCategories = categoryService.createCategories(categories);

        Opportunity opportunity = toOpportunity(request);
        opportunity.setCategories(savedCategories);
        opportunity.setCreatedAt(LocalDateTime.now());
        if (opportunity.getStatus() == null) {
            opportunity.setStatus(OpportunityStatus.OPEN);
        }

        Opportunity created = opportunityService.createOpportunity(opportunity);
        URI uri = entityWithLocation(created.getId());
        return ResponseEntity.created(uri).build();
    }

    @GetMapping
    public OpportunityResponse getAllOpportunities(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String town,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) WorkType workType,
            @RequestParam(required = false) OpportunityStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Opportunity> result = opportunityService.search(title, category, town, startDate, country, workType, status, pageable);

        return OpportunityResponse.builder()
                .opportunities(result.getContent().stream().map(this::toResponseDto).toList())
                .currentPage(result.getNumber())
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .itemsPerPage(result.getSize())
                .build();
    }

    @GetMapping("/organization/{orgId}")
    public OpportunityResponse getOpportunitiesByOrgId(
            @PathVariable Integer orgId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Opportunity> result = opportunityService.searchByOrgId(orgId, pageable);

        return OpportunityResponse.builder()
                .opportunities(result.getContent().stream().map(this::toResponseDto).toList())
                .currentPage(result.getNumber())
                .totalItems(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .itemsPerPage(result.getSize())
                .build();
    }

    @GetMapping("/feed")
    public ResponseEntity<FeedResponseDto> getFeed(
            @RequestParam(required = false, defaultValue = "") List<String> preferredCategories) {
        return ResponseEntity.ok(opportunityService.getFeed(preferredCategories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OpportunityResponseDto> getOpportunityById(@PathVariable String id) {
        Opportunity opportunity = opportunityService.getOpportunityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found for this id :: " + id));
        return ResponseEntity.ok(toResponseDto(opportunity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OpportunityResponseDto> updateOpportunity(
            @PathVariable String id,
            @Valid @RequestBody OpportunityRequest request,
            Authentication authentication) {

        Opportunity existing = opportunityService.getOpportunityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + id));
        verifyOwnership(authentication, existing);

        Opportunity details = toOpportunity(request);
        Opportunity updated = opportunityService.updateOpportunity(id, details);
        return ResponseEntity.ok(toResponseDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOpportunity(@PathVariable String id, Authentication authentication) {
        Opportunity existing = opportunityService.getOpportunityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Opportunity not found: " + id));
        verifyOwnership(authentication, existing);

        opportunityService.deleteOpportunity(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ensures the authenticated user owns the opportunity.
     * The JWT userId claim must match opportunity.orgId.
     */
    private void verifyOwnership(Authentication authentication, Opportunity opportunity) {
        if (authentication == null || authentication.getDetails() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }
        Long userId = (Long) authentication.getDetails();
        if (!userId.equals(opportunity.getOrgId().longValue())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this opportunity");
        }
    }

    // --- Mapping helpers ---

    private Opportunity toOpportunity(OpportunityRequest request) {
        List<Skill> skills = request.getSkillNames() == null ? List.of() :
                request.getSkillNames().stream()
                        .map(name -> { Skill s = new Skill(); s.setName(name); return s; })
                        .toList();

        return Opportunity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .town(request.getTown())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .requirements(request.getRequirements())
                .orgId(request.getOrgId())
                .country(request.getCountry())
                .targetCountries(request.getTargetCountries())
                .skillsRequired(skills)
                .status(request.getStatus())
                .volunteersNeeded(request.getVolunteersNeeded())
                .workType(request.getWorkType())
                .imageUrls(request.getImageUrls())
                .build();
    }

    private List<Category> toCategories(List<String> names) {
        if (names == null) return List.of();
        return names.stream().map(Category::new).toList();
    }

    private OpportunityResponseDto toResponseDto(Opportunity o) {
        List<String> categoryNames = o.getCategories() == null ? List.of() :
                o.getCategories().stream().map(Category::getName).toList();
        List<String> skillNames = o.getSkillsRequired() == null ? List.of() :
                o.getSkillsRequired().stream().map(Skill::getName).toList();

        Set<String> tags = computeTags(o);

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
                .tags(tags)
                .build();
    }

    /**
     * Computes urgency tags locally:
     * - NEW: created within the last 7 days
     * - URGENT: OPEN opportunity starting within the next 7 days
     */
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
