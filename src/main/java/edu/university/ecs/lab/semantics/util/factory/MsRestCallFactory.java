package edu.university.ecs.lab.semantics.util.factory;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.metamodel.BinaryExprMetaModel;

import edu.university.ecs.lab.semantics.entity.graph.MsRestCall;

import java.util.Optional;

/** A utility class for finding a MsRestCall associated with a MethodCallExpr */
public class MsRestCallFactory {

  /**
   * Finds the MsRestCall associated with a MethodCallExpr
   *
   * @param n the MethodCallExpr being searched
   * @return the MsRestCall associated with n
   */
  public static MsRestCall getMsRestCall(MethodCallExpr n) {
    // ms cache add MsRestCall
    MsRestCall msRestCall = new MsRestCall();
    msRestCall.setStatementDeclaration(n.toString());
    // here try to print the n
    NodeList<Expression> expressionNodeList = n.getArguments();
    expressionNodeList.forEach(
        e -> {
          if (e instanceof StringLiteralExpr) {
            StringLiteralExpr se = (StringLiteralExpr) e;
            msRestCall.setApi(se.toString());
          }
          if (e instanceof BinaryExpr) {
            BinaryExpr be = (BinaryExpr) e;
            BinaryExprMetaModel me = be.getMetaModel();
            // the URL
            msRestCall.setApi(be.toString());
          }
          if (e instanceof FieldAccessExpr) {
            FieldAccessExpr f = (FieldAccessExpr) e;
            // GET, POST, etc.
            msRestCall.setHttpMethod(f.getName().toString());
          }
          if (e instanceof NameExpr) {
            NameExpr ne = (NameExpr) e;
            msRestCall.setReturnType(ne.toString());
          }
          if (e instanceof ObjectCreationExpr) {
            ObjectCreationExpr oce = (ObjectCreationExpr) e;
            ClassOrInterfaceType paramType = oce.getType();
            Optional<NodeList<Type>> optParamTypes = paramType.getTypeArguments();
            optParamTypes.ifPresent(
                types ->
                    types.forEach(
                        p -> {
                          if (p instanceof ClassOrInterfaceType) {
                            ClassOrInterfaceType tp = (ClassOrInterfaceType) p;
                            if (tp.getTypeArguments().isPresent()) {
                              tp.getTypeArguments()
                                  .get()
                                  .forEach(
                                      ta -> {
                                        // return type
                                        msRestCall.setReturnType(ta.toString());
                                      });
                            }
                          }
                        }));
          }
        });
    return msRestCall;
  }
}
