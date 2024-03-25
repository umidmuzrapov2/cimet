package edu.university.ecs.lab.semantics;

import com.github.javaparser.utils.Pair;
import edu.university.ecs.lab.common.config.InputConfig;
import edu.university.ecs.lab.semantics.models.Method;
import edu.university.ecs.lab.semantics.models.RestCall;
import edu.university.ecs.lab.semantics.services.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.util.List;

public class Main {

  public static void main(String[] args) throws Exception {
    String configPath = (args.length == 1) ? args[0] : "config.json";
    InputConfig config = SetupService.loadConfig(configPath);
    SetupService.cloneRepositories(config);

    String s =
        System.getProperty("user.dir")
            + File.separator
            + config.getClonePath()
            + "\\train-ticket-microservices";
    File file = new File(s);

    for (String childName : file.list()) {
      File f = new File(file.getAbsoluteFile(), childName);
      if (f.isDirectory() && f.getName().contains("ts")) {
        VisitorService visitorService = new VisitorService(f.getName(), f);
        visitorService.processRoot();
      }
    }

    FlowService f = new FlowService();
    f.buildFlows();

    CachingService cachingService = new CachingService();
    cachingService.persistCache();
    List<Pair<RestCall, Method>> list = DependencyService.getRawDependencies();
    System.out.println(DependencyService.getDependenciesList(list));

    //  }
    // }

    //    String s = System.getProperty("user.dir")
    //            + File.separator
    //            + config.getClonePath()
    //            + "\\train-ticket-microservices";

    // Get the local repository
    File localRepoDir = new File(s);
    Repository localRepo =
        new FileRepositoryBuilder().setGitDir(new File(localRepoDir, ".git")).build();

    List<String> changedFiles =
        SetupService.changes(localRepo, "82d85b7295169b55b56da9787afe72b9198b3106");

    // Set it to second commit (the next commit) so we can actually visit new content
    try (Git git = new Git(localRepo)) {
      git.reset()
          .setMode(ResetCommand.ResetType.HARD)
          .setRef("82d85b7295169b55b56da9787afe72b9198b3106")
          .call();

    } catch (Exception e) {
      e.printStackTrace();
    }

    // Load cache
    //    CachingService cachingService = new CachingService();
    //    cachingService.loadCache();

    // Update cache

    // Loop through the cache, remove instances of
    for (String fname : changedFiles) {
      if (fname.endsWith(".java")) {
        VisitorService visitorService =
            new VisitorService(
                fname.substring(0, fname.indexOf('/')),
                new File(s + fname.substring(0, fname.indexOf('/'))));
        cachingService.clearCacheOfFile(s + "\\" + fname.replace('/', '\\'));
        visitorService.processUpdate(new File(s + "\\" + fname.replace('/', '\\')));
      }
    }
    //    flowService.buildFlows();
    List<Pair<RestCall, Method>> list2 = DependencyService.getRawDependencies();
    System.out.println(DependencyService.getDependenciesList(list2));

    System.out.println("Dependencies Before");
    System.out.println(DependencyService.getDependenciesMap(list));

    System.out.println("Dependencies After");
    System.out.println(DependencyService.getDependenciesMap(list2));

    System.out.println("NEW");
    DependencyService.compareTwoMaps(
        DependencyService.getDependenciesMap(list), DependencyService.getDependenciesMap(list2));
  }
}
