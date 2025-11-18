    package starlight.order.toss.domain;

import java.util.Objects;

/**
 * 금액을 표현하는 값 객체
 * 불변 객체로 통화와 금액을 함께 관리
 */
public class Money {

    private final Long amount;
    private final String currency;

    private Money(Long amount, String currency) {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다.");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("통화는 필수입니다.");
        }
        this.amount = amount;
        this.currency = currency.toUpperCase();
    }

    public static Money of(Long amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money krw(Long amount) {
        return new Money(amount, "KRW");
    }

    public boolean equals(Money other) {
        if (other == null) return false;
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("다른 통화는 비교할 수 없습니다.");
        }
        return Objects.equals(this.amount, other.amount);
    }

    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("다른 통화는 비교할 수 없습니다.");
        }
        return this.amount > other.amount;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) &&
                Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return currency + " " + amount;
    }
}