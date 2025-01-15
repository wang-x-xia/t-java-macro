package project_t.java_macro.code_generator;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults;

public class MethodInterpreter extends GenericVisitorWithDefaults<MethodInterpreter.Result, MethodInterpreterContext> {

    public record Result(MethodInterpreterValue returnValue, MethodInterpreterContext context) {
        public boolean hasReturn() {
            return returnValue != null;
        }
    }

    @Override
    public Result visit(BlockStmt n, MethodInterpreterContext ctx) {
        MethodInterpreterContext block = ctx.enterBlock();
        ctx = block;
        NodeList<Statement> list = n.getStatements();
        for (Statement statement : list) {
            Result result = statement.accept(this, ctx);
            if (result.returnValue().hasValue()) {
                // If the statement return value, return it and skip following statements
                return new Result(result.returnValue(), result.context().exitBlock(block));
            }
            ctx = result.context();
        }
        return new Result(null, ctx.exitBlock(block));
    }

    @Override
    public Result visit(IfStmt n, MethodInterpreterContext ctx) {
        ExpressionInterpreter.Result condition = n.getCondition().accept(ExpressionInterpreter.getInstance(), ctx);
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
                return new Result(null, ctx);
            }
            return n.getElseStmt().get().accept(this, ctx);
        }
    }

    @Override
    public Result visit(MethodDeclaration n, MethodInterpreterContext arg) {
        BlockStmt body = n.getBody().orElseThrow(() -> new AssertionError("Abstract Method is not allow"));
        Result result = body.accept(this, arg);
        if (!result.hasReturn()) {
            return new Result(MethodInterpreterValue.voidValue(), result.context());
        } else {
            return result;
        }
    }

    @Override
    public Result visit(ReturnStmt n, MethodInterpreterContext arg) {
        if (n.getExpression().isPresent()) {
            ExpressionInterpreter.Result result = n.getExpression().get().accept(ExpressionInterpreter.getInstance(), arg);
            return new Result(result.value(), result.context());
        } else {
            return new Result(MethodInterpreterValue.voidValue(), arg);
        }
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
