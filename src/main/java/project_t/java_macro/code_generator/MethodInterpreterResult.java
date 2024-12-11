package project_t.java_macro.code_generator;

import java.util.Objects;

public sealed class MethodInterpreterResult {

    final static class NoValue extends MethodInterpreterResult {
        public static final NoValue INSTANCE = new NoValue();
    }

    static MethodInterpreterResult noValue() {
        return NoValue.INSTANCE;
    }

    final static class Value extends MethodInterpreterResult {
        private final Object value;

        Value(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
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
