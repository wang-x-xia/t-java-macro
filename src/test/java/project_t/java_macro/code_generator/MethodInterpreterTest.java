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

@SuppressWarnings({"unused"})
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
                    MethodInterpreter.Result actual = classDeclaration.getMethodsByName(method.getName()).get(0).accept(new MethodInterpreter(), InterpreterContext.root());
                    if (method.getReturnType() == void.class) {
                        assertEquals(InterpreterValue.voidValue(), actual.returnValue(),
                                "Void method should return no value");
                    } else {
                        assertEquals(method.invoke(this), actual.returnValue().get(),
                                "Method should return expected value");
                    }
                }));
    }

    @NoArgTest
    public void voidMethod() {
    }

    @SuppressWarnings({"OnlyOneElementUsed", "ResultOfMethodCallIgnored"})
    @NoArgTest
    public int statementMethod() {
        "Test".charAt(0);
        return 7;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @NoArgTest
    public int finalVar() {
        int value = 10;
        return value;
    }

    @SuppressWarnings("UnusedAssignment")
    @NoArgTest
    public int mutableVar() {
        int value = 10;
        return value = 3;
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

//    @NoArgTest
//    public boolean boolAnd() {
//        return true && false;
//    }
//
//    @NoArgTest
//    public boolean boolOr() {
//        return false || true;
//    }
//
//    @NoArgTest
//    public int addInt() {
//        return 1 + 'a';
//    }
//
//    @NoArgTest
//    public long addLong() {
//        return 1L + 100;
//    }
//
//    @NoArgTest
//    public double addDouble() {
//        return 1 + 0.5;
//    }
}