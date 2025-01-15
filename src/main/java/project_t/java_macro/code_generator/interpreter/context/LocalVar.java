package project_t.java_macro.code_generator.interpreter.context;

import project_t.java_macro.code_generator.InterpreterContext;
import project_t.java_macro.code_generator.InterpreterValue;

public class LocalVar extends BaseContext {

    private final String name;
    private final InterpreterValue value;

    public LocalVar(InterpreterContext parent, String name, InterpreterValue value) {
        super(parent);
        this.name = name;
        this.value = value;
    }

    @Override
    public InterpreterValue get(String name) {
        if (name.equals(this.name)) {
            return value;
        }
        return super.get(name);
    }

}
