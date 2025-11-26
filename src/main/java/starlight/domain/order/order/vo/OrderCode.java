package starlight.domain.order.order.vo;

import java.util.Objects;

/**
 * 주문번호를 표현하는 값 객체
 * 프론트엔드에서 생성한 주문번호를 검증하고 캡슐화
 */
public class OrderCode {

    private final String value;

    private OrderCode(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("주문번호는 필수입니다.");
        }
        if (value.length() > 64) {
            throw new IllegalArgumentException("주문번호는 64자를 초과할 수 없습니다.");
        }
        if (!value.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("주문번호는 영문, 숫자, -, _ 만 허용됩니다.");
        }
    }

    /**
     * 프론트엔드에서 전달받은 주문번호로 값 객체 생성
     */
    public static OrderCode of(String value) {
        return new OrderCode(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderCode)) return false;
        OrderCode orderCode = (OrderCode) o;
        return Objects.equals(value, orderCode.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
