package edu.university.ecs.lab.semantics.services;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.university.ecs.lab.semantics.models.*;
import edu.university.ecs.lab.semantics.models.enums.ClassRole;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static edu.university.ecs.lab.semantics.services.ParserService.parseMethodLocation;

/**
 * Service for processing files and storing information relevant to one microservice system.
 * Therefore, a new visitor service should be invoked for each system.
 */
@AllArgsConstructor
public class VisitorService {
  /** Represents the name of the scanned microservice system */
  private final String msName;

  private final File root;

  /**
   * Function for visiting a particular file and updating Cache for that one file
   *
   * <p>TODO: This file should only be used if we know the name of the service e.g. in the context
   * of an update
   *
   * @param currFile the file to visit
   * @throws IllegalAccessException if the file is a directory
   */
  public void processFile(File currFile) throws IllegalArgumentException {
    if (currFile.isDirectory()) {
      throw new IllegalArgumentException("Provided file cannot be a directory");
    }
    fileHandle(currFile);
  }

  /**
   * Function for visiting an entire directory and updating Cache for all files.
   *
   * <p>Assumption -- At this point the called directory should have one microservice below. This
   * function does not break a folder with multiple services down into its respective services.
   *
   * @throws IllegalArgumentException if the file is a not a directory
   */
  public void processRoot() throws IllegalArgumentException {
    if (!root.isDirectory()) {
      throw new IllegalArgumentException("Provided file must be a directory");
    }
    fileFilter(root, root.getName());
  }

  /**
   * Function for recursively filtering children files if currFile is a directory or handling
   * (visiting) a file if it is not a directory
   *
   * @param currFile the current file or directory to filter recursively
   */
  private void fileFilter(File currFile, String msName) {
    if (currFile.isDirectory()) {
      for (File child : currFile.listFiles()) {
        fileFilter(new File(currFile, child.getName()), msName);
      }
    } else {
      if (currFile.getPath().endsWith(".java") && !currFile.getName().contains("Test")) {
        fileHandle(currFile);
      }
    }
  }

  /**
   * Handling a file means visiting all aspects of the file via the visit functions and filling
   * cache with information
   *
   * @param file the file to handle
   */
  private void fileHandle(File file) {
    String path = file.getName();
    ClassRole role = null;
    if (path.contains("Controller")) {
      role = ClassRole.CONTROLLER;
    }
    if (path.contains("Service")) {
      role = ClassRole.SERVICE;
    }
    if (path.contains("Repository")) {
      role = ClassRole.REPOSITORY;
    }

    if (role != null) {
      if (role.equals(ClassRole.CONTROLLER) || role.equals(ClassRole.SERVICE)) {
        visitClasses(file, role);
        visitMethods(file, role);
        visitMethodCalls(file);
        visitFields(file, path);

      } else if (role.equals(ClassRole.REPOSITORY)) {
        visitClasses(file, role);
        visitMethods(file, role);
      }
    }
  }

  public void visitClasses(File file, ClassRole role) {
    try {
      new VoidVisitorAdapter<Object>() {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {
          super.visit(n, arg);

          JClass jclass = new JClass();
          jclass.setClassName(n.getNameAsString());
          Id id = new Id();
          id.setProject(msName);
          id.setLocation(file.getAbsolutePath());

          Optional<Node> parentNode = n.getParentNode();
          if (parentNode.isPresent()) {
            if (parentNode.get() instanceof CompilationUnit) {
              CompilationUnit cu = (CompilationUnit) parentNode.get();
              Optional<PackageDeclaration> pd = cu.getPackageDeclaration();
              pd.ifPresent(
                  packageDeclaration ->
                      jclass.setPackageName(packageDeclaration.getNameAsString()));
            } else if (parentNode.get() instanceof ClassOrInterfaceDeclaration) {
              ClassOrInterfaceDeclaration c = (ClassOrInterfaceDeclaration) n.getParentNode().get();
              CompilationUnit cu = (CompilationUnit) c.getParentNode().get();
              Optional<PackageDeclaration> pd = cu.getPackageDeclaration();
              pd.ifPresent(
                  packageDeclaration ->
                      jclass.setPackageName(packageDeclaration.getNameAsString()));
            }
          }
          NodeList<AnnotationExpr> nl = n.getAnnotations();
          jclass.setAnnotations(ParserService.parseAnnotations(nl));
          jclass.setRole(role);
          for (AnnotationExpr annotationExpr : nl) {
            if (annotationExpr.getNameAsString().equals("Service")) {
              jclass.setRole(ClassRole.SERVICE);
            } else if (annotationExpr.getNameAsString().equals("RestController")) {
              jclass.setRole(ClassRole.CONTROLLER);
              // get annotation request mapping and value
            } else if (annotationExpr.getNameAsString().equals("Repository")) {
              jclass.setRole(ClassRole.REPOSITORY);
            }
          }
          if (nl.size() == 0 && n.getNameAsString().contains("Service")) {
            jclass.setRole(ClassRole.SERVICE_INTERFACE);
          }

          //                    msClass.setIds();
          jclass.setId(id);
          CachingService.getCache().getClassList().add(jclass);
        }
      }.visit(StaticJavaParser.parse(file), null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitMethods(File file, ClassRole role) {
    try {
      new VoidVisitorAdapter<Object>() {
        @Override
        public void visit(MethodDeclaration n, Object arg) {
          super.visit(n, arg);
          Method m = ParserService.parseMethod(n, role);
          m.setId(new Id(msName, file.getAbsolutePath()));
          CachingService.getCache().getMethodList().add(m);
        }
      }.visit(StaticJavaParser.parse(file), null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitMethodCalls(File file) {
    try {
      Map<String, ArrayList<RestCall>> methodsContainingRestCalls = new HashMap<>();
      List<String> usedServiceMethods = new ArrayList<>();

      Map<String, ArrayList<MethodCall>> repositoryCallsContainingMethods = new HashMap<>();

      new VoidVisitorAdapter<Object>() {
        @Override
        public void visit(MethodCallExpr n, Object arg) {
          super.visit(n, arg);

          Optional<Expression> scope = n.getScope();

          if (scope.isPresent()) {
            if (scope.get() instanceof NameExpr) {
              Id id = new Id();
              id.setLocation(file.getAbsolutePath());
              id.setProject(msName);

              // TODO isPresent()
              int lineNumber = n.getBegin().get().line;
              NameExpr fae = scope.get().asNameExpr();
              String name = fae.getNameAsString();

              if (name.toLowerCase().contains("repository")) {
                MethodCall methodCall = new MethodCall();

                methodCall.setLineNumber(lineNumber);
                //                                methodCall.setStatementDeclaration(n.toString());
                methodCall.setMethodLocation(parseMethodLocation(n));
                methodCall.setCalledServiceId(name);
                MethodCallExpr methodCallExpr = (MethodCallExpr) fae.getParentNode().get();
                methodCall.setCalledMethodName(methodCallExpr.getNameAsString());
                methodCall.setId(id);
                // register method call to cache
                CachingService.getCache().getMethodCallList().add(methodCall);
              }
              if (name.toLowerCase().contains("service")) {
                // service is being called
                MethodCall methodCall = new MethodCall();

                methodCall.setLineNumber(lineNumber);
                //                                methodCall.setStatementDeclaration(n.toString());
                methodCall.setMethodLocation(parseMethodLocation(n));
                methodCall.setCalledServiceId(name);
                MethodCallExpr methodCallExpr = (MethodCallExpr) fae.getParentNode().get();
                methodCall.setCalledMethodName(methodCallExpr.getNameAsString());
                methodCall.setId(id);
                // register method call to cache
                CachingService.getCache().getMethodCallList().add(methodCall);
              } else if (name.equals("restTemplate")) {
                // restTemplate.<insertMethodName>() being called
                RestCall msRestCall = ParserService.parseRestCall(n);
                msRestCall.setLineNumber(lineNumber);
                MethodLocation methodLocationCall = parseMethodLocation(n);
                msRestCall.setMethodLocation(methodLocationCall);
                msRestCall.setId(id);
                CachingService.getCache().getRestCallList().add(msRestCall);
              }
            }
          }
        }
      }.visit(StaticJavaParser.parse(file), null);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void visitFields(File file, String msName) {
    try {
      new VoidVisitorAdapter<Object>() {
        @Override
        public void visit(FieldDeclaration n, Object arg) {
          super.visit(n, arg);

          Id id = new Id();
          id.setProject(msName);
          id.setLocation(file.getAbsolutePath());
          Optional<Field> f = ParserService.parseField(n);
          if (f.isPresent()) {
            f.get().setId(id);
            CachingService.getCache().getFieldList().add(f.get());
          }
        }
      }.visit(StaticJavaParser.parse(file), null);
      // System.out.println(); // empty line
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
