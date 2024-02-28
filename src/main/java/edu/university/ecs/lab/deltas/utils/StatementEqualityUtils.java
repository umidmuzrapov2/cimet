package edu.university.ecs.lab.deltas.utils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class StatementEqualityUtils {
  public static boolean checkEquality(Statement localStatement, Statement targetStatement) {
    if (localStatement == null || targetStatement == null) {
      return false;
    }

    StatementEqualityVisitor visitor = new StatementEqualityVisitor(targetStatement);
    localStatement.accept(visitor, null);
    return visitor.isEqual();
  }

  private static class StatementEqualityVisitor extends VoidVisitorAdapter<Void> {
    private final Statement targetStatement;
    private boolean equal;

    public StatementEqualityVisitor(Statement targetStatement) {
      this.targetStatement = targetStatement;
      this.equal = false;
    }

    public boolean isEqual() {
      return equal;
    }

    @Override
    public void visit(ExpressionStmt exprStmt, Void arg) {
      if (targetStatement.isExpressionStmt()) {
        equal = exprStmt.getExpression().equals(targetStatement.asExpressionStmt().getExpression());
      }
    }

    @Override
    public void visit(BlockStmt blockStmt, Void arg) {
      if (targetStatement.isBlockStmt()) {
        equal = blockStmt.equals(targetStatement);
      }
    }

    @Override
    public void visit(IfStmt ifStmt, Void arg) {
      if (targetStatement.isIfStmt()) {
        equal = ifStmt.equals(targetStatement);
      }
    }

    @Override
    public void visit(ForStmt forStmt, Void arg) {
      if (targetStatement.isForStmt()) {
        equal = forStmt.equals(targetStatement);
      }
    }
  }
}
