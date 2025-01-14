package project_t.java_macro.code_generator.interpreter.context;

import project_t.java_macro.code_generator.MethodInterpreterContext;
import project_t.java_macro.code_generator.MethodInterpreterValue;

public class Root implements MethodInterpreterContext {
    public Root() {
    }

    @Override
    public MethodInterpreterValue get(String name) {
        throw new IllegalArgumentException("Unknown var " + name);
    }

    @Override
    public MethodInterpreterContext exitBlock(MethodInterpreterContext block) {
        throw new IllegalArgumentException("Unknown block " + block);
    }
}
