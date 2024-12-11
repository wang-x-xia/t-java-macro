package project_t.java_macro.code_generator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("unused")
class MethodInterpreterTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface NoArgTest {
    }

    @TestFactory
    Stream<DynamicTest> testMethodInterpreter() throws IOException {
        JavaParser parser = new JavaParser(new SymbolSolverCollectionStrategy().getParserConfiguration());
        Path filePath = Paths.get("src/test/java/project_t/java_macro/code_generator/MethodInterpreterTest.java");
        CompilationUnit compilationUnit = parser.parse(filePath).getResult().orElseThrow();
        ClassOrInterfaceDeclaration classDeclaration = compilationUnit.getClassByName("MethodInterpreterTest")
                .orElseThrow();
        return AnnotationSupport.findAnnotatedMethods(MethodInterpreterTest.class, NoArgTest.class, HierarchyTraversalMode.TOP_DOWN)
                .stream().map(method -> DynamicTest.dynamicTest(method.getName(), () -> {
                    MethodInterpreterResult actual = classDeclaration.getMethodsByName(method.getName()).get(0).accept(new MethodInterpreter(), new MethodInterpreterContext());
                    if (method.getReturnType() == void.class) {
                        assertEquals(MethodInterpreterResult.noValue(), actual,
                                "Void method should return no value");
                    } else {
                        assertEquals(MethodInterpreterResult.of(method.invoke(this)), actual,
                                "Method should return expected value");
                    }
                }));
    }

    @NoArgTest
    public void voidMethod() {
    }

    @NoArgTest
    public String stringLiteral() {
        return "ok";
    }

    @NoArgTest
    public int intLiteral() {
        return 1;
    }

    @NoArgTest
    @SuppressWarnings("ConstantValue")
    public int ifTest() {
        if (true) {
            return 1;
        } else {
            return 2;
        }
    }

    @NoArgTest
    @SuppressWarnings("ConstantValue")
    public int ifNotTest() {
        if (false) {
            return 1;
        } else {
            return 2;
        }
    }

    @NoArgTest
    public int staticMethod() {
        return Math.min(1, 2);
    }

    @NoArgTest
    public int memberMethod() {
        return "ok".length();
    }

}