package project_t.java_macro.code_generator.interpreter.context;

import project_t.java_macro.code_generator.MethodInterpreterContext;
import project_t.java_macro.code_generator.MethodInterpreterValue;

public class BaseContext implements MethodInterpreterContext {

    final MethodInterpreterContext parent;

    public BaseContext(MethodInterpreterContext parent) {
        this.parent = parent;
    }

    @Override
    public MethodInterpreterValue get(String name) {
        return this.parent.get(name);
    }

    @Override
    public MethodInterpreterContext exitBlock(MethodInterpreterContext block) {
        return this.parent.exitBlock(block);
    }
}
