package project_t.java_macro.code_generator;

import java.util.Objects;

public class MethodInterpreterValue {

    private static final MethodInterpreterValue NO_VALUE = new MethodInterpreterValue(false, false, null, null);

    public static MethodInterpreterValue noValue() {
        return NO_VALUE;
    }

    private static final MethodInterpreterValue UNKNOWN_VALUE = new MethodInterpreterValue(true, true, null, null);

    public static MethodInterpreterValue unknown() {
        return UNKNOWN_VALUE;
    }

    private static final MethodInterpreterValue VOID_VALUE = new MethodInterpreterValue(true, false, null, Void.class);

    public static MethodInterpreterValue voidValue() {
        return VOID_VALUE;
    }

    private static final MethodInterpreterValue NULL_VALUE = new MethodInterpreterValue(true, false, null, null);

    public static MethodInterpreterValue of(Object value) {
        if (value == null) {
            return NULL_VALUE;
        }
        return new MethodInterpreterValue(true, false, value, value.getClass());
    }


    private final boolean hasValue;
    private final boolean unknown;
    private final Object value;
    private final Class<?> type;

    private MethodInterpreterValue(boolean hasValue, boolean unknown, Object value, Class<?> type) {
        this.hasValue = hasValue;
        this.unknown = unknown;
        this.value = value;
        this.type = type;
    }

    public boolean hasValue() {
        return hasValue;
    }

    public boolean isUnknown() {
        return unknown;
    }

    public Object get() {
        return value;
    }

    public boolean bool() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else {
            throw new AssertionError("Not a boolean value: " + value);
        }
    }

    public double asDouble() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof Character) {
            return (Character) value;
        } else {
            throw new AssertionError("Not a double value: " + value);
        }
    }

    public float asFloat() {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value instanceof Character) {
            return (Character) value;
        } else {
            throw new AssertionError("Not a float value: " + value);
        }
    }

    public long asLong() {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof Character) {
            return (Character) value;
        } else {
            throw new AssertionError("Not a long value: " + value);
        }
    }

    public int asInt() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof Character) {
            return (Character) value;
        } else {
            throw new AssertionError("Not a int value: " + value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MethodInterpreterValue value1 = (MethodInterpreterValue) o;
        return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
