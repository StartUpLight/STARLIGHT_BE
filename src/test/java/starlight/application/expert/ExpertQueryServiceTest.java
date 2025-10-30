package starlight.application.expert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import starlight.application.expert.required.ExpertQuery;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpertQueryServiceTest {

    @Mock ExpertQuery expertQueryPort;
    @InjectMocks ExpertQueryService sut; // System Under Test

    @Test
    @DisplayName("전체 조회는 포트의 findAllWithDetails를 호출한다")
    void loadAll() throws Exception {
        when(expertQueryPort.findAllWithDetails()).thenReturn(List.of(expert(1L)));

        var result = sut.loadAll();

        assertThat(result).hasSize(1);
        verify(expertQueryPort, times(1)).findAllWithDetails();
    }

    @Test
    @DisplayName("카테고리 AND 매칭 조회는 포트의 findByAllCategories를 호출한다")
    void findByAllCategories() throws Exception {
        Set<TagCategory> cats = Set.of(TagCategory.GROWTH_STRATEGY, TagCategory.TEAM_CAPABILITY);
        when(expertQueryPort.findByAllCategories(cats)).thenReturn(List.of(expert(2L)));

        var result = sut.findByAllCategories(cats);

        assertThat(result).hasSize(1);
        ArgumentCaptor<Set<TagCategory>> captor = ArgumentCaptor.forClass(Set.class);
        verify(expertQueryPort, times(1)).findByAllCategories(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrderElementsOf(cats);
    }

    private Expert expert(Long id) throws Exception {
        Constructor<Expert> ctor = Expert.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Expert e = ctor.newInstance();
        ReflectionTestUtils.setField(e, "id", id);
        ReflectionTestUtils.setField(e, "name", "tester");
        ReflectionTestUtils.setField(e, "email", "t@example.com");
        return e;
    }
}
