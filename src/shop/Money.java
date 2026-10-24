package shop;

public class Money {
    private final long cents;

    public Money(long cents) {
        this.cents = cents;
    }

    public static Money of(double amount) {
        return new Money(Math.round(amount * 100));
    }

    public Money plus(Money other) {
        return new Money(cents + other.cents);
    }

    public Money minus(Money other) {
        return new Money(cents - other.cents);
    }

    public Money times(double factor) {
        return new Money(Math.round(cents * factor));
    }

    public boolean isNegative() {
        return cents < 0;
    }

    public double toDouble() {
        return cents / 100.0;
    }

    @Override
    public String toString() {
        return String.format("%.2f", toDouble());
    }
}
