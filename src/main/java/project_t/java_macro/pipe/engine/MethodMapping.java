package project_t.java_macro.pipe.engine;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults;
import com.github.javaparser.ast.visitor.Visitable;
import project_t.java_macro.pipe.ShortCircuitConditionPipe;

import java.util.List;

public class MethodMapping extends GenericVisitorWithDefaults<Visitable, Void> {

    private static final ThreadLocal<String> methodName = new ThreadLocal<>();

    @Override
    public BlockStmt visit(BlockStmt n, Void arg) {
        boolean changed = false;
        while (true) {
            List<Statement> statements = n.getStatements();
            NodeList<Statement> newStatements = new NodeList<>();
            for (Statement statement : statements) {
                Visitable r = statement.accept(this, null);
                if (r != statement) {
                    changed = true;
                }
                if (r != null) {
                    newStatements.add((Statement) r);
                }
            }
            if (!changed) {
                return n;
            }
            n = n.clone();
            n.setStatements(newStatements);
            changed = false;
        }
    }

    @Override
    public Visitable visit(ExpressionStmt n, Void arg) {
        while (true) {
            Expression expression = n.getExpression();
            Expression newExpression = (Expression) expression.accept(this, null);
            if (newExpression == null) {
                return null;
            }
            if (newExpression == expression) {
                return n;
            }
            n = n.clone();
            n.setExpression(newExpression);
        }
    }

    @Override
    public Statement visit(IfStmt n, Void arg) {
        Statement newN = ShortCircuitConditionPipe.visit(n);
        if (newN != n) {
            return (Statement) newN.accept(this, null);
        }
        return n;
    }

    @Override
    public Visitable visit(MethodCallExpr n, Void arg) {
        if (n.getName().asString().equals("setMethodName")) {
            methodName.set(n.getArguments().get(0).asStringLiteralExpr().getValue());
            return null;
        }
        return n;
    }


    @Override
    public MethodDeclaration visit(MethodDeclaration n, Void arg) {
        boolean changed = false;
        while (true) {
            BlockStmt body = n.getBody().orElseThrow();
            BlockStmt newBody = visit(body, null);
            if (newBody != body) {
                changed = true;
            }
            if (!changed && methodName.get() == null) {
                return n;
            }
            n = n.clone();
            if (methodName.get() != null) {
                n.setName(methodName.get());
                methodName.remove();
            }
            n.setBody(newBody);
            changed = false;
        }
    }

    @Override
    public Visitable defaultAction(Node n, Void arg) {
        return n;
    }

    @Override
    public Visitable defaultAction(NodeList n, Void arg) {
        return n;
    }
}
