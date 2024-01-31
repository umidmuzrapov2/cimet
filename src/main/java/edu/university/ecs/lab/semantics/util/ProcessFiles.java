package edu.university.ecs.lab.semantics.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import edu.university.ecs.lab.semantics.entity.graph.MsClassRoles;
import edu.university.ecs.lab.semantics.entity.graph.MsId;
import edu.university.ecs.lab.semantics.util.visitor.MsVisitor;

/**
 * Class holding static functionality related to crawling through main file
 * directory and parsing initial repos for cache
 */
public class ProcessFiles {

    /**
     * This function crawls through every subdirectory iteratively under projectDir's path
     * and initializes an MsID object and calls appropriate visit method's if it is a 
     * Controller, Service, or Repository
     * 
     * @param projectDir the file object representing the folder containing repos
     */
    public static void processFile(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
//            System.out.println(path);
//            System.out.println(Strings.repeat("=", path.length()));
            MsClassRoles role = null;
            if (path.contains("Controller") && (!path.contains("Test"))){
                role = MsClassRoles.CONTROLLER;
            }
            if (path.contains("Service") && (!path.contains("Test"))) {
                role = MsClassRoles.SERVICE;
            }
            if (path.contains("Repository") && (!path.contains("Test"))) {
                role = MsClassRoles.REPOSITORY;
            }
            MsId msId = new MsId(path);
            if (role != null) {
                if (role.equals(MsClassRoles.CONTROLLER) || role.equals(MsClassRoles.SERVICE)) {
                    // CLASS
                    MsVisitor.visitClass(file, path, role, msId);
                    // METHOD
                    MsVisitor.visitMethods(file, role, path, msId);
                    // METHOD CALLS
                    MsVisitor.visitMethodCalls(file, path, msId);
                    // FIELDS
                    MsVisitor.visitFields(file, path, msId);
                } else if (role.equals(MsClassRoles.REPOSITORY)){
                    // CLASS
                    MsVisitor.visitClass(file, path, role, msId);
                    // METHOD
                    MsVisitor.visitMethods(file, role, path, msId);
                } else if (path.contains("entity")) {
                    // visitFieldDeclaration
                }
            } else {
//                System.out.println();
            }
        }).explore(projectDir);
        // PRINT CACHE
    }

    /**
     * This function first loads all modules into cache and then
     * calls processFile() on path
     * 
     * @param path path to the folder containing repos
     */
    public static void run(String path) {

        String myDirectoryPath = path;
        File file = new File(myDirectoryPath);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                boolean isDirectory = new File(current, name).isDirectory();
                boolean isModule = name.contains("ts");
                return isDirectory && isModule;
            }
        });
        MsCache.modules = Arrays.asList(directories);
        File projectDir = new File(path);
        processFile(projectDir);
//        System.out.println();
    }
}