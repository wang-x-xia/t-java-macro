package project_t.java_macro.pipe.engine;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class MethodMappingTest {

    public void assertMethodEquals(String sourceMethodName, String generatedMethodName) {
        JavaParser parser = new JavaParser(new SymbolSolverCollectionStrategy().getParserConfiguration());
        Class<?> selfClazz = getClass();

        Path filePath = Paths.get("src/test/java/");
        for (String folder : selfClazz.getPackage().getName().split("\\.")) {
            filePath = filePath.resolve(folder);
        }

        filePath = filePath.resolve(selfClazz.getSimpleName() + ".java");

        CompilationUnit compilationUnit;
        try {
            compilationUnit = parser.parse(filePath).getResult().orElseThrow();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ClassOrInterfaceDeclaration classDeclaration = compilationUnit.getClassByName(selfClazz.getSimpleName())
                .orElseThrow();

        MethodDeclaration source = classDeclaration.getMethodsByName(sourceMethodName).get(0);
        MethodDeclaration expectedGenerated = classDeclaration.getMethodsByName(generatedMethodName).get(0);

        MethodMapping methodMapping = new MethodMapping();
        MethodDeclaration generated = methodMapping.visit(source, null);

        assertEquals(expectedGenerated.getBody().orElseThrow(), generated.getBody().orElseThrow(), "Generated should be match");
    }

}