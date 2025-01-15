package project_t.java_macro.code_generator.interpreter.context;

import project_t.java_macro.code_generator.InterpreterContext;
import project_t.java_macro.code_generator.InterpreterValue;

public class BaseContext implements InterpreterContext {

    final InterpreterContext parent;

    public BaseContext(InterpreterContext parent) {
        this.parent = parent;
    }

    @Override
    public InterpreterValue get(String name) {
        return this.parent.get(name);
    }

    @Override
    public InterpreterContext exitBlock(InterpreterContext block) {
        return this.parent.exitBlock(block);
    }
}
