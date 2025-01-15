package project_t.java_macro.code_generator;

import project_t.java_macro.code_generator.interpreter.context.Block;
import project_t.java_macro.code_generator.interpreter.context.LocalVar;
import project_t.java_macro.code_generator.interpreter.context.Root;

public interface InterpreterContext {

    static InterpreterContext root() {
        return new Root();
    }

    /**
     * @param name is the local var name
     * @return is the value of local var
     */
    InterpreterValue get(String name);

    /**
     * Declare a local var
     *
     * @param name is the local var name
     * @return new context
     */
    default InterpreterContext localVar(String name) {
        return new LocalVar(this, name, null);
    }

    /**
     * @param name  is the local var name
     * @param value is the new value
     * @return new context
     */
    default InterpreterContext set(String name, InterpreterValue value) {
        return new LocalVar(this, name, value);
    }

    /**
     * Create a new block scope.
     *
     * @return new context
     */
    default InterpreterContext enterBlock() {
        return new Block(this);
    }

    /**
     * Create a new block scope.
     *
     * @return new context
     */
    InterpreterContext exitBlock(InterpreterContext block);
}
