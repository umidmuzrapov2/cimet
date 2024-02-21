package edu.university.ecs.lab.intermediate.services;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import edu.university.ecs.lab.common.models.Dependency;
import edu.university.ecs.lab.common.models.RestCallMethod;
import edu.university.ecs.lab.intermediate.utils.StringParserUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestDependencyService {
  public List<Dependency> parseDependencies(File sourceFile) throws IOException {
    List<Dependency> dependencies = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    // don't analyse further if no RestTemplate import exists
    if (!hasRestTemplateImport(cu)) {
      return dependencies;
    }

    String packageName = StringParserUtils.findPackage(cu);

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      String className = cid.getNameAsString();

      // loop through methods
      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
        String methodName = md.getNameAsString();

        // loop through method calls
        for (MethodCallExpr mce : md.findAll(MethodCallExpr.class)) {
          String methodCall = mce.getNameAsString();

          RestCallMethod restTemplateMethod = RestCallMethod.findByName(methodCall);

          if (restTemplateMethod != null) {
            Expression scope = mce.getScope().orElse(null);

            // match field access
            if (isRestTemplateScope(scope, cid)) {
              // construct rest call
              Dependency dependency = new Dependency();
              dependency.setSourceFile(sourceFile.getCanonicalPath());
              dependency.setParentMethod(packageName + "." + className + "." + methodName);
              dependency.setHttpMethod(restTemplateMethod.getHttpMethod().toString());

              // get http methods for exchange method
              if (restTemplateMethod.getMethodName().equals("exchange")) {
                dependency.setHttpMethod(getHttpMethodForExchange(mce.getArguments().toString()));
              }

              // find url
              dependency.setUrl(findUrl(mce, cid));

              // add to list of restCall
              dependencies.add(dependency);
            }
          }
        }
      }
    }

    return dependencies;
  }

  private String getHttpMethodForExchange(String arguments) {
    if (arguments.contains("HttpMethod.POST")) {
      return "POST";
    } else if (arguments.contains("HttpMethod.PUT")) {
      return "PUT";
    } else if (arguments.contains("HttpMethod.DELETE")) {
      return "DELETE";
    } else {
      return "GET"; // default
    }
  }

  private String findUrl(MethodCallExpr mce, ClassOrInterfaceDeclaration cid) {
    if (mce.getArguments().size() == 0) {
      return "";
    }

    Expression exp = mce.getArguments().get(0);

    if (exp.isStringLiteralExpr()) {
      return StringParserUtils.removeEnclosedQuotations(exp.toString());
    } else if (exp.isFieldAccessExpr()) {
      return fieldValue(cid, exp.asFieldAccessExpr().getNameAsString());
    } else if (exp.isNameExpr()) {
      return fieldValue(cid, exp.asNameExpr().getNameAsString());
    } else if (exp.isBinaryExpr()) {
      return resolveUrlFromBinaryExp(exp.asBinaryExpr());
    }

    return "";
  }

  private boolean hasRestTemplateImport(CompilationUnit cu) {
    for (ImportDeclaration id : cu.findAll(ImportDeclaration.class)) {
      if (id.getNameAsString().equals("org.springframework.web.client.RestTemplate")) {
        return true;
      }
    }
    return false;
  }

  private boolean isRestTemplateScope(Expression scope, ClassOrInterfaceDeclaration cid) {
    if (scope == null) {
      return false;
    }

    // field access: this.restTemplate
    if (scope.isFieldAccessExpr() && isRestTemplateField(cid, scope.asFieldAccessExpr().getNameAsString())) {
      return true;
    }

    // filed access without this
    if (scope.isNameExpr() && isRestTemplateField(cid, scope.asNameExpr().getNameAsString())) {
      return true;
    }

    return false;
  }

  private boolean isRestTemplateField(ClassOrInterfaceDeclaration cid, String fieldName) {
    for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
      if (fd.getElementType().toString().equals("RestTemplate") &&
              fd.getVariables().toString().contains(fieldName)) {

        return true;
      }
    }
    return false;
  }

  private String fieldValue(ClassOrInterfaceDeclaration cid, String fieldName) {
    for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
      if (fd.getVariables().toString().contains(fieldName)) {
        Expression init = fd.getVariable(0).getInitializer().orElse(null);
        if (init != null) {
          return StringParserUtils.removeEnclosedQuotations(init.toString());
        }
      }
    }
    return "";
  }

  // TODO: kind of resolved, probably not every case considered
  private String resolveUrlFromBinaryExp(BinaryExpr exp) {
    String url = "";

    String right = exp.getRight().toString();
    String left = exp.getLeft().toString();

    if (right.contains("/")) {
      url = right.substring(right.indexOf('/'));

      if (url.endsWith("\"")) {
        url = url.substring(0, url.length()-1);
      }
    }

    if (left.contains("/")) {
      url += left.substring(left.indexOf('/'));

      if (url.endsWith("\"")) {
        url = url.substring(0, url.length()-1);
      }
    }

    return StringParserUtils.removeEnclosedQuotations(url);
  }
}
