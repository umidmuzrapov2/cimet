package edu.university.ecs.lab.intermediate.services;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import edu.university.ecs.lab.common.models.Endpoint;
import edu.university.ecs.lab.intermediate.utils.StringParserUtils;

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
  public List<Endpoint> parseEndpoints(File sourceFile) throws IOException {
    List<Endpoint> endpoints = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    String packageName = StringParserUtils.findPackage(cu);
    if (packageName == null) {
      return endpoints;
    }

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      String className = cid.getNameAsString();

      AnnotationExpr aExpr = cid.getAnnotationByName("RequestMapping").orElse(null);
      if (aExpr == null) {
        return endpoints;
      }

      String classLevelPath = pathFromAnnotation(aExpr);

      // loop through methods
      for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {

        String methodName = md.getNameAsString();


        NodeList<Parameter>  parameterList= md.getParameters();
        String parameter = "";
        if(parameterList.size() != 0){
          parameter = "[";

          for(int i = 0; i < parameterList.size(); i++){
            parameter = parameter + parameterList.get(i).toString();
            if(i !=  parameterList.size()-1){
              parameter = parameter + ", ";
            }else{
              parameter = parameter + "]";
            }
          }
        }

        // loop through annotations
        for (AnnotationExpr ae : md.getAnnotations()) {
          Endpoint endpoint = new Endpoint();
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

          if (endpoint.getHttpMethod() == null) {
            continue;
          }

          endpoint.setSourceFile(sourceFile.getCanonicalPath());
          endpoint.setUrl(StringParserUtils.mergePaths(classLevelPath, pathFromAnnotation(ae)));
          endpoint.setParentMethod(packageName + "." + className + "." + methodName);
          endpoint.setMethodName(methodName);
          endpoint.setParameter(parameter);
          endpoint.setReturnType(md.getTypeAsString());
          endpoints.add(endpoint);

        }
      }
    }

    return endpoints;
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
