package edu.university.ecs.lab.semantics.services;

import com.github.javaparser.ParseException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import edu.university.ecs.lab.semantics.models.*;
import edu.university.ecs.lab.semantics.models.enums.ClassRole;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class VisitorService {

    /**
     * Function for visiting a particular file and updating Cache for that one file
     *
     * TODO: This file should only be used if we know the name of the service e.g. in the context of an update
     *
     * @param currFile the file to visit
     * @throws IllegalAccessException if the file is a directory
     */
    public static void processFile(File currFile, String msName) throws IllegalAccessException {
        if(currFile.isDirectory()) {
            throw new IllegalAccessException("Provided file cannot be a directory");
        }
        fileHandle(currFile, msName);
    }

    /**
     * Function for visiting an entire directory and updating Cache for all files.
     *
     * Assumption -- At this point the called directory should have one microservice
     * below. This function does not break a folder with multiple services down into
     * its respective services.
     *
     * @param rootDirectory the directory to visit
     * @throws IllegalAccessException if the file is a not a directory
     */
    public static void processRoot(File rootDirectory) throws IllegalAccessException {
        if(!rootDirectory.isDirectory()) {
            throw new IllegalAccessException("Provided file must be a directory");
        }
        fileFilter(rootDirectory, rootDirectory.getName());
    }

    /**
     * Function for recursively filtering children files if currFile is a directory or
     * handling (visiting) a file if it is not a directory
     *
     * @param currFile the current file or directory to filter recursively
     */
    private static void fileFilter(File currFile, String msName) {
        if (currFile.isDirectory()) {
            for (File child : currFile.listFiles()) {
                fileFilter(new File(currFile, child.getName()), msName);
            }
        } else {
            if (currFile.getPath().endsWith(".java") && !currFile.getName().contains("Test")) {
                fileHandle(currFile, msName);
            }
        }
    }

    /**
     * Handling a file means visiting all aspects of the file via the
     * visit functions and filling cache with information
     *
     * @param file the file to handle
     */
    private static void fileHandle(File file, String msName) {
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
                visitClass(file, role, msName);
                visitMethods(file, role, msName);
                visitMethodCalls(file, msName);
                visitFields(file, path);

            } else if (role.equals(ClassRole.REPOSITORY)) {
                visitClass(file, role, msName);
                visitMethods(file, role, msName);
            }
        }

    }



    /**
     * Parse MsParentMethod from MethodCallExpr object
     *
     * @param n the MethodCallExpr that will be parsed
     * @return MsParentMethod object representing parent method
     */
    public static ParentMethod visitParentMethod(MethodCallExpr n) {
        ParentMethod msParentMethod = new ParentMethod();
        Optional<Node> parentNode = n.getParentNode();
        while (parentNode.isPresent() && !(parentNode.get() instanceof MethodDeclaration)) {
            parentNode = parentNode.get().getParentNode();
        }
        if (parentNode.isPresent()) {
            // Set Method
            MethodDeclaration md = (MethodDeclaration) parentNode.get();
            msParentMethod.setParentMethodName(md.getName().getIdentifier());
            // Find Class
            while (parentNode.isPresent() && !(parentNode.get() instanceof ClassOrInterfaceDeclaration)) {
                parentNode = parentNode.get().getParentNode();
            }
            if (parentNode.isPresent()) {
                // Set Class
                ClassOrInterfaceDeclaration cl = (ClassOrInterfaceDeclaration) parentNode.get();
                msParentMethod.setParentClassName(cl.getName().getIdentifier());
                // Find Package
                parentNode = parentNode.get().getParentNode();
                if (parentNode.isPresent() && parentNode.get() instanceof CompilationUnit) {
                    // Set Package
                    CompilationUnit cu = (CompilationUnit) parentNode.get();
                    Optional<PackageDeclaration> pd = cu.getPackageDeclaration();
                    if (pd.isPresent()) {
                        msParentMethod.setParentPackageName(pd.get().getNameAsString());
                    }
                } else {
                    System.err.println("Cannot get the Package Declaration");
                }
            }
        }
        return msParentMethod;
    }

    /**
     * Parse MsParentMethod from FieldDeclaration object
     *
     * @param n the FieldDeclaration that will be parsed
     * @return MsParentMethod object representing parent method
     */
    public static ParentMethod visitParentMethod(FieldDeclaration n) {
        ParentMethod msParentMethod = new ParentMethod();
        Optional<Node> parentNode = n.getParentNode();

        // Find Class
        while (parentNode.isPresent() && !(parentNode.get() instanceof ClassOrInterfaceDeclaration)) {
            parentNode = parentNode.get().getParentNode();
        }
        if (parentNode.isPresent()) {
            // Set Class
            ClassOrInterfaceDeclaration cl = (ClassOrInterfaceDeclaration) parentNode.get();
            msParentMethod.setParentClassName(cl.getName().getIdentifier());
            // Find Package
            parentNode = parentNode.get().getParentNode();
            if (parentNode.isPresent()) {
                // Set Package
                CompilationUnit cu = (CompilationUnit) parentNode.get();
                Optional<PackageDeclaration> pd = cu.getPackageDeclaration();
                if (pd.isPresent()) {
                    msParentMethod.setParentPackageName(pd.get().getNameAsString());
                }
            }
        }

        return msParentMethod;
    }


    public static void visitClass(File file, ClassRole role, String msName) {
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

    public static void visitMethods(File file, ClassRole role, String msName) {
        try {
            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(MethodDeclaration n, Object arg) {
                    super.visit(n, arg);

                    Id id = new Id();
                    id.setProject(msName);
                    ParserService.parseMethod(n, role, id);
                }
            }.visit(StaticJavaParser.parse(file), null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void visitMethodCalls(File file, String msName) {
        try {
            Map<String, ArrayList<RestCall>> restCallsContainingMethods = new HashMap<>();
            List<String> usedServiceMethods = new ArrayList<>();

            Map<String, ArrayList<MethodCall>> repositoryCallsContainingMethods = new HashMap<>();

            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(MethodCallExpr n, Object arg) {
                    super.visit(n, arg);

                    Optional<Expression> scope = n.getScope();
                    if (scope.isPresent()) {
                        if (scope.get() instanceof NameExpr) {

                            int lineNumber = n.getBegin().get().line;
                            NameExpr fae = scope.get().asNameExpr();
                            String name = fae.getNameAsString();

                            if (name.toLowerCase().contains("repository")) {
                                MethodCall methodCall = new MethodCall();

                                methodCall.setLineNumber(lineNumber);
                                methodCall.setStatementDeclaration(n.toString());
                                methodCall.setMsParentMethod(visitParentMethod(n));
                                methodCall.setCalledServiceId(name);
                                MethodCallExpr methodCallExpr = (MethodCallExpr) fae.getParentNode().get();
                                methodCall.setCalledMethodName(methodCallExpr.getNameAsString());
                                methodCall.setParentClassId();
//                                methodCall.setId(id);
                                // register method call to cache
                                CachingService.getCache().getMethodCallList().add(methodCall);

                                String parentMethodFullName = methodCall.getParentMethodFullName();
                                if (!repositoryCallsContainingMethods.containsKey(parentMethodFullName)) {
                                    repositoryCallsContainingMethods.put(
                                            parentMethodFullName, new ArrayList<MethodCall>());
                                }
                                repositoryCallsContainingMethods.get(parentMethodFullName).add(methodCall);
                            }
                            if (name.toLowerCase().contains("service")) {
                                // service is being called
                                MethodCall methodCall = new MethodCall();

                                methodCall.setLineNumber(lineNumber);
                                methodCall.setStatementDeclaration(n.toString());
                                methodCall.setMsParentMethod(visitParentMethod(n));
                                methodCall.setCalledServiceId(name);
                                MethodCallExpr methodCallExpr = (MethodCallExpr) fae.getParentNode().get();
                                methodCall.setCalledMethodName(methodCallExpr.getNameAsString());
                                methodCall.setParentClassId();
//                                methodCall.setMsId(msId);
                                // register method call to cache
                                CachingService.getCache().getMethodCallList().add(methodCall);

                                usedServiceMethods.add(methodCall.getParentMethodFullName());

                            } else if (name.equals("restTemplate")) {
                                // rest template is being called
                                RestCall msRestCall = ParserService.parseRestCall(n);
                                msRestCall.setLineNumber(lineNumber);
                                ParentMethod parentMethodCall = visitParentMethod(n);
                                msRestCall.setMsParentMethod(parentMethodCall);
                                msRestCall.setParentClassId();
//                                msRestCall.setMsId(msId);
                                CachingService.getCache().getRestCallList().add(msRestCall);

                                String parentMethodFullName = msRestCall.getParentMethodFullName();
                                if (!restCallsContainingMethods.containsKey(parentMethodFullName)) {
                                    restCallsContainingMethods.put(parentMethodFullName, new ArrayList<RestCall>());
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
                    ParentMethod updatedParentMethod = visitParentMethod(n);

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
                            ArrayList<RestCall> restCalls =
                                    restCallsContainingMethods.get(calledMethodFullName);
                            for (RestCall restCall : restCalls) {
                                RestCall newRestCall =
                                        new RestCall(
                                                restCall.getApiEndpoint(), restCall.getHttpMethod(), restCall.getReturnType());
                                newRestCall.setMsParentMethod(updatedParentMethod);
                                newRestCall.setParentClassId();
//                                newRestCall.setMsId(restCall.getMsId());
                                newRestCall.setLineNumber(restCall.getLineNumber());
                                newRestCall.setCalledMethodName(restCall.getCalledMethodName());
                                newRestCall.setCalledServiceId(restCall.getCalledServiceId());
                                newRestCall.setStatementDeclaration(restCall.getStatementDeclaration());

                                CachingService.getCache().getRestCallList().add(newRestCall);
                            }
                        }

                        if (repositoryCallsContainingMethods.containsKey(calledMethodFullName)
                                && !usedServiceMethods.contains(calledMethodFullName)) {
                            ArrayList<MethodCall> repositoryCalls =
                                    repositoryCallsContainingMethods.get(calledMethodFullName);
                            for (MethodCall repositoryCall : repositoryCalls) {

                                MethodCall newRepositoryCall = new MethodCall();

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
//                                newRepositoryCall.setMsId(repositoryCall.getMsId());
                                // register method call to cache
                                CachingService.getCache().getMethodCallList().add(newRepositoryCall);
                            }
                        }
                    }
                }
            }.visit(StaticJavaParser.parse(file), null);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void visitFields(File file, String msName) {
        try {
            new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(FieldDeclaration n, Object arg) {
                    super.visit(n, arg);

                    Id id = new Id();
                    id.setProject(msName);

                    visitFieldDeclaration(n, id);
                }
            }.visit(StaticJavaParser.parse(file), null);
            // System.out.println(); // empty line
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void visitFieldDeclaration(FieldDeclaration n, Id id) {
        Field msField = new Field();
        if (n.getVariables().size() > 0) {
            VariableDeclarator vd = n.getVariables().get(0);
            String variableName = vd.getNameAsString();
            if (variableName.toLowerCase().contains("service") || variableName.toLowerCase().contains("repository")) {
                msField.setFieldVariable(vd.getNameAsString());
                if (vd.getType() != null) {
                    msField.setFieldClass(vd.getTypeAsString());
                    msField.setParentMethod(visitParentMethod(n));
                    msField.setLine(n.getBegin().get().line);
                    msField.setId(id);

                    CachingService.getCache().getFieldList().add(msField);
                }
            }
        }
    }

}
