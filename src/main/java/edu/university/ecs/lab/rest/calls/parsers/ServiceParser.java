package edu.university.ecs.lab.rest.calls.parsers;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import edu.university.ecs.lab.common.models.rest.RestService;
import edu.university.ecs.lab.rest.calls.utils.StringParserUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Class for parsing REST services from source files */
public class ServiceParser {
  /**
   * Parse the REST services from the given source file.
   *
   * @param sourceFile the source file to parse
   * @return list of parsed services
   * @throws IOException i/o error occurs
   */
  public static List<RestService> parseServices(File sourceFile) throws IOException {
    List<RestService> restServices = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    String packageName = StringParserUtils.findPackage(cu);
    if (packageName == null) {
      return restServices;
    }

    // loop through class declarations (and extract variables + methods)
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      restServices.add(new RestService(RestParser.extractJavaClass(sourceFile, cid)));
    }

    return restServices;
  }
}
