package project_t.java_macro.code_generator.interpreter.context;

import project_t.java_macro.code_generator.InterpreterContext;
import project_t.java_macro.code_generator.InterpreterValue;

public class Root implements InterpreterContext {
    public Root() {
    }

    @Override
    public InterpreterValue get(String name) {
        throw new IllegalArgumentException("Unknown var " + name);
    }

    @Override
    public InterpreterContext exitBlock(InterpreterContext block) {
        throw new IllegalArgumentException("Unknown block " + block);
    }
}
