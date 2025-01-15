package project_t.java_macro.code_generator;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.utils.TypeUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ExpressionInterpreter extends GenericVisitorWithDefaults<ExpressionInterpreter.Result, MethodInterpreterContext> {

    private static final ExpressionInterpreter INSTANCE = new ExpressionInterpreter();

    public static ExpressionInterpreter getInstance() {
        return INSTANCE;
    }

    public record Result(MethodInterpreterValue value, MethodInterpreterContext context) {
    }

    @Override
    public Result visit(BooleanLiteralExpr n, MethodInterpreterContext ctx) {
        return new Result(MethodInterpreterValue.of(n.getValue()), ctx);
    }

    @Override
    public Result visit(CharLiteralExpr n, MethodInterpreterContext ctx) {
        return new Result(MethodInterpreterValue.of(n.asChar()), ctx);
    }

    @Override
    public Result visit(DoubleLiteralExpr n, MethodInterpreterContext ctx) {
        return new Result(MethodInterpreterValue.of(n.asDouble()), ctx);
    }

    @Override
    public Result visit(IntegerLiteralExpr n, MethodInterpreterContext ctx) {
        return new Result(MethodInterpreterValue.of(n.asNumber()), ctx);
    }

    @Override
    public Result visit(LongLiteralExpr n, MethodInterpreterContext ctx) {
        return new Result(MethodInterpreterValue.of(n.asNumber()), ctx);
    }

    @Override
    public Result visit(MethodCallExpr n, MethodInterpreterContext arg) {
        ResolvedMethodDeclaration resolvedMethod = n.resolve();
        Method method = methodReflection(resolvedMethod);
        Object scope;
        if (resolvedMethod.isStatic()) {
            scope = null;
        } else {
            scope = n.getScope().orElseThrow().accept(this, arg).value().get();
        }
        List<Object> args = n.getArguments().stream().map(it -> it.accept(this, arg))
                .map(it -> it.value().get())
                .toList();
        try {
            return new Result(MethodInterpreterValue.of(method.invoke(scope, args.toArray())), arg);
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
    public Result visit(NullLiteralExpr n, MethodInterpreterContext ctx) {
        return new Result(MethodInterpreterValue.of(null), ctx);
    }

    @Override
    public Result visit(StringLiteralExpr n, MethodInterpreterContext ctx) {
        return new Result(MethodInterpreterValue.of(n.getValue()), ctx);
    }

    @Override
    public Result visit(TextBlockLiteralExpr n, MethodInterpreterContext ctx) {
        return new Result(MethodInterpreterValue.of(n.getValue()), ctx);
    }

    @Override
    public Result defaultAction(Node n, MethodInterpreterContext arg) {
        if (n instanceof Expression) {
            throw new IllegalArgumentException("Node is not supported yet, " + n.getClass());
        }
        throw new AssertionError("Node is not expr, " + n.getClass());
    }

    @Override
    public Result defaultAction(NodeList n, MethodInterpreterContext arg) {
        throw new AssertionError("NodeList is not expr");
    }
}
