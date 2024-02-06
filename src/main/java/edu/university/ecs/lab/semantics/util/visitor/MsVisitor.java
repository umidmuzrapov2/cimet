package edu.university.ecs.lab.semantics.util.visitor;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.university.ecs.lab.semantics.entity.graph.*;
import edu.university.ecs.lab.semantics.util.MsCache;
import edu.university.ecs.lab.semantics.util.constructs.MsMethodBuilder;
import edu.university.ecs.lab.semantics.util.factory.MsRestCallFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core logic class using java parser libraries to pick apart respective classes, methods, method
 * calls, fields, etc and adding them to cache
 *
 * <p>These methods visitClass, visitField, visitMethod, visitMethodCalls form the contents of the
 * respect raw outputfiles msClassList.json, msFieldList.json, etc
 *
 * <p>This class heavily relies on the use of JavaParser,
 *
 * @see <a
 *     href="https://www.javadoc.io/doc/com.github.javaparser/javaparser-core/latest/index.html">JavaParser
 *     Documentation</a>
 */
public class MsVisitor {

  /**
   * Parse class or interface information from file and generate MsClass object that will be saved
   * to cache
   *
   * @param file the file object
   * @param path the file path
   * @param role the role of the file (repository, service, controller)
   * @param msId the msId object associated with the file/path
   */
  public static void visitClass(File file, String path, MsClassRoles role, MsId msId) {
    try {
      new VoidVisitorAdapter<Object>() {
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {
          super.visit(n, arg);
          MsClass msClass = new MsClass();
          msClass.setClassName(n.getNameAsString());

          Optional<Node> parentNode = n.getParentNode();
          if (parentNode.isPresent()) {

            if (parentNode.get() instanceof CompilationUnit) {
              CompilationUnit cu = (CompilationUnit) parentNode.get();
              Optional<PackageDeclaration> pd = cu.getPackageDeclaration();
              pd.ifPresent(
                  packageDeclaration ->
                      msClass.setPackageName(packageDeclaration.getNameAsString()));
            }

            if (parentNode.get() instanceof ClassOrInterfaceDeclaration) {
              ClassOrInterfaceDeclaration c = (ClassOrInterfaceDeclaration) n.getParentNode().get();
              CompilationUnit cu = (CompilationUnit) c.getParentNode().get();
              Optional<PackageDeclaration> pd = cu.getPackageDeclaration();
              pd.ifPresent(
                  packageDeclaration ->
                      msClass.setPackageName(packageDeclaration.getNameAsString()));
            }
          }
          NodeList<AnnotationExpr> nl = n.getAnnotations();
          msClass.setRole(role);
          for (AnnotationExpr annotationExpr : nl) {
            if (annotationExpr.getNameAsString().equals("Service")) {
              msClass.setRole(MsClassRoles.SERVICE);
            }
            if (annotationExpr.getNameAsString().equals("RestController")) {
              msClass.setRole(MsClassRoles.CONTROLLER);
              // get annotation request mapping and value
            }
            if (annotationExpr.getNameAsString().equals("Repository")) {
              msClass.setRole(MsClassRoles.REPOSITORY);
            }
          }
          if (nl.size() == 0 && n.getNameAsString().contains("Service")) {
            msClass.setRole(MsClassRoles.SERVICE_INTERFACE);
          }

          msClass.setIds();
          msClass.setMsId(msId);
          MsCache.addMsClass(msClass);
        }
      }.visit(StaticJavaParser.parse(file), null);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parse method information and hand off information to MsMethodBuilder
   *
   * @param file the file object
   * @param path the file path
   * @param role the role of the file (repository, service, controller)
   * @param msId the msId object associated with the file/path
   */
  public static void visitMethods(File file, MsClassRoles role, String path, MsId msId) {
    try {
      new VoidVisitorAdapter<Object>() {
        @Override
        public void visit(MethodDeclaration n, Object arg) {
          super.visit(n, arg);
          MsMethodBuilder.buildMsMethod(n, role, path, msId);
        }
      }.visit(StaticJavaParser.parse(file), null);
      // System.out.println(); // empty line
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parse method call information and generate MsMethodCall object that will be saved to cache.
   * Goes down the flow to add all calls if they traverse multiple layers of the program
   *
   * @param file the file object
   * @param path the file path
   * @param msId the msId object associated with the file/path
   */
  public static void visitMethodCalls(File file, String path, MsId msId) {
    try {
      Map<String, ArrayList<MsRestCall>> restCallsContainingMethods = new HashMap<>();
      List<String> usedServiceMethods = new ArrayList<>();

      Map<String, ArrayList<MsMethodCall>> repositoryCallsContainingMethods = new HashMap<>();

      new VoidVisitorAdapter<Object>() {
        @Override
        public void visit(MethodCallExpr n, Object arg) {
          super.visit(n, arg);

          Optional<Expression> scope = n.getScope();
          if (scope.isPresent()) {
            if (scope.get() instanceof NameExpr) {
              // get common properties
              int lineNumber = n.getBegin().get().line;
              // decide between service / restTemplate
              NameExpr fae = scope.get().asNameExpr();
              String name = fae.getNameAsString();

              if (name.toLowerCase().contains("repository")) {
                MsMethodCall msMethodCall = new MsMethodCall();

                msMethodCall.setLineNumber(lineNumber);
                msMethodCall.setStatementDeclaration(n.toString());
                msMethodCall.setMsParentMethod(MsParentVisitor.getMsParentMethod(n));
                msMethodCall.setCalledServiceId(name);
                MethodCallExpr methodCallExpr = (MethodCallExpr) fae.getParentNode().get();
                msMethodCall.setCalledMethodName(methodCallExpr.getNameAsString());
                msMethodCall.setParentClassId();
                msMethodCall.setMsId(msId);
                // register method call to cache
                MsCache.addMsMethodCall(msMethodCall);

                String parentMethodFullName = msMethodCall.getParentMethodFullName();
                if (!repositoryCallsContainingMethods.containsKey(parentMethodFullName)) {
                  repositoryCallsContainingMethods.put(
                      parentMethodFullName, new ArrayList<MsMethodCall>());
                }
                repositoryCallsContainingMethods.get(parentMethodFullName).add(msMethodCall);
              }
              if (name.toLowerCase().contains("service")) {
                // service is being called
                MsMethodCall msMethodCall = new MsMethodCall();

                msMethodCall.setLineNumber(lineNumber);
                msMethodCall.setStatementDeclaration(n.toString());
                msMethodCall.setMsParentMethod(MsParentVisitor.getMsParentMethod(n));
                msMethodCall.setCalledServiceId(name);
                MethodCallExpr methodCallExpr = (MethodCallExpr) fae.getParentNode().get();
                msMethodCall.setCalledMethodName(methodCallExpr.getNameAsString());
                msMethodCall.setParentClassId();
                msMethodCall.setMsId(msId);
                // register method call to cache
                MsCache.addMsMethodCall(msMethodCall);

                usedServiceMethods.add(msMethodCall.getParentMethodFullName());

              } else if (name.equals("restTemplate")) {
                // rest template is being called
                MsRestCall msRestCall = MsRestCallFactory.getMsRestCall(n);
                msRestCall.setLineNumber(lineNumber);
                MsParentMethod parentMethodCall = MsParentVisitor.getMsParentMethod(n);
                msRestCall.setMsParentMethod(parentMethodCall);
                msRestCall.setParentClassId();
                msRestCall.setMsId(msId);
                MsCache.addMsRestMethodCall(msRestCall);

                String parentMethodFullName = msRestCall.getParentMethodFullName();
                if (!restCallsContainingMethods.containsKey(parentMethodFullName)) {
                  restCallsContainingMethods.put(parentMethodFullName, new ArrayList<MsRestCall>());
                }
                restCallsContainingMethods.get(parentMethodFullName).add(msRestCall);
              }
            }
          }
        }
      }.visit(StaticJavaParser.parse(file), null);

      /// For handling the one level inter-method calls to RestCalls.
      new VoidVisitorAdapter<Object>() {
        @Override
        public void visit(MethodCallExpr n, Object arg) {
          super.visit(n, arg);
          String calledMethodName = n.getNameAsString();
          MsParentMethod updatedParentMethod = MsParentVisitor.getMsParentMethod(n);

          String calledMethodFullName =
              updatedParentMethod.getParentPackageName()
                  + "."
                  + updatedParentMethod.getParentClassName()
                  + "."
                  + calledMethodName;

          Optional<Expression> scope = n.getScope();

          if (!(scope.isPresent() && scope.get() instanceof NameExpr)) {
            if (restCallsContainingMethods.containsKey(calledMethodFullName)
                && !usedServiceMethods.contains(calledMethodFullName)) {
              ArrayList<MsRestCall> restCalls =
                  restCallsContainingMethods.get(calledMethodFullName);
              for (MsRestCall restCall : restCalls) {
                MsRestCall newRestCall =
                    new MsRestCall(
                        restCall.getApi(), restCall.getHttpMethod(), restCall.getReturnType());
                newRestCall.setMsParentMethod(updatedParentMethod);
                newRestCall.setParentClassId();
                newRestCall.setMsId(restCall.getMsId());
                newRestCall.setLineNumber(restCall.getLineNumber());
                newRestCall.setCalledMethodName(restCall.getCalledMethodName());
                newRestCall.setCalledServiceId(restCall.getCalledServiceId());
                newRestCall.setStatementDeclaration(restCall.getStatementDeclaration());

                MsCache.addMsRestMethodCall(newRestCall);
              }
            }

            if (repositoryCallsContainingMethods.containsKey(calledMethodFullName)
                && !usedServiceMethods.contains(calledMethodFullName)) {
              ArrayList<MsMethodCall> repositoryCalls =
                  repositoryCallsContainingMethods.get(calledMethodFullName);
              for (MsMethodCall repositoryCall : repositoryCalls) {

                MsMethodCall newRepositoryCall = new MsMethodCall();

                newRepositoryCall.setLineNumber(repositoryCall.getLineNumber());
                newRepositoryCall.setStatementDeclaration(n.toString());
                newRepositoryCall.setMsParentMethod(updatedParentMethod);
                newRepositoryCall.setCalledServiceId(repositoryCall.getCalledServiceId());
                //                                MethodCallExpr methodCallExpr = (MethodCallExpr)
                // fae.getParentNode().get();
                //
                // newRepositoryCall.setCalledMethodName(methodCallExpr.getNameAsString());
                newRepositoryCall.setCalledMethodName(repositoryCall.getCalledMethodName());
                newRepositoryCall.setParentClassId();
                newRepositoryCall.setMsId(repositoryCall.getMsId());
                // register method call to cache
                MsCache.addMsMethodCall(newRepositoryCall);
              }
            }
          }
        }
      }.visit(StaticJavaParser.parse(file), null);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parse field information and hand off information to MsFieldVisitor
   *
   * @param file the file object
   * @param path the file path
   * @param msId the msId object associated with the file/path
   */
  public static void visitFields(File file, String path, MsId msId) {
    try {
      new VoidVisitorAdapter<Object>() {
        @Override
        public void visit(FieldDeclaration n, Object arg) {
          super.visit(n, arg);
          MsFieldVisitor.visitFieldDeclaration(n, path, msId);
        }
      }.visit(StaticJavaParser.parse(file), null);
      // System.out.println(); // empty line
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
