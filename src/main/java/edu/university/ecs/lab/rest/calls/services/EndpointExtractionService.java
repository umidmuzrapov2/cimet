package edu.university.ecs.lab.rest.calls.services;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import edu.university.ecs.lab.common.models.JavaMethod;
import edu.university.ecs.lab.rest.calls.models.RestEndpoint;
import edu.university.ecs.lab.rest.calls.models.RestService;
import edu.university.ecs.lab.rest.calls.utils.StringParserUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing REST endpoints from source files and describing them in relation to their
 * relative microservice.
 */
public class EndpointExtractionService {
  /**
   * Parse the REST endpoints from the given source file.
   *
   * @param sourceFile the source file to parse
   * @return the list of parsed endpoints
   * @throws IOException if an I/O error occurs
   */
  public List<RestEndpoint> parseEndpoints(File sourceFile) throws IOException {
    List<RestEndpoint> restEndpoints = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    String packageName = StringParserUtils.findPackage(cu);
    if (packageName == null) {
      return restEndpoints;
    }

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      String className = cid.getNameAsString();

      List<String> services = new ArrayList<>();

      // find service variables
      for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
        if (fd.getElementType().isClassOrInterfaceType()) {
          ClassOrInterfaceType type = fd.getElementType().asClassOrInterfaceType();

          if (type.getNameAsString().contains("Service")) {
            services.add(type.getNameAsString());
          }
        }
      }

      AnnotationExpr aExpr = cid.getAnnotationByName("RequestMapping").orElse(null);
      if (aExpr == null) {
        return restEndpoints;
      }

      String classLevelPath = pathFromAnnotation(aExpr);

      // loop through methods
      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
        JavaMethod method = extractJavaMethod(md);

        // loop through annotations
        for (AnnotationExpr ae : md.getAnnotations()) {
          RestEndpoint restEndpoint = new RestEndpoint();
          restEndpoint.setDecorator(ae.getNameAsString());

          switch (ae.getNameAsString()) {
            case "GetMapping":
              restEndpoint.setHttpMethod("GET");
              break;
            case "PostMapping":
              restEndpoint.setHttpMethod("POST");
              break;
            case "DeleteMapping":
              restEndpoint.setHttpMethod("DELETE");
              break;
            case "PutMapping":
              restEndpoint.setHttpMethod("PUT");
              break;
            case "RequestMapping":
              if (ae.toString().contains("RequestMethod.POST")) {
                restEndpoint.setHttpMethod("POST");
              } else if (ae.toString().contains("RequestMethod.DELETE")) {
                restEndpoint.setHttpMethod("DELETE");
              } else if (ae.toString().contains("RequestMethod.PUT")) {
                restEndpoint.setHttpMethod("PUT");
              } else {
                restEndpoint.setHttpMethod("GET");
              }
              break;
          }

          if (restEndpoint.getHttpMethod() == null) {
            continue;
          }

          restEndpoint.setSourceFile(sourceFile.getCanonicalPath());
          restEndpoint.setUrl(StringParserUtils.mergePaths(classLevelPath, pathFromAnnotation(ae)));
          restEndpoint.setParentMethod(packageName + "." + className + "." + method.getMethodName());
          restEndpoint.setClassName(className);
          restEndpoint.setMethod(method);
          restEndpoint.setServices(services);
          restEndpoints.add(restEndpoint);
        }
      }
    }

    return restEndpoints;
  }

  /**
   * Parse the REST endpoints from the given source file.
   *
   * @param sourceFile the source file to parse
   * @return list of parsed services
   * @throws IOException i/o error occurs
   */
  public List<RestService> parseServices(File sourceFile) throws IOException {
    List<RestService> restServices = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    String packageName = StringParserUtils.findPackage(cu);
    if (packageName == null) {
      return restServices;
    }

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      RestService restService = new RestService();
      restService.setClassName(cid.getNameAsString());
      restService.setSourceFile(sourceFile.getCanonicalPath());

      List<String> dtos = new ArrayList<>();

      // find dto variables
      for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
        if (fd.getElementType().isClassOrInterfaceType()) {
          ClassOrInterfaceType type = fd.getElementType().asClassOrInterfaceType();

          if (type.getNameAsString().toLowerCase().contains("dto")) {
            dtos.add(type.getNameAsString());
          }
        }
      }

      // loop through methods
      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
        restService.addMethod(extractJavaMethod(md));
      }

      restService.setDtos(dtos);
      restServices.add(restService);
    }

    return restServices;
  }

  /**
   * Get the java method from the given declaration
   *
   * @param md method declaration
   * @return method information
   */
  private JavaMethod extractJavaMethod(MethodDeclaration md) {
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

  /**
   * Get the api path from the given annotation.
   *
   * @param ae the annotation expression
   * @return the path else an empty string if not found or ae was null
   */
  private String pathFromAnnotation(AnnotationExpr ae) {
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
}
