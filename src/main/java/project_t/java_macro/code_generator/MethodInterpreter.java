package project_t.java_macro.code_generator;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.utils.TypeUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class MethodInterpreter extends GenericVisitorWithDefaults<MethodInterpreterResult, MethodInterpreterContext> {

    @Override
    public MethodInterpreterResult visit(BlockStmt n, MethodInterpreterContext arg) {
        NodeList<Statement> list = n.getStatements();
        for (Statement statement : list) {
            MethodInterpreterResult result = statement.accept(this, arg);
            if (result instanceof MethodInterpreterResult.Value) {
                // If the statement return value, return it and skip following statements
                return result;
            }
        }
        return MethodInterpreterResult.noValue();
    }

    @Override
    public MethodInterpreterResult visit(BooleanLiteralExpr n, MethodInterpreterContext arg) {
        return MethodInterpreterResult.of(n.getValue());
    }

    @Override
    public MethodInterpreterResult visit(IfStmt n, MethodInterpreterContext arg) {
        MethodInterpreterResult condition = n.getCondition().accept(this, arg);
        if (!(condition instanceof MethodInterpreterResult.Value)) {
            throw new RuntimeException("Unknown result of condition: " + condition);
        }
        Object conditionValue = ((MethodInterpreterResult.Value) condition).getValue();
        if (!(conditionValue instanceof Boolean)) {
            throw new RuntimeException("Unknown type of condition value: " + condition);
        }
        if ((Boolean) conditionValue) {
            return n.getThenStmt().accept(this, arg);
        } else {
            return n.getElseStmt().map(s -> s.accept(this, arg)).orElse(MethodInterpreterResult.noValue());
        }
    }


    @Override
    public MethodInterpreterResult visit(IntegerLiteralExpr n, MethodInterpreterContext arg) {
        return MethodInterpreterResult.of(n.asNumber());
    }

    @Override
    public MethodInterpreterResult visit(MethodCallExpr n, MethodInterpreterContext arg) {
        ResolvedMethodDeclaration resolvedMethod = n.resolve();
        Method method = methodReflection(resolvedMethod);
        Object scope;
        if (resolvedMethod.isStatic()) {
            scope = null;
        } else {
            scope = ((MethodInterpreterResult.Value) n.getScope().orElseThrow().accept(this, arg)).getValue();
        }
        List<Object> args = n.getArguments().stream().map(it -> it.accept(this, arg))
                .map(it -> ((MethodInterpreterResult.Value) it).getValue())
                .toList();
        try {
            return MethodInterpreterResult.of(method.invoke(scope, args.toArray()));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }

    public Method methodReflection(ResolvedMethodDeclaration method) {
        Class<?> clazz = loadClazz(method.declaringType().getQualifiedName());
        return Arrays.stream(clazz.getMethods()).filter(it -> it.getName().equals(method.getName()))
                .filter(it -> method.toDescriptor().equals(TypeUtils.getMethodDescriptor(it)))
                .findFirst().orElseThrow(() -> new AssertionError("Method not found: " + method));
    }

    public Class<?> loadClazz(String clazzName) {
        try {
            return Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public MethodInterpreterResult visit(MethodDeclaration n, MethodInterpreterContext arg) {
        BlockStmt result = n.getBody().orElseThrow(() -> new AssertionError("Abstract Method is not allow"));
        return result.accept(this, arg);
    }

    @Override
    public MethodInterpreterResult visit(NullLiteralExpr n, MethodInterpreterContext arg) {
        return MethodInterpreterResult.of(null);
    }

    @Override
    public MethodInterpreterResult visit(ReturnStmt n, MethodInterpreterContext arg) {
        if (n.getExpression().isPresent()) {
            return n.getExpression().get().accept(this, arg);
        } else {
            return MethodInterpreterResult.noValue();
        }
    }

    @Override
    public MethodInterpreterResult visit(StringLiteralExpr n, MethodInterpreterContext arg) {
        return MethodInterpreterResult.of(n.getValue());
    }

    @Override
    public MethodInterpreterResult visit(TextBlockLiteralExpr n, MethodInterpreterContext arg) {
        return MethodInterpreterResult.of(n.getValue());
    }

    @Override
    public MethodInterpreterResult defaultAction(Node n, MethodInterpreterContext arg) {
        throw new AssertionError("Unhandled node: " + n.getClass());
    }

    @Override
    public MethodInterpreterResult defaultAction(NodeList n, MethodInterpreterContext arg) {
        throw new AssertionError("Unhandled node: " + n);
    }
}
