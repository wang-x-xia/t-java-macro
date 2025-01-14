package project_t.java_macro.code_generator;

import project_t.java_macro.code_generator.interpreter.context.Block;
import project_t.java_macro.code_generator.interpreter.context.LocalVar;
import project_t.java_macro.code_generator.interpreter.context.Root;

public interface MethodInterpreterContext {

    static MethodInterpreterContext root() {
        return new Root();
    }

    /**
     * @param name is the local var name
     * @return is the value of local var
     */
    MethodInterpreterValue get(String name);

    /**
     * Declare a local var
     *
     * @param name is the local var name
     * @return new context
     */
    default MethodInterpreterContext localVar(String name) {
        return new LocalVar(this, name, MethodInterpreterValue.noValue());
    }

    /**
     * @param name  is the local var name
     * @param value is the new value
     * @return new context
     */
    default MethodInterpreterContext set(String name, MethodInterpreterValue value) {
        if (!value.hasValue()) {
            throw new IllegalArgumentException("If declare a new local var, use localVar()");
        }
        return new LocalVar(this, name, value);
    }

    /**
     * Create a new block scope.
     *
     * @return new context
     */
    default MethodInterpreterContext enterBlock() {
        return new Block(this);
    }

    /**
     * Create a new block scope.
     *
     * @return new context
     */
    MethodInterpreterContext exitBlock(MethodInterpreterContext block);
}
