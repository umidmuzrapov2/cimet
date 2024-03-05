package edu.university.ecs.lab.semantics;

import edu.university.ecs.lab.common.config.InputConfig;
import edu.university.ecs.lab.semantics.services.CachingService;
import edu.university.ecs.lab.semantics.services.SetupService;
import edu.university.ecs.lab.semantics.services.VisitorService;
import javassist.compiler.ast.Visitor;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.io.FilenameFilter;

public class Main {

    public static void main(String[] args) throws Exception {
        String configPath = (args.length == 1) ? args[0] : "config.json";
        InputConfig config = SetupService.loadConfig(configPath);
//        SetupService.cloneRepositories(config);

        File file = new File("C:\\Users\\Gabriel_Goulis1\\IdeaProjects\\curr-cimet\\out\\train-ticket-microservices");
        for(String childName : file.list()) {
            File f = new File(file.getAbsoluteFile(), childName);
            if(f.isDirectory() && f.getName().contains("ts")) {
                VisitorService.processRoot(f);
            }
        }

        CachingService cachingService = new CachingService();
        cachingService.persistCache();
    }
}
