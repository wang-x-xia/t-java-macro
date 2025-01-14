package project_t.java_macro.code_generator.interpreter.context;

import project_t.java_macro.code_generator.MethodInterpreterContext;

public class Block extends BaseContext {

    public Block(MethodInterpreterContext parent) {
        super(parent);
    }

    @Override
    public MethodInterpreterContext exitBlock(MethodInterpreterContext block) {
        if (block == this) {
            return parent;
        }
        return super.exitBlock(block);
    }
}
