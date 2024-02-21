package edu.university.ecs.lab.intermediate.utils;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import org.apache.commons.io.FilenameUtils;

public class StringParserUtils {
  public static String removeEnclosedQuotations(String s) {
    if (s != null && s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
      return s.substring(1, s.length() - 1);
    }
    return s;
  }

  public static String mergePaths(String classPath, String methodPath) {
    if (classPath.startsWith("/")) classPath = classPath.substring(1);
    if (methodPath.startsWith("/")) methodPath = methodPath.substring(1);

    String path =
        FilenameUtils.normalizeNoEndSeparator(FilenameUtils.concat(classPath, methodPath), true);
    if (!path.startsWith("/")) path = "/" + path;

    return path;
  }

  public static String findPackage(CompilationUnit cu) {
    for (PackageDeclaration pd : cu.findAll(PackageDeclaration.class)) {
      return pd.getNameAsString();
    }
    return null;
  }
}
