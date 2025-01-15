package project_t.java_macro.code_generator.interpreter.context;

import project_t.java_macro.code_generator.InterpreterContext;

public class Block extends BaseContext {

    public Block(InterpreterContext parent) {
        super(parent);
    }

    @Override
    public InterpreterContext exitBlock(InterpreterContext block) {
        if (block == this) {
            return parent;
        }
        return super.exitBlock(block);
    }
}
