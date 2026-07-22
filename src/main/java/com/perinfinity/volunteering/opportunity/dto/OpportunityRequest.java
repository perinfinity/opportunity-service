package com.perinfinity.volunteering.opportunity.dto;

import com.perinfinity.volunteering.opportunity.model.OpportunityStatus;
import com.perinfinity.volunteering.opportunity.model.WorkType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpportunityRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    private String location;

    private String town;

    private LocalDate startDate;

    private LocalDate endDate;

    private String requirements;

    @NotNull(message = "L'identifiant de l'organisation est obligatoire")
    private Integer orgId;

    private String country;

    private List<String> targetCountries;

    private List<String> categoryNames;

    private List<String> skillNames;

    private OpportunityStatus status;

    private Integer volunteersNeeded;

    private WorkType workType;

    private List<String> imageUrls;
}
