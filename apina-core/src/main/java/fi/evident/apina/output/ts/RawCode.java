package fi.evident.apina.output.ts;

/**
 * Represents raw piece of code written as it is when passed to {@link CodeWriter#writeValue(Object)}.
 */
final class RawCode {

    private final Object value;

    RawCode(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
