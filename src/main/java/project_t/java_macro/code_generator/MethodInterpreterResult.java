package project_t.java_macro.code_generator;

import java.util.Objects;

public sealed class MethodInterpreterResult {

    final static class NoValue extends MethodInterpreterResult {
        public static final NoValue INSTANCE = new NoValue();
    }

    public MethodInterpreterResult.Value asValue() {
        return (Value) this;
    }

    static MethodInterpreterResult noValue() {
        return NoValue.INSTANCE;
    }

    final static class Value extends MethodInterpreterResult {
        private final Object value;

        Value(Object value) {
            this.value = value;
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
            Value value1 = (Value) o;
            return Objects.equals(value, value1.value);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }
    }

    static MethodInterpreterResult of(Object value) {
        return new Value(value);
    }

}
