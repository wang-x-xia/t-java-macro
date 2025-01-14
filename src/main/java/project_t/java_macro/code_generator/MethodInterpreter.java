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

public class MethodInterpreter extends GenericVisitorWithDefaults<MethodInterpreter.Result, MethodInterpreterContext> {

    public record Result(MethodInterpreterValue value, MethodInterpreterContext context) {
    }

    @Override
    public Result visit(BlockStmt n, MethodInterpreterContext ctx) {
        MethodInterpreterContext block = ctx.enterBlock();
        ctx = block;
        NodeList<Statement> list = n.getStatements();
        for (Statement statement : list) {
            Result result = statement.accept(this, ctx);
            if (result.value().hasValue()) {
                // If the statement return value, return it and skip following statements
                return new Result(result.value(), result.context().exitBlock(block));
            }
            ctx = result.context();
        }
        return new Result(MethodInterpreterValue.noValue(), ctx.exitBlock(block));
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
    public Result visit(IfStmt n, MethodInterpreterContext ctx) {
        Result condition = n.getCondition().accept(this, ctx);
        if (condition.context() != ctx) {
            // TODO
            throw new RuntimeException("Current interpreter can't process pattern match");
        }
        if (!(condition.value().hasValue())) {
            throw new RuntimeException("The if condition must have return value");
        }
        if (condition.value().isUnknown()) {
            throw new RuntimeException("Current interpreter can't process unknown if");
        } else if (condition.value().bool()) {
            return n.getThenStmt().accept(this, ctx);
        } else {
            if (n.getElseStmt().isEmpty()) {
                // No value, no changed ctx
                return new Result(MethodInterpreterValue.noValue(), ctx);
            }
            return n.getElseStmt().get().accept(this, ctx);
        }
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
    public Result visit(MethodDeclaration n, MethodInterpreterContext arg) {
        BlockStmt result = n.getBody().orElseThrow(() -> new AssertionError("Abstract Method is not allow"));
        return result.accept(this, arg);
    }

    @Override
    public Result visit(NullLiteralExpr n, MethodInterpreterContext ctx) {
        return new Result(MethodInterpreterValue.of(null), ctx);
    }

    @Override
    public Result visit(ReturnStmt n, MethodInterpreterContext arg) {
        if (n.getExpression().isPresent()) {
            return n.getExpression().get().accept(this, arg);
        } else {
            return new Result(MethodInterpreterValue.voidValue(), arg);
        }
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
        throw new AssertionError("Unhandled node: " + n.getClass());
    }

    @Override
    public Result defaultAction(NodeList n, MethodInterpreterContext arg) {
        throw new AssertionError("Unhandled node: " + n);
    }
}
