package starlight.application.expert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import starlight.application.expert.required.ExpertQueryPort;
import starlight.domain.expert.entity.Expert;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpertQueryServiceTest {

    @Mock ExpertQueryPort expertQueryPort;
    @InjectMocks ExpertQueryService sut; // System Under Test

    @Test
    @DisplayName("상세 조회는 포트의 findByIdWithDetails를 호출한다")
    void findByIdWithDetails() throws Exception {
        when(expertQueryPort.findByIdWithDetails(1L)).thenReturn(expert(1L));

        var result = sut.findByIdWithDetails(1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(expertQueryPort, times(1)).findByIdWithDetails(1L);
    }

    @Test
    @DisplayName("ID 목록 조회는 포트의 findExpertMapByIds를 호출한다")
    void findByIds() throws Exception {
        Set<Long> ids = Set.of(1L, 2L);
        when(expertQueryPort.findExpertMapByIds(ids)).thenReturn(
                Map.of(1L, expert(1L), 2L, expert(2L))
        );

        var result = sut.findByIds(ids);

        assertThat(result).hasSize(2);
        ArgumentCaptor<Set<Long>> captor = ArgumentCaptor.forClass(Set.class);
        verify(expertQueryPort, times(1)).findExpertMapByIds(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrderElementsOf(ids);
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
