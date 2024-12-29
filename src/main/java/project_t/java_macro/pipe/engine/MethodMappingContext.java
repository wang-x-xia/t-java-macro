package project_t.java_macro.pipe.engine;

@SuppressWarnings("unused")
public interface MethodMappingContext {

    @EngineCall
    static void setMethodName(String name) {
        MethodMapping.addCustomizer(m -> m.setName(name));
    }
}
