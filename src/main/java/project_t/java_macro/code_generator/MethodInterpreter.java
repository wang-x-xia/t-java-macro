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
    public MethodInterpreterResult visit(BinaryExpr n, MethodInterpreterContext arg) {
        MethodInterpreterResult.Value left = n.getLeft().accept(this, arg).asValue();
        switch (n.getOperator()) {
            case OR -> {
                if (left.bool()) {
                    return left;
                } else {
                    return n.getRight().accept(this, arg);
                }
            }
            case AND -> {
                if (left.bool()) {
                    return n.getRight().accept(this, arg);
                } else {
                    return left;
                }
            }
        }
        MethodInterpreterResult.Value right = n.getRight().accept(this, arg).asValue();
        switch (n.getOperator()) {
            case BINARY_OR -> {
                if (left.get() instanceof Boolean) {
                    return MethodInterpreterResult.of(left.bool() | right.bool());
                }
                switch (numericPromotion(List.of(left, right))) {
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() | right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() | right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            case BINARY_AND -> {
                if (left.get() instanceof Boolean) {
                    return MethodInterpreterResult.of(left.bool() & right.bool());
                }
                switch (numericPromotion(List.of(left, right))) {
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() & right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() & right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            case XOR -> {
                if (left.get() instanceof Boolean) {
                    return MethodInterpreterResult.of(left.bool() ^ right.bool());
                }
                switch (numericPromotion(List.of(left, right))) {
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() ^ right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() ^ right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            case EQUALS -> {
                return MethodInterpreterResult.of(left.get() == right.get());
            }
            case NOT_EQUALS -> {
                return MethodInterpreterResult.of(left.get() != right.get());
            }
            case LESS -> {
                switch (numericPromotion(List.of(left, right))) {
                    case DOUBLE -> {
                        return MethodInterpreterResult.of(left.asDouble() < right.asDouble());
                    }
                    case FLOAT -> {
                        return MethodInterpreterResult.of(left.asFloat() < right.asFloat());
                    }
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() < right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() < right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            case GREATER -> {
                switch (numericPromotion(List.of(left, right))) {
                    case DOUBLE -> {
                        return MethodInterpreterResult.of(left.asDouble() > right.asDouble());
                    }
                    case FLOAT -> {
                        return MethodInterpreterResult.of(left.asFloat() > right.asFloat());
                    }
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() > right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() > right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            case LESS_EQUALS -> {
                switch (numericPromotion(List.of(left, right))) {
                    case DOUBLE -> {
                        return MethodInterpreterResult.of(left.asDouble() <= right.asDouble());
                    }
                    case FLOAT -> {
                        return MethodInterpreterResult.of(left.asFloat() <= right.asFloat());
                    }
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() <= right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() <= right.asInt());
                    }
                    default -> throw new AssertionError();
                }

            }
            case GREATER_EQUALS -> {
                switch (numericPromotion(List.of(left, right))) {
                    case DOUBLE -> {
                        return MethodInterpreterResult.of(left.asDouble() >= right.asDouble());
                    }
                    case FLOAT -> {
                        return MethodInterpreterResult.of(left.asFloat() >= right.asFloat());
                    }
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() >= right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() >= right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            case LEFT_SHIFT -> {
                if (left.get() instanceof Long) {
                    return MethodInterpreterResult.of(left.asLong() << right.asInt());
                } else if (left.get() instanceof Integer) {
                    return MethodInterpreterResult.of(left.asInt() << right.asInt());
                } else {
                    throw new AssertionError();
                }
            }
            case SIGNED_RIGHT_SHIFT -> {
                if (left.get() instanceof Long) {
                    return MethodInterpreterResult.of(left.asLong() >> right.asInt());
                } else if (left.get() instanceof Integer) {
                    return MethodInterpreterResult.of(left.asInt() >> right.asInt());
                } else {
                    throw new AssertionError();
                }
            }
            case UNSIGNED_RIGHT_SHIFT -> {
                if (left.get() instanceof Long) {
                    return MethodInterpreterResult.of(left.asLong() >>> right.asInt());
                } else if (left.get() instanceof Integer) {
                    return MethodInterpreterResult.of(left.asInt() >>> right.asInt());
                } else {
                    throw new AssertionError();
                }
            }
            case PLUS -> {
                switch (numericPromotion(List.of(left, right))) {
                    case DOUBLE -> {
                        return MethodInterpreterResult.of(left.asDouble() + right.asDouble());
                    }
                    case FLOAT -> {
                        return MethodInterpreterResult.of(left.asFloat() + right.asFloat());
                    }
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() + right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() + right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            case MINUS -> {
                switch (numericPromotion(List.of(left, right))) {
                    case DOUBLE -> {
                        return MethodInterpreterResult.of(left.asDouble() - right.asDouble());
                    }
                    case FLOAT -> {
                        return MethodInterpreterResult.of(left.asFloat() - right.asFloat());
                    }
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() - right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() - right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            case MULTIPLY -> {
                switch (numericPromotion(List.of(left, right))) {
                    case DOUBLE -> {
                        return MethodInterpreterResult.of(left.asDouble() * right.asDouble());
                    }
                    case FLOAT -> {
                        return MethodInterpreterResult.of(left.asFloat() * right.asFloat());
                    }
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() * right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() * right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            case DIVIDE -> {
                switch (numericPromotion(List.of(left, right))) {
                    case DOUBLE -> {
                        return MethodInterpreterResult.of(left.asDouble() / right.asDouble());
                    }
                    case FLOAT -> {
                        return MethodInterpreterResult.of(left.asFloat() / right.asFloat());
                    }
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() / right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() / right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            case REMAINDER -> {
                switch (numericPromotion(List.of(left, right))) {
                    case DOUBLE -> {
                        return MethodInterpreterResult.of(left.asDouble() % right.asDouble());
                    }
                    case FLOAT -> {
                        return MethodInterpreterResult.of(left.asFloat() % right.asFloat());
                    }
                    case LONG -> {
                        return MethodInterpreterResult.of(left.asLong() % right.asLong());
                    }
                    case INT -> {
                        return MethodInterpreterResult.of(left.asInt() % right.asInt());
                    }
                    default -> throw new AssertionError();
                }
            }
            default -> throw new AssertionError();
        }
    }

    public enum NumberType {
        DOUBLE,
        FLOAT,
        LONG,
        INT,
    }

    /**
     * <a href="https://docs.oracle.com/javase/specs/jls/se21/html/jls-5.html#jls-5.6">5.6. Numeric Contexts</a>
     */
    public NumberType numericPromotion(List<MethodInterpreterResult.Value> values) {
        if (values.stream().anyMatch(it -> it.get() instanceof Double)) {
            return NumberType.DOUBLE;
        }
        if (values.stream().anyMatch(it -> it.get() instanceof Float)) {
            return NumberType.FLOAT;
        }
        if (values.stream().anyMatch(it -> it.get() instanceof Long)) {
            return NumberType.LONG;
        }
        return NumberType.INT;
    }

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
    public MethodInterpreterResult visit(CharLiteralExpr n, MethodInterpreterContext arg) {
        return MethodInterpreterResult.of(n.asChar());
    }

    @Override
    public MethodInterpreterResult visit(DoubleLiteralExpr n, MethodInterpreterContext arg) {
        return MethodInterpreterResult.of(n.asDouble());
    }

    @Override
    public MethodInterpreterResult visit(IfStmt n, MethodInterpreterContext arg) {
        MethodInterpreterResult condition = n.getCondition().accept(this, arg);
        if (!(condition instanceof MethodInterpreterResult.Value)) {
            throw new RuntimeException("Unknown result of condition: " + condition);
        }
        Object conditionValue = condition.asValue().get();
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
    public MethodInterpreterResult visit(LongLiteralExpr n, MethodInterpreterContext arg) {
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
            scope = n.getScope().orElseThrow().accept(this, arg).asValue().get();
        }
        List<Object> args = n.getArguments().stream().map(it -> it.accept(this, arg))
                .map(it -> ((MethodInterpreterResult.Value) it).get())
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
