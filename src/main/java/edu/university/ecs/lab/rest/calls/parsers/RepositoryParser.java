//package edu.university.ecs.lab.rest.calls.parsers;
//
//import com.github.javaparser.StaticJavaParser;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import edu.university.ecs.lab.common.models.rest.RestEntity;
//import edu.university.ecs.lab.common.models.rest.RestRepository;
//import edu.university.ecs.lab.rest.calls.utils.StringParserUtils;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///** Class for parsing REST repositories from source files */
//public class RepositoryParser {
//  public static List<RestRepository> parseRepos(File sourceFile) throws IOException {
//    List<RestRepository> restRepositories = new ArrayList<>();
//
//    CompilationUnit cu = StaticJavaParser.parse(sourceFile);
//
//    String packageName = StringParserUtils.findPackage(cu);
//    if (packageName == null) {
//      return restRepositories;
//    }
//
//    // loop through class declarations
//    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
//      restRepositories.add(new RestRepository(RestParser.extractJavaClass(sourceFile, cid)));
//    }
//
//    return restRepositories;
//  }
//
//  public static List<RestEntity> parseEntities(File sourceFile) throws IOException {
//    List<RestEntity> restEntities = new ArrayList<>();
//
//    CompilationUnit cu = StaticJavaParser.parse(sourceFile);
//
//    String packageName = StringParserUtils.findPackage(cu);
//    if (packageName == null) {
//      return restEntities;
//    }
//
//    // loop through class declarations
//    for (ClassOrInterfaceDeclaration cid : cu.findAll(ClassOrInterfaceDeclaration.class)) {
//      restEntities.add(new RestEntity(RestParser.extractJavaClass(sourceFile, cid)));
//    }
//
//    return restEntities;
//  }
//}
