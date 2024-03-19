package edu.university.ecs.lab.common;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.rest.calls.utils.StringParserUtils;
import edu.university.ecs.lab.common.models.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParserService {

  /**
   * Parses method information and creates a Method object representing the method
   *
   * @param n the MethodDeclaration that will be parsed
   */
  public static List<Method> parseMethods(File sourceFile, String restMapping) throws IOException {
    List<Method> methods = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      // loop through methods
      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
        Method method = new Method();
        method.setMethodName(md.getNameAsString());

        // Get params and returnType
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

        method.setParameterList(parameter.toString());
        method.setReturnType(md.getTypeAsString());

        // If class passes a restMapping
        if(!restMapping.isEmpty()) {
          Endpoint endpoint = new Endpoint(method);
          // loop through annotations
          for (AnnotationExpr ae : md.getAnnotations()) {
            endpoint.setUrl(StringParserUtils.mergePaths(restMapping, pathFromAnnotation(ae)));
            endpoint.setDecorator(ae.getNameAsString());

            switch (ae.getNameAsString()) {
              case "GetMapping":
                endpoint.setHttpMethod("GET");
                break;
              case "PostMapping":
                endpoint.setHttpMethod("POST");
                break;
              case "DeleteMapping":
                endpoint.setHttpMethod("DELETE");
                break;
              case "PutMapping":
                endpoint.setHttpMethod("PUT");
                break;
              case "RequestMapping":
                if (ae.toString().contains("RequestMethod.POST")) {
                  endpoint.setHttpMethod("POST");
                } else if (ae.toString().contains("RequestMethod.DELETE")) {
                  endpoint.setHttpMethod("DELETE");
                } else if (ae.toString().contains("RequestMethod.PUT")) {
                  endpoint.setHttpMethod("PUT");
                } else {
                  endpoint.setHttpMethod("GET");
                }
                break;
            }
          }

          // TODO find cause of this
          if(endpoint.getHttpMethod() == null) {
            methods.add(method);
          } else {
            methods.add(endpoint);
          }
        } else {
          methods.add(method);
        }

      }
    }

    return methods;
  }

  /**
   * Parse a rest call from a MethodCallExpr
   *
   * @param n The method call to parse
   * @param b
   * @return the rest call
   */
  public static List<MethodCall> parseMethodCalls(File sourceFile, boolean isService) throws IOException {
    List<MethodCall> methodCalls = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    String packageName = StringParserUtils.findPackage(cu);

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      String className = cid.getNameAsString();

      // loop through methods
      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
        String parentMethodName = md.getNameAsString();

        // loop through method calls
        for (MethodCallExpr mce : md.findAll(MethodCallExpr.class)) {
          MethodCall methodCall = new MethodCall();
          String methodName = mce.getNameAsString();

          RestCall restCall = RestCall.findByName(methodName);

          // Are we a rest call
          if (!Objects.isNull(restCall) && isService) {
//            Expression scope = mce.getScope().orElse(null);

            // match field access
//            if (isRestTemplateScope(scope, cid)) {
              // construct rest call

            // get http methods for exchange method
            if (restCall.getMethodName().equals("exchange")) {
              restCall.setHttpMethod(getHttpMethodForExchange(mce.getArguments().toString()));
            }

            // find url
            if(parseURL(mce, cid).equals("/users")) {
              System.out.println(methodName + " targets /users");
            }
            restCall.setUrl(parseURL(mce, cid));

            restCall.setParentMethod(parentMethodName);
            restCall.setCalledFieldName(parseCalledFieldName(mce));

            // add to list of restCall
            methodCalls.add(restCall);
//            }
          } else {
            methodCall.setParentMethod(parentMethodName);
            methodCall.setMethodName(methodName);
            methodCall.setCalledFieldName(parseCalledFieldName(mce));
            methodCalls.add(methodCall);
          }
        }
      }
    }

    return methodCalls;
  }


  public static List<Field> parseFields(File sourceFile) throws IOException {
    List<Field> javaFields = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
        for (VariableDeclarator variable : fd.getVariables()) {
          javaFields.add(new Field(variable));
        }
      }
    }

    return javaFields;
  }

  public static JClass parseClass(File sourceFile, ClassRole role) throws IOException {
    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    String packageName = StringParserUtils.findPackage(cu);
    if (packageName == null) {
      return null;
    }


    JClass jClass = null;
    // Loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      jClass = new JClass();
      jClass.setFileName(sourceFile.getAbsolutePath());
      jClass.setClassName(sourceFile.getName().replace(".java", ""));
      jClass.setPackageName(packageName);
      jClass.setRole(role);

      if(role == ClassRole.CONTROLLER) {
        AnnotationExpr aExpr = cid.getAnnotationByName("RequestMapping").orElse(null);
        if (aExpr == null) {
          throw new RuntimeException();
        }

        String classLevelPath = pathFromAnnotation(aExpr);
        jClass.setMethods(parseMethods(sourceFile, classLevelPath));
      } else {
        jClass.setMethods(parseMethods(sourceFile, ""));
      }
      jClass.setMethodCalls(parseMethodCalls(sourceFile, (jClass.getRole() == ClassRole.SERVICE)));
      jClass.setFields(parseFields(sourceFile));


    }

    return jClass;
  }

  /**
   * Get the api path from the given annotation.
   *
   * @param ae the annotation expression
   * @return the path else an empty string if not found or ae was null
   */
  private static String pathFromAnnotation(AnnotationExpr ae) {
    if (ae == null) {
      return "";
    }

    if (ae.isSingleMemberAnnotationExpr()) {
      return StringParserUtils.removeOuterQuotations(
              ae.asSingleMemberAnnotationExpr().getMemberValue().toString());
    }

    if (ae.isNormalAnnotationExpr() && ae.asNormalAnnotationExpr().getPairs().size() > 0) {
      for (MemberValuePair mvp : ae.asNormalAnnotationExpr().getPairs()) {
        if (mvp.getName().toString().equals("path") || mvp.getName().toString().equals("value")) {
          return StringParserUtils.removeOuterQuotations(mvp.getValue().toString());
        }
      }
    }

    return "";
  }

  private static String parseCalledFieldName(MethodCallExpr mce) {
    String returnString = "";
    if(mce.getScope().isPresent()) {
      returnString = mce.getScope().get().toString();
    } else {
      return returnString;
    }

    return returnString;
  }

  /**
   * Find the URL from the given method call expression.
   *
   * @param mce the method call to extract url from
   * @param cid the class or interface to search
   * @return the URL found
   */
  private static String parseURL(MethodCallExpr mce, ClassOrInterfaceDeclaration cid) {
    if (mce.getArguments().isEmpty()) {
      return "";
    }

    Expression exp = mce.getArguments().get(0);

    if (exp.isStringLiteralExpr()) {
      return StringParserUtils.removeOuterQuotations(exp.toString());
    } else if (exp.isFieldAccessExpr()) {
      return parseFieldValue(cid, exp.asFieldAccessExpr().getNameAsString());
    } else if (exp.isNameExpr()) {
      return parseFieldValue(cid, exp.asNameExpr().getNameAsString());
    } else if (exp.isBinaryExpr()) {
      return parseUrlFromBinaryExp(exp.asBinaryExpr());
    }

    return "";
  }

  private static String parseFieldValue(ClassOrInterfaceDeclaration cid, String fieldName) {
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
  private static String parseUrlFromBinaryExp(BinaryExpr exp) {
    Expression left = exp.getLeft();
    Expression right = exp.getRight();

    if (left instanceof BinaryExpr) {
      return parseUrlFromBinaryExp((BinaryExpr) left);
    } else if (left instanceof StringLiteralExpr) {
      return formatURL((StringLiteralExpr) left);
    }

    // Check if right side is a binary expression
    if (right instanceof BinaryExpr) {
      return parseUrlFromBinaryExp((BinaryExpr) right);
    } else if (right instanceof StringLiteralExpr) {
      return formatURL((StringLiteralExpr) right);
    }

    return ""; // URL not found in subtree
  }

  private static String formatURL(StringLiteralExpr stringLiteralExpr) {
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
      str = str.substring(0, str.length() - 1);
    }

    if (str.endsWith("/")) {
      str = str.substring(0, str.length() - 1);
    }

    return str;
  }

  /**
   * Get the HTTP method for the JSF exchange() method call.
   *
   * @param arguments the arguments of the exchange() method
   * @return the HTTP method extracted
   */
  private static String getHttpMethodForExchange(String arguments) {
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

}
