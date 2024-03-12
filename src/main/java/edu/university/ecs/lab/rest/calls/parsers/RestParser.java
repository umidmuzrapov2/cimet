package edu.university.ecs.lab.rest.calls.parsers;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import edu.university.ecs.lab.common.models.JavaClass;
import edu.university.ecs.lab.common.models.JavaMethod;
import edu.university.ecs.lab.common.models.JavaVariable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestParser {
  /**
   * Get the java method from the given declaration
   *
   * @param md method declaration
   * @return method information
   */
  public static JavaMethod extractJavaMethod(MethodDeclaration md) {
    String methodName = md.getNameAsString();

    NodeList<Parameter> parameterList = md.getParameters();
    StringBuilder parameter = new StringBuilder();
    if (parameterList.size() != 0) {
      parameter = new StringBuilder("[");

      for (int i = 0; i < parameterList.size(); i++) {
        parameter.append(parameterList.get(i).toString());
        if (i != parameterList.size() - 1) {
          parameter.append(", ");
        } else {
          parameter.append("]");
        }
      }
    }

    JavaMethod method = new JavaMethod();
    method.setMethodName(methodName);
    method.setParameter(parameter.toString());
    method.setReturnType(md.getTypeAsString());

    return method;
  }

  public static JavaClass extractJavaClass(File sourceFile, ClassOrInterfaceDeclaration cid)
      throws IOException {
    JavaClass javaClass = new JavaClass();

    javaClass.setClassName(cid.getNameAsString());
    javaClass.setSourceFile(sourceFile.getCanonicalPath());
    javaClass.setVariables(extractVariables(cid));
    javaClass.setMethods(extractMethods(cid));

    return javaClass;
  }

  public static List<JavaVariable> extractVariables(ClassOrInterfaceDeclaration cid) {
    List<JavaVariable> javaVariables = new ArrayList<>();

    // loop through/find variables
    for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
      for (VariableDeclarator variable : fd.getVariables()) {
        javaVariables.add(new JavaVariable(variable.getNameAsString(), variable.getTypeAsString()));
      }
    }

    return javaVariables;
  }

  public static List<JavaVariable> extractVariables(MethodDeclaration md) {
    List<JavaVariable> javaVariables = new ArrayList<>();

    BlockStmt methodBody = md.getBody().orElse(null);
    if (methodBody == null) {
      return javaVariables;
    }

    // loop through/find variables
    for (VariableDeclarationExpr vExpr : methodBody.findAll(VariableDeclarationExpr.class)) {
      if (vExpr.getElementType().isClassOrInterfaceType()) {
        for (VariableDeclarator vd : vExpr.getVariables()) {
          javaVariables.add(new JavaVariable(vd.getNameAsString(), vd.getTypeAsString()));
        }
      }
    }

    return javaVariables;
  }

  public static List<JavaMethod> extractMethods(ClassOrInterfaceDeclaration cid) {
    List<JavaMethod> javaMethods = new ArrayList<>();

    // loop through methods
    for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
      javaMethods.add(RestParser.extractJavaMethod(md));
    }

    return javaMethods;
  }
}
