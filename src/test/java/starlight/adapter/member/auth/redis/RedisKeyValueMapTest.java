package starlight.adapter.member.auth.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import starlight.shared.apiPayload.exception.GlobalException;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisKeyValueMapTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisKeyValueMap redisKeyValueMap;

    @Test
    @DisplayName("setValue 성공")
    void setValue_Success() {
        // given
        String key = "testKey";
        String value = "testValue";
        Long timeout = 3600000L;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        // when
        redisKeyValueMap.setValue(key, value, timeout);

        // then
        verify(valueOperations).set(eq(key), eq(value), eq(Duration.ofMillis(timeout)));
    }

    @Test
    @DisplayName("setValue 실패 - Redis 에러")
    void setValue_Fail() {
        // given
        String key = "testKey";
        String value = "testValue";
        Long timeout = 3600000L;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        doThrow(new RuntimeException()).when(valueOperations).set(any(), any(), any(Duration.class));

        // when & then
        assertThatThrownBy(() -> redisKeyValueMap.setValue(key, value, timeout))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    @DisplayName("getValue 성공")
    void getValue_Success() {
        // given
        String key = "testKey";
        String expectedValue = "testValue";
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(expectedValue);

        // when
        String result = redisKeyValueMap.getValue(key);

        // then
        assertThat(result).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("getValue 실패 - 값이 없는 경우")
    void getValue_ReturnEmptyString() {
        // given
        String key = "testKey";
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willReturn(null);

        // when
        String result = redisKeyValueMap.getValue(key);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getValue 실패 - Redis 에러")
    void getValue_Fail() {
        // given
        String key = "testKey";
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(key)).willThrow(new RuntimeException());

        // when & then
        assertThatThrownBy(() -> redisKeyValueMap.getValue(key))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    @DisplayName("deleteValue 성공")
    void deleteValue_Success() {
        // given
        String key = "testKey";
        given(redisTemplate.delete(key)).willReturn(true);

        // when
        redisKeyValueMap.deleteValue(key);

        // then
        verify(redisTemplate).delete(key);
    }

    @Test
    @DisplayName("deleteValue 실패 - Redis 에러")
    void deleteValue_Fail() {
        // given
        String key = "testKey";
        doThrow(new RuntimeException()).when(redisTemplate).delete(key);

        // when & then
        assertThatThrownBy(() -> redisKeyValueMap.deleteValue(key))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    @DisplayName("checkExistsValue 성공 - 존재하는 경우")
    void checkExistsValue_True() {
        // given
        String key = "testKey";
        given(redisTemplate.hasKey(key)).willReturn(true);

        // when
        boolean result = redisKeyValueMap.checkExistsValue(key);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkExistsValue 성공 - 존재하지 않는 경우")
    void checkExistsValue_False() {
        // given
        String key = "testKey";
        given(redisTemplate.hasKey(key)).willReturn(false);

        // when
        boolean result = redisKeyValueMap.checkExistsValue(key);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("checkExistsValue 실패 - Redis 에러")
    void checkExistsValue_Fail() {
        // given
        String key = "testKey";
        given(redisTemplate.hasKey(key)).willThrow(new RuntimeException());

        // when & then
        assertThatThrownBy(() -> redisKeyValueMap.checkExistsValue(key))
                .isInstanceOf(GlobalException.class);
    }
}
