package project_t.java_macro.code_generator;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults;

public class MethodInterpreter extends GenericVisitorWithDefaults<MethodInterpreter.Result, InterpreterContext> {

    public record Result(InterpreterValue returnValue, InterpreterContext context) {
        public boolean hasReturn() {
            return returnValue != null;
        }
    }

    @Override
    public Result visit(BlockStmt n, InterpreterContext ctx) {
        InterpreterContext block = ctx.enterBlock();
        ctx = block;
        NodeList<Statement> list = n.getStatements();
        for (Statement statement : list) {
            Result result = statement.accept(this, ctx);
            if (result.hasReturn()) {
                // If the statement return value, return it and skip following statements
                return new Result(result.returnValue(), result.context().exitBlock(block));
            }
            ctx = result.context();
        }
        return new Result(null, ctx.exitBlock(block));
    }

    @Override
    public Result visit(ExpressionStmt n, InterpreterContext ctx) {
        if (n.getExpression().isVariableDeclarationExpr()) {
            // Process LocalVariableDeclarationStatement
            return visit(n.getExpression().asVariableDeclarationExpr(), ctx);
        }
        // Ignore the value of expression
        return new Result(null, n.getExpression().accept(ExpressionInterpreter.getInstance(), ctx).context());
    }

    @Override
    public Result visit(IfStmt n, InterpreterContext ctx) {
        ExpressionInterpreter.Result condition = n.getCondition().accept(ExpressionInterpreter.getInstance(), ctx);
        if (condition.context() != ctx) {
            // TODO
            throw new RuntimeException("Current interpreter can't process pattern match");
        }
        if (condition.value().isUnknown()) {
            throw new RuntimeException("Current interpreter can't process unknown if");
        } else if (condition.value().bool()) {
            return n.getThenStmt().accept(this, ctx);
        } else {
            if (n.getElseStmt().isEmpty()) {
                // No value, no changed ctx
                return new Result(null, ctx);
            }
            return n.getElseStmt().get().accept(this, ctx);
        }
    }

    @Override
    public Result visit(MethodDeclaration n, InterpreterContext arg) {
        BlockStmt body = n.getBody().orElseThrow(() -> new AssertionError("Abstract Method is not allow"));
        Result result = body.accept(this, arg);
        if (!result.hasReturn()) {
            return new Result(InterpreterValue.voidValue(), result.context());
        } else {
            return result;
        }
    }

    @Override
    public Result visit(ReturnStmt n, InterpreterContext arg) {
        if (n.getExpression().isPresent()) {
            ExpressionInterpreter.Result result = n.getExpression().get().accept(ExpressionInterpreter.getInstance(), arg);
            return new Result(result.value(), result.context());
        } else {
            return new Result(InterpreterValue.voidValue(), arg);
        }
    }

    @Override
    public Result visit(VariableDeclarationExpr n, InterpreterContext ctx) {
        NodeList<VariableDeclarator> variables = n.getVariables();
        for (VariableDeclarator variable : variables) {
            ctx = ctx.localVar(variable.getNameAsString());
            if (variable.getInitializer().isPresent()) {
                ExpressionInterpreter.Result result = variable.getInitializer().get().accept(ExpressionInterpreter.getInstance(), ctx);
                ctx = ctx.set(variable.getNameAsString(), result.value());
            }
        }
        return new Result(null, ctx);
    }

    @Override
    public Result defaultAction(Node n, InterpreterContext arg) {
        throw new AssertionError("Unhandled node: " + n.getClass());
    }

    @Override
    public Result defaultAction(NodeList n, InterpreterContext arg) {
        throw new AssertionError("Unhandled node: " + n);
    }
}
