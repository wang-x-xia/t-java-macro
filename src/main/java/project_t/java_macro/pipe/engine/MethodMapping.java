package project_t.java_macro.pipe.engine;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitorWithDefaults;
import com.github.javaparser.ast.visitor.Visitable;
import project_t.java_macro.pipe.ShortCircuitConditionPipe;

import java.util.List;

public class MethodMapping extends GenericVisitorWithDefaults<Visitable, Void> {

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
                newStatements.add((Statement) r);
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
    public Statement visit(IfStmt n, Void arg) {
        Statement newN = ShortCircuitConditionPipe.visit(n);
        if (newN != n) {
            return (Statement) newN.accept(this, null);
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
            if (!changed) {
                return n;
            }
            n = n.clone();
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
