package com.perinfinity.volunteering.opportunity.repository;

import com.perinfinity.volunteering.opportunity.model.Opportunity;
import com.perinfinity.volunteering.opportunity.model.OpportunityStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpportunityRepositoryImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private OpportunityRepositoryImpl repository;

    @Test
    void search_withCountry_returnsFilteredResults() {
        Opportunity opp = Opportunity.builder().id("1").title("Beach cleanup").country("CM").build();
        when(mongoTemplate.count(any(Query.class), eq(Opportunity.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Opportunity.class))).thenReturn(List.of(opp));

        Page<Opportunity> result = repository.search(null, null, null, null, "CM", null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().get(0).getCountry()).isEqualTo("CM");
    }

    @Test
    void search_withoutCountry_returnsAllResults() {
        Opportunity opp1 = Opportunity.builder().id("1").title("Op 1").country("CM").build();
        Opportunity opp2 = Opportunity.builder().id("2").title("Op 2").country("CI").build();
        when(mongoTemplate.count(any(Query.class), eq(Opportunity.class))).thenReturn(2L);
        when(mongoTemplate.find(any(Query.class), eq(Opportunity.class))).thenReturn(List.of(opp1, opp2));

        Page<Opportunity> result = repository.search(null, null, null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2L);
    }

    @Test
    void search_withTitleAndCountry_appliesBothFilters() {
        Opportunity opp = Opportunity.builder().id("1").title("Beach cleanup").country("CM").build();
        when(mongoTemplate.count(any(Query.class), eq(Opportunity.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Opportunity.class))).thenReturn(List.of(opp));

        Page<Opportunity> result = repository.search("Beach", null, null, null, "CM", null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Beach cleanup");
    }

    @Test
    void findOpenByCategories_withMatchingCategories_returnsResults() {
        Opportunity opp = Opportunity.builder().id("1").title("Community Garden").status(OpportunityStatus.OPEN).build();
        when(mongoTemplate.find(any(Query.class), eq(Opportunity.class))).thenReturn(List.of(opp));

        List<Opportunity> result = repository.findOpenByCategories(List.of("Environment"), 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Community Garden");
    }

    @Test
    void findOpenByCategories_withEmptyCategories_returnsEmptyList() {
        List<Opportunity> result = repository.findOpenByCategories(List.of(), 5);

        assertThat(result).isEmpty();
    }

    @Test
    void findOpenByCategories_withNullCategories_returnsEmptyList() {
        List<Opportunity> result = repository.findOpenByCategories(null, 5);

        assertThat(result).isEmpty();
    }

    @Test
    void findRecentOpen_returnsOpenOpportunities() {
        Opportunity opp1 = Opportunity.builder().id("1").title("Op 1").status(OpportunityStatus.OPEN).build();
        Opportunity opp2 = Opportunity.builder().id("2").title("Op 2").status(OpportunityStatus.OPEN).build();
        when(mongoTemplate.find(any(Query.class), eq(Opportunity.class))).thenReturn(List.of(opp1, opp2));

        List<Opportunity> result = repository.findRecentOpen(10);

        assertThat(result).hasSize(2);
    }
}
