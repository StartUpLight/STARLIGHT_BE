package starlight.application.member.auth.required;

public interface KeyValueMap {

    void setValue(String key, String value, Long timeout);

    String getValue(String key);

    void deleteValue(String key);

    boolean checkExistsValue(String key);
}

