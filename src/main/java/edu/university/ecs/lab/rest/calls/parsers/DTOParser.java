package edu.university.ecs.lab.rest.calls.parsers;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import edu.university.ecs.lab.common.models.JavaVariable;
import edu.university.ecs.lab.rest.calls.models.RestDTO;
import edu.university.ecs.lab.rest.calls.utils.StringParserUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for parsing REST dtos from source files
 */
public class DTOParser {
  /**
   * Parse the REST dtos from the given source file
   *
   * @param sourceFile the source file to parse
   * @return list of parsed dtos
   * @throws IOException i/o error occurs
   */
  public static List<RestDTO> parseDTOs(File sourceFile) throws IOException {
    List<RestDTO> dtos = new ArrayList<>();

    CompilationUnit cu = StaticJavaParser.parse(sourceFile);

    String packageName = StringParserUtils.findPackage(cu);
    if (packageName == null) {
      return dtos;
    }

    // loop through class declarations
    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
      dtos.add(new RestDTO(RestParser.extractJavaClass(sourceFile, cid)));
    }

    return dtos;
  }
}
