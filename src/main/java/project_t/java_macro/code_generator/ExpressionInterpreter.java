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

public class ExpressionInterpreter extends GenericVisitorWithDefaults<ExpressionInterpreter.Result, InterpreterContext> {

    private static final ExpressionInterpreter INSTANCE = new ExpressionInterpreter();

    public static ExpressionInterpreter getInstance() {
        return INSTANCE;
    }

    public record Result(InterpreterValue value, InterpreterContext context) {
    }

    @Override
    public Result visit(AssignExpr n, InterpreterContext ctx) {
        if (n.getTarget().isNameExpr()) {
            String name = n.getTarget().asNameExpr().getNameAsString();
            if (n.getOperator() == AssignExpr.Operator.ASSIGN) {
                Result result = n.getValue().accept(this, ctx);
                return new Result(result.value(), result.context().set(name, result.value()));
            }
            throw new AssertionError("Operator is not support yet, " + n.getOperator());
        }
        throw new AssertionError("Target is not support yet, " + n.getTarget().getClass());
    }

    @Override
    public Result visit(BooleanLiteralExpr n, InterpreterContext ctx) {
        return new Result(InterpreterValue.of(n.getValue()), ctx);
    }

    @Override
    public Result visit(CharLiteralExpr n, InterpreterContext ctx) {
        return new Result(InterpreterValue.of(n.asChar()), ctx);
    }

    @Override
    public Result visit(DoubleLiteralExpr n, InterpreterContext ctx) {
        return new Result(InterpreterValue.of(n.asDouble()), ctx);
    }

    @Override
    public Result visit(IntegerLiteralExpr n, InterpreterContext ctx) {
        return new Result(InterpreterValue.of(n.asNumber()), ctx);
    }

    @Override
    public Result visit(LongLiteralExpr n, InterpreterContext ctx) {
        return new Result(InterpreterValue.of(n.asNumber()), ctx);
    }

    @Override
    public Result visit(MethodCallExpr n, InterpreterContext arg) {
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
            return new Result(InterpreterValue.of(method.invoke(scope, args.toArray())), arg);
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
    public Result visit(NameExpr n, InterpreterContext ctx) {
        return new Result(ctx.get(n.getNameAsString()), ctx);
    }

    @Override
    public Result visit(NullLiteralExpr n, InterpreterContext ctx) {
        return new Result(InterpreterValue.of(null), ctx);
    }

    @Override
    public Result visit(StringLiteralExpr n, InterpreterContext ctx) {
        return new Result(InterpreterValue.of(n.getValue()), ctx);
    }

    @Override
    public Result visit(VariableDeclarationExpr n, InterpreterContext ctx) {
        // It's a statement
        throw new AssertionError("VariableDeclarationExpr is not expr");
    }

    @Override
    public Result visit(TextBlockLiteralExpr n, InterpreterContext ctx) {
        return new Result(InterpreterValue.of(n.getValue()), ctx);
    }

    @Override
    public Result defaultAction(Node n, InterpreterContext arg) {
        if (n instanceof Expression) {
            throw new IllegalArgumentException("Node is not supported yet, " + n.getClass());
        }
        throw new AssertionError("Node is not expr, " + n.getClass());
    }

    @Override
    public Result defaultAction(NodeList n, InterpreterContext arg) {
        throw new AssertionError("NodeList is not expr");
    }
}
