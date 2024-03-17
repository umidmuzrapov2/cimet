package edu.university.ecs.lab.rest.calls.parsers;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import edu.university.ecs.lab.common.models.JavaClass;
import edu.university.ecs.lab.common.models.JavaMethod;
import edu.university.ecs.lab.common.models.JavaVariable;
import edu.university.ecs.lab.common.models.rest.*;
import edu.university.ecs.lab.rest.calls.utils.StringParserUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParserService {
    /**
     * Parse the REST calls from the given source file.
     *
     * @param sourceFile the source file to parse
     * @return the list of parsed dependencies
     * @throws IOException if an I/O error occurs
     */
    public static List<RestCall> parseCalls(File sourceFile) throws IOException {
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
                                restCall.setHttpMethod(getHttpMethodForExchange(mce.getArguments().toString()));
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

    /**
     * Find the URL from the given method call expression.
     *
     * @param mce the method call to extract url from
     * @param cid the class or interface to search
     * @return the URL found
     */
    private static String findUrl(MethodCallExpr mce, ClassOrInterfaceDeclaration cid) {
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
    private static boolean hasRestTemplateImport(CompilationUnit cu) {
        for (ImportDeclaration id : cu.findAll(ImportDeclaration.class)) {
            if (id.getNameAsString().equals("org.springframework.web.client.RestTemplate")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRestTemplateScope(Expression scope, ClassOrInterfaceDeclaration cid) {
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

    private static boolean isRestTemplateField(ClassOrInterfaceDeclaration cid, String fieldName) {
        for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
            if (fd.getElementType().toString().equals("RestTemplate")
                    && fd.getVariables().toString().contains(fieldName)) {

                return true;
            }
        }
        return false;
    }

    private static String fieldValue(ClassOrInterfaceDeclaration cid, String fieldName) {
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
    private static String resolveUrlFromBinaryExp(BinaryExpr exp) {
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

    /**
     * Parse the REST endpoints from the given source file.
     *
     * @param sourceFile the source file to parse
     * @return the list of parsed endpoints
     * @throws IOException if an I/O error occurs
     */
    public static List<RestController> parseEndpoints(File sourceFile) throws IOException {
        List<RestController> restControllers = new ArrayList<>();

        CompilationUnit cu = StaticJavaParser.parse(sourceFile);

        String packageName = StringParserUtils.findPackage(cu);
        if (packageName == null) {
            return restControllers;
        }

        // loop through class declarations
        for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            String className = cid.getNameAsString();

            AnnotationExpr aExpr = cid.getAnnotationByName("RequestMapping").orElse(null);
            if (aExpr == null) {
                return restControllers;
            }

            String classLevelPath = pathFromAnnotation(aExpr);

            RestController restController = new RestController();

            // loop through methods
            for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
                JavaMethod method = RestParser.extractJavaMethod(md);
                RestEndpoint restEndpoint = new RestEndpoint();

                // loop through annotations
                for (AnnotationExpr ae : md.getAnnotations()) {
                    restEndpoint.setUrl(StringParserUtils.mergePaths(classLevelPath, pathFromAnnotation(ae)));
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

                    restEndpoint.setParentMethod(
                            packageName + "." + className + "." + method.getMethodName());
                    restEndpoint.setMethod(method);
                    restEndpoint.setMethodVariables(RestParser.extractVariables(md));

                    restController.addEndpoint(restEndpoint);
                }

                restController.setClassName(className);
                restController.setSourceFile(sourceFile.getCanonicalPath());
                restController.setVariables(RestParser.extractVariables(cid));

                restControllers.add(restController);
            }
        }

        return restControllers;
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

    public static List<RestRepository> parseRepos(File sourceFile) throws IOException {
        List<RestRepository> restRepositories = new ArrayList<>();

        CompilationUnit cu = StaticJavaParser.parse(sourceFile);

        String packageName = StringParserUtils.findPackage(cu);
        if (packageName == null) {
            return restRepositories;
        }

        // loop through class declarations
        for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            restRepositories.add(new RestRepository(RestParser.extractJavaClass(sourceFile, cid)));
        }

        return restRepositories;
    }

    public static List<RestEntity> parseEntities(File sourceFile) throws IOException {
        List<RestEntity> restEntities = new ArrayList<>();

        CompilationUnit cu = StaticJavaParser.parse(sourceFile);

        String packageName = StringParserUtils.findPackage(cu);
        if (packageName == null) {
            return restEntities;
        }

        // loop through class declarations
        for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            restEntities.add(new RestEntity(RestParser.extractJavaClass(sourceFile, cid)));
        }

        return restEntities;
    }

    /**
     * Get the java method from the given declaration
     *
     * @param md method declaration
     * @return method information
     */
    public static JavaMethod extractJavaMethod(MethodDeclaration md) {
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

    public static JavaClass extractJavaClass(File sourceFile, ClassOrInterfaceDeclaration cid)
            throws IOException {
        JavaClass javaClass = new JavaClass();

        javaClass.setClassName(cid.getNameAsString());
        javaClass.setSourceFile(sourceFile.getCanonicalPath());
        javaClass.setVariables(extractVariables(cid));
        javaClass.setMethods(extractMethods(cid));

        return javaClass;
    }

    public static List<JavaVariable> extractVariables(ClassOrInterfaceDeclaration cid) {
        List<JavaVariable> javaVariables = new ArrayList<>();

        // loop through/find variables
        for (FieldDeclaration fd : cid.findAll(FieldDeclaration.class)) {
            for (VariableDeclarator variable : fd.getVariables()) {
                javaVariables.add(new JavaVariable(variable.getNameAsString(), variable.getTypeAsString()));
            }
        }

        return javaVariables;
    }

    public static List<JavaVariable> extractVariables(MethodDeclaration md) {
        List<JavaVariable> javaVariables = new ArrayList<>();

        BlockStmt methodBody = md.getBody().orElse(null);
        if (methodBody == null) {
            return javaVariables;
        }

        // loop through/find variables
        for (VariableDeclarationExpr vExpr : methodBody.findAll(VariableDeclarationExpr.class)) {
            if (vExpr.getElementType().isClassOrInterfaceType()) {
                for (VariableDeclarator vd : vExpr.getVariables()) {
                    javaVariables.add(new JavaVariable(vd.getNameAsString(), vd.getTypeAsString()));
                }
            }
        }

        return javaVariables;
    }

    public static List<JavaMethod> extractMethods(ClassOrInterfaceDeclaration cid) {
        List<JavaMethod> javaMethods = new ArrayList<>();

        // loop through methods
        for (MethodDeclaration md : cid.findAll(MethodDeclaration.class)) {
            javaMethods.add(RestParser.extractJavaMethod(md));
        }

        return javaMethods;
    }

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
