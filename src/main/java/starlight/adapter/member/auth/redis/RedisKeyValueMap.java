package starlight.adapter.member.auth.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import starlight.application.member.auth.required.KeyValueMap;
import starlight.shared.apiPayload.exception.GlobalErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisKeyValueMap implements KeyValueMap {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 지정된 키에 값을 저장합니다.
     *
     * @param key 저장할 키
     * @param value 저장할 값
     * @param timeout (선택 사항) 값의 만료 시간(초 단위). null이면 만료되지 않음.
     */
    public void setValue(String key, String value, Long timeout) {
        try {
            ValueOperations<String, Object> values = redisTemplate.opsForValue();
            values.set(key, value, Duration.ofMillis(timeout));
        } catch (Exception e) {
            throw new GlobalException(GlobalErrorType.REDIS_SET_ERROR);
        }
    }

    /**
     * 지정된 키에 대한 값을 가져옵니다.
     *
     * @param key 값을 가져올 키
     * @return 키에 해당하는 값, 키가 존재하지 않으면 null
     */
    public String getValue(String key) {
        try {
            ValueOperations<String, Object> values = redisTemplate.opsForValue();
            Object value = values.get(key);
            return value == null ? null : value.toString();
        } catch (Exception e) {
            throw new GlobalException(GlobalErrorType.REDIS_GET_ERROR);
        }
    }

    /**
     * 지정된 키에 대한 값을 삭제합니다.
     *
     * @param key 삭제할 키
     */
    public void deleteValue(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            throw new GlobalException(GlobalErrorType.REDIS_DELETE_ERROR);
        }
    }

    /**
     * 지정된 키가 존재하는지 확인합니다.
     *
     * @param key 확인할 키
     * @return 키가 존재하면 true, 그렇지 않으면 false
     */
    public boolean checkExistsValue(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            throw new GlobalException(GlobalErrorType.REDIS_GET_ERROR);
        }
    }
}
