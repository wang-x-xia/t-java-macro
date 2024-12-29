package project_t.java_macro.pipe;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;

public class ShortCircuitConditionPipe {

    public static Expression visit(BinaryExpr n) {
        if (n.getOperator() == BinaryExpr.Operator.OR) {
            if (n.getLeft().isBooleanLiteralExpr()) {
                if (n.getLeft().asBooleanLiteralExpr().getValue()) {
                    // true or x is true
                    return n.getLeft();
                } else {
                    // false or x is x
                    return n.getRight();
                }
            }
        } else if (n.getOperator() == BinaryExpr.Operator.AND) {
            if (n.getLeft().isBooleanLiteralExpr()) {
                if (n.getLeft().asBooleanLiteralExpr().getValue()) {
                    // true and x is x
                    return n.getRight();
                } else {
                    // false and x is false
                    return n.getLeft();
                }
            }
        }
        return n;
    }

    public static Expression visit(ConditionalExpr n) {
        Expression condition = n.getCondition();
        if (condition.isBooleanLiteralExpr()) {
            if (condition.asBooleanLiteralExpr().getValue()) {
                return n.getThenExpr();
            } else {
                return n.getElseExpr();
            }
        } else {
            return n;
        }
    }

    public static Statement visit(DoStmt n) {
        if (isLiteralFalse(n.getCondition())) {
            return n.getBody();
        } else {
            return n;
        }
    }

    public static Statement visit(ForStmt n) {
        if (n.getCompare().map(ShortCircuitConditionPipe::isLiteralFalse).orElse(false)) {
            // skip the for loop is the condition is always false
            return null;
        } else {
            return n;
        }
    }

    public static Statement visit(IfStmt n) {
        Expression condition = n.getCondition();
        if (condition.isBooleanLiteralExpr()) {
            if (condition.asBooleanLiteralExpr().getValue()) {
                return n.getThenStmt();
            } else {
                return n.getElseStmt().orElse(null);
            }
        } else {
            return n;
        }
    }

    private static boolean isLiteralFalse(Expression condition) {
        return condition.isBooleanLiteralExpr() && !condition.asBooleanLiteralExpr().getValue();
    }
}
