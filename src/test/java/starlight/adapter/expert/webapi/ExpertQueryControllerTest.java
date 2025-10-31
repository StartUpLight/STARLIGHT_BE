package starlight.adapter.expert.webapi;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import starlight.adapter.auth.security.filter.JwtFilter;
import starlight.application.expert.provided.ExpertFinder;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.lang.reflect.Constructor;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ExpertQueryController.class,
        excludeAutoConfiguration = JpaRepositoriesAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtFilter.class
        )
)
@AutoConfigureMockMvc(addFilters = false)
class ExpertQueryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;

    @MockitoBean ExpertFinder expertFinder;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext; // ← 필드로 추가!

    @Test
    @DisplayName("카테고리 미전달 시 전체 조회")
    void listAll() throws Exception {
        Expert e1 = expert(1L, "홍길동",
                Set.of(TagCategory.GROWTH_STRATEGY, TagCategory.TEAM_CAPABILITY));
        when(expertFinder.loadAll()).thenReturn(List.of(e1));

        mockMvc.perform(get("/v1/experts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].name").value("홍길동"));
    }

    @Test
    @DisplayName("카테고리 AND 매칭 (?categories=A&categories=B)")
    void searchByAllCategories_multiParams() throws Exception {
        Expert e1 = expert(2L, "이영희",
                Set.of(TagCategory.GROWTH_STRATEGY, TagCategory.TEAM_CAPABILITY));

        when(expertFinder.findByAllCategories(Set.of(
                TagCategory.GROWTH_STRATEGY, TagCategory.TEAM_CAPABILITY
        ))).thenReturn(List.of(e1));

        mockMvc.perform(get("/v1/experts")
                        .param("categories", "GROWTH_STRATEGY")
                        .param("categories", "TEAM_CAPABILITY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].name").value("이영희"));
    }

    @Test
    @DisplayName("카테고리 AND 매칭 (콤마 구분)")
    void searchByAllCategories_commaSeparated() throws Exception {
        Expert e1 = expert(3L, "박철수",
                Set.of(TagCategory.MARKET_BM, TagCategory.METRIC_DATA));

        when(expertFinder.findByAllCategories(Set.of(
                TagCategory.MARKET_BM, TagCategory.METRIC_DATA
        ))).thenReturn(List.of(e1));

        mockMvc.perform(get("/v1/experts")
                        .param("categories", "MARKET_BM,METRIC_DATA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].name").value("박철수"));
    }

    // helper
    private Expert expert(Long id, String name, Set<TagCategory> cats) throws Exception {
        Constructor<Expert> ctor = Expert.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Expert e = ctor.newInstance();
        ReflectionTestUtils.setField(e, "id", id);
        ReflectionTestUtils.setField(e, "name", name);
        ReflectionTestUtils.setField(e, "email", name + "@example.com");
        ReflectionTestUtils.setField(e, "profileImageUrl", "https://cdn.example.com/" + id + ".png");
        ReflectionTestUtils.setField(e, "mentoringPriceWon", 50000);
        ReflectionTestUtils.setField(e, "careers", List.of("A사 PO", "B사 PM"));
        ReflectionTestUtils.setField(e, "categories", new LinkedHashSet<>(cats));
        return e;
    }
}