package starlight.adapter.expert.webapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import starlight.adapter.auth.security.filter.JwtFilter;
import starlight.application.expert.provided.ExpertDetailQueryUseCase;
import starlight.application.expert.provided.dto.ExpertCareerResult;
import starlight.application.expert.provided.dto.ExpertDetailResult;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ExpertController.class,
        excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class ExpertControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean
    ExpertDetailQueryUseCase expertDetailQuery;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext; 

    @Test
    @DisplayName("전문가 전체 조회")
    void listAll() throws Exception {
        ExpertDetailResult e1 = expertResult(1L, "홍길동",
                Set.of(TagCategory.GROWTH_STRATEGY, TagCategory.TEAM_CAPABILITY));
        when(expertDetailQuery.searchAll()).thenReturn(List.of(e1));

        mockMvc.perform(get("/v1/experts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].name").value("홍길동"))
                .andExpect(jsonPath("$.data[0].careers.length()").value(3))
                .andExpect(jsonPath("$.data[0].careers[0].orderIndex").exists())
                .andExpect(jsonPath("$.data[0].careers[0].careerTitle").exists())
                .andExpect(jsonPath("$.data[0].applicationCount").doesNotExist());
    }

    @Test
    @DisplayName("전문가 상세 조회")
    void detail() throws Exception {
        ExpertDetailResult result = expertResult(10L, "김철수",
                Set.of(TagCategory.MARKET_BM, TagCategory.GROWTH_STRATEGY));
        when(expertDetailQuery.findById(10L)).thenReturn(result);

        mockMvc.perform(get("/v1/experts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.applicationCount").value(0))
                .andExpect(jsonPath("$.data.categories").doesNotExist())
                .andExpect(jsonPath("$.data.tags").isArray());
    }

    // helper
    private ExpertDetailResult expertResult(Long id, String name, Set<TagCategory> cats) throws Exception {
        List<ExpertCareerResult> careers = List.of(
                new ExpertCareerResult(1L, 0, "A사 PO", "설명", null, null),
                new ExpertCareerResult(2L, 1, "B사 PM", "설명", null, null),
                new ExpertCareerResult(3L, 2, "C사 리드", "설명", null, null),
                new ExpertCareerResult(4L, 3, "D사 CTO", "설명", null, null)
        );

        return new ExpertDetailResult(
                id,
                0L,
                name,
                "한줄소개",
                "상세소개",
                "https://cdn.example.com/" + id + ".png",
                12L,
                name + "@example.com",
                50000,
                careers,
                List.of("tag1", "tag2"),
                cats.stream().map(TagCategory::name).toList()
        );
    }
}
