package edu.university.ecs.lab.semantics.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import edu.university.ecs.lab.semantics.entity.graph.MsClassRoles;
import edu.university.ecs.lab.semantics.entity.graph.MsId;
import edu.university.ecs.lab.semantics.util.visitor.MsVisitor;

/**
 * Class holding static functionality for crawling through root file
 * directory and tagging files then storing them in cache
 */
public class ProcessFiles {

    /**
     * This function crawls through every repo iteratively under projectDir's (root) path 
     * and tags the role of the file, initializes an MsID object then calls visit method's 
     * to extract further data
     * 
     * @see edu.university.ecs.lab.semantics.util.visitor
     * 
     * @param projectDir the root directory, represents the file (folder) holding all repos
     */
    public static void processFile(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
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
     * This function first parses all modules (microservices)
     * then loads their names in cache and lastly calls
     * processFile on the root
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