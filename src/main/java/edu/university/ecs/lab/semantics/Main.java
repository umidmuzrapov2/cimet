package edu.university.ecs.lab.semantics;

import edu.university.ecs.lab.common.config.InputConfig;
import edu.university.ecs.lab.semantics.services.CachingService;
import edu.university.ecs.lab.semantics.services.SetupService;
import edu.university.ecs.lab.semantics.services.VisitorService;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {
        String configPath = (args.length == 1) ? args[0] : "config.json";
        InputConfig config = SetupService.loadConfig(configPath);
//        SetupService.cloneRepositories(config);

        String s = System.getProperty("user.dir") + File.separator + config.getClonePath() + "\\train-ticket-microservices";
        File file = new File(s);


        for(String childName : file.list()) {
            File f = new File(file.getAbsoluteFile(), childName);
            if(f.isDirectory() && f.getName().contains("ts")) {
                VisitorService visitorService = new VisitorService(f.getName(), f);
                visitorService.processRoot();
            }
        }

        CachingService cachingService = new CachingService();
        cachingService.persistCache();
    }
}
