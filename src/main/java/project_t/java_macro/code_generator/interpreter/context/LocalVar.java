package project_t.java_macro.code_generator.interpreter.context;

import project_t.java_macro.code_generator.MethodInterpreterContext;
import project_t.java_macro.code_generator.MethodInterpreterValue;

public class LocalVar extends BaseContext {

    private final String name;
    private final MethodInterpreterValue value;

    public LocalVar(MethodInterpreterContext parent, String name, MethodInterpreterValue value) {
        super(parent);
        this.name = name;
        this.value = value;
    }

    @Override
    public MethodInterpreterValue get(String name) {
        if (name.equals(this.name)) {
            return value;
        }
        return super.get(name);
    }

}
