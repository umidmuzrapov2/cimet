package edu.university.ecs.lab.rest.calls.parsers;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import edu.university.ecs.lab.common.models.JavaClass;
import edu.university.ecs.lab.common.models.JavaMethod;
import edu.university.ecs.lab.common.models.JavaVariable;

import java.io.File;
import java.io.IOException;

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

  public static JavaClass extractJavaClass(File sourceFile, ClassOrInterfaceDeclaration cid) throws IOException {
    JavaClass javaClass = new JavaClass();
    javaClass.setClassName(cid.getNameAsString());
    javaClass.setSourceFile(sourceFile.getCanonicalPath());

    // find variables
    for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
      for (VariableDeclarator variableDeclarator : fd.getVariables()) {
        javaClass.addVariable(new JavaVariable(variableDeclarator.getNameAsString(), variableDeclarator.getTypeAsString()));
      }
    }

    // loop through methods
    for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
      javaClass.addMethod(RestParser.extractJavaMethod(md));
    }

    return javaClass;
  }
}
