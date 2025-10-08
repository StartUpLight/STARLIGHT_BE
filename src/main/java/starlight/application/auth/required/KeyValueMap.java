package starlight.application.auth.required;

/**
 * 키-값 저장소 인터페이스.
 * 다양한 구현체(Redis, In-Memory 등)에서 사용할 수 있습니다.
 */
public interface KeyValueMap {

    /**
     * 지정된 키에 값을 저장합니다.
     *
     * @param key 저장할 키
     * @param value 저장할 값
     * @param timeout (선택 사항) 값의 만료 시간(초 단위). null이면 만료되지 않음.
     */
    void setValue(String key, String value, Long timeout);

    /**
     * 지정된 키에 대한 값을 가져옵니다.
     *
     * @param key 값을 가져올 키
     * @return 키에 해당하는 값, 키가 존재하지 않으면 null
     */
    String getValue(String key);

    /**
     * 지정된 키에 대한 값을 삭제합니다.
     *
     * @param key 삭제할 키
     */
    void deleteValue(String key);

    /**
     * 지정된 키가 존재하는지 확인합니다.
     *
     * @param key 확인할 키
     * @return 키가 존재하면 true, 그렇지 않으면 false
     */
    boolean checkExistsValue(String key);
}

