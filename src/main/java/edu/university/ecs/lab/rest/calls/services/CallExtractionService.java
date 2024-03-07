package edu.university.ecs.lab.rest.calls.services;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import edu.university.ecs.lab.rest.calls.utils.StringParserUtils;
import edu.university.ecs.lab.rest.calls.models.RestCall;
import edu.university.ecs.lab.rest.calls.models.RestCallMethod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing REST calls from source files and describing them in relation to the
 * microservice that calls the endpoint.
 */
public class CallExtractionService {
  /**
   * Parse the REST calls from the given source file.
   *
   * @param sourceFile the source file to parse
   * @return the list of parsed dependencies
   * @throws IOException if an I/O error occurs
   */
  public List<RestCall> parseCalls(File sourceFile) throws IOException {
    List<RestCall> dependencies = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    // don't analyze further if no RestTemplate import exists
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
              RestCall restCall = new RestCall();
              restCall.setSourceFile(sourceFile.getCanonicalPath());
              restCall.setCallMethod(packageName + "." + className + "." + methodName);
              restCall.setCallClass(className);
              restCall.setHttpMethod(restTemplateMethod.getHttpMethod().toString());

              // get http methods for exchange method
              if (restTemplateMethod.getMethodName().equals("exchange")) {
                restCall.setHttpMethod(
                    getHttpMethodForExchange(mce.getArguments().toString()));
              }

              // find url
              restCall.setUrl(findUrl(mce, cid));

              // skip empty urls
              if (restCall.getUrl().equals("")) {
                continue;
              }

              restCall.setDestFile("");

              // add to list of restCall
              dependencies.add(restCall);
            }
          }
        }
      }
    }

    return dependencies;
  }

  /**
   * Get the HTTP method for the JSF exchange() method call.
   *
   * @param arguments the arguments of the exchange() method
   * @return the HTTP method extracted
   */
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

  /**
   * Find the URL from the given method call expression.
   *
   * @param mce the method call to extract url from
   * @param cid the class or interface to search
   * @return the URL found
   */
  private String findUrl(MethodCallExpr mce, ClassOrInterfaceDeclaration cid) {
    if (mce.getArguments().isEmpty()) {
      return "";
    }

    Expression exp = mce.getArguments().get(0);

    if (exp.isStringLiteralExpr()) {
      return StringParserUtils.removeOuterQuotations(exp.toString());
    } else if (exp.isFieldAccessExpr()) {
      return fieldValue(cid, exp.asFieldAccessExpr().getNameAsString());
    } else if (exp.isNameExpr()) {
      return fieldValue(cid, exp.asNameExpr().getNameAsString());
    } else if (exp.isBinaryExpr()) {
      return resolveUrlFromBinaryExp(exp.asBinaryExpr());
    }

    return "";
  }

  /**
   * Check if the given compilation unit has a RestTemplate import in order to determine if it would
   * have any dependencies in the file.
   *
   * @param cu the compilation unit to check
   * @return if a RestTemplate import exists else false
   */
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
    if (scope.isFieldAccessExpr()
        && isRestTemplateField(cid, scope.asFieldAccessExpr().getNameAsString())) {
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
      if (fd.getElementType().toString().equals("RestTemplate")
          && fd.getVariables().toString().contains(fieldName)) {

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
          return StringParserUtils.removeOuterQuotations(init.toString());
        }
      }
    }

    return "";
  }

  // TODO: kind of resolved, probably not every case considered
  private String resolveUrlFromBinaryExp(BinaryExpr exp) {
    Expression left = exp.getLeft();
    Expression right = exp.getRight();

    if (left instanceof BinaryExpr) {
      return resolveUrlFromBinaryExp((BinaryExpr) left);
    } else if (left instanceof StringLiteralExpr) {
      return formatURL((StringLiteralExpr) left);
    }

    // Check if right side is a binary expression
    if (right instanceof BinaryExpr) {
      return resolveUrlFromBinaryExp((BinaryExpr) right);
    } else if (right instanceof StringLiteralExpr) {
      return formatURL((StringLiteralExpr) right);
    }

    return ""; // URL not found in subtree
  }

  private String formatURL(StringLiteralExpr stringLiteralExpr) {
    String str = stringLiteralExpr.toString();
    str = str.replace("http://", "");
    str = str.replace("https://", "");

    int backslashNdx = str.indexOf("/");
    if (backslashNdx > 0) {
      str = str.substring(backslashNdx);
    }

    int questionNdx = str.indexOf("?");
    if (questionNdx > 0) {
      str = str.substring(0, questionNdx);
    }

    if (str.endsWith("\"")) {
      str = str.substring(0, str.length()-1);
    }

    if (str.endsWith("/")) {
      str = str.substring(0, str.length()-1);
    }

    return str;
  }
}
