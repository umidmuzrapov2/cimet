package edu.university.ecs.lab.semantics;

import edu.university.ecs.lab.semantics.util.MsCache;
import edu.university.ecs.lab.semantics.util.ProcessFiles;
import edu.university.ecs.lab.semantics.util.entityextraction.EntityContextAdapter;
import edu.university.ecs.lab.semantics.util.factory.*;
import edu.university.ecs.lab.semantics.util.file.CacheManager;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.File;


// Program Entry
@QuarkusMain
public class SemanticAnalysisCommand implements QuarkusApplication {

    public static String[] repoUrls;
    public static String outputPath;
    private static String clonePath;

    @Override
    public int run(String... args) throws Exception {
        long start = System.currentTimeMillis();
        initCache();
        initPaths(args);
        cloneRemotes(clonePath, repoUrls);
        preProcess();
        persistCache();
        System.out.println(System.currentTimeMillis() - start);
        return 0;
    }

    private void persistCache() {
        CacheManager cacheManager = new CacheManager();
        cacheManager.persistCache(outputPath);
    }

    private void initPaths(String... args) {
        clonePath = args[0];
        repoUrls = args[1].split(",");
        outputPath = args[2];
    }

    public void initCache(){
        MsCache.init();
    }

    private void cloneRemotes(String clonePath, String[] urls) throws Exception {
        File destinationDir = new File(clonePath);

        if (!destinationDir.exists()) {
            boolean success = destinationDir.mkdirs();
            if (!success) {
                System.err.println("Failed to create destination directory");
                System.exit(1);
            }
        }

        for (String url : urls) {
            String output = clonePath + File.separator + getRepositoryName(url);
            ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", url, output);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Git clone of " + url + " successful ");
            } else {
                throw new Exception("Git clone of " + url + " failed with status code: " + exitCode);
            }
        }

    }

    private String getRepositoryName(String repositoryUrl) {
        System.out.println("Extracting repo from url: " + repositoryUrl);

        // Extract repository name from the URL
        int lastSlashIndex = repositoryUrl.lastIndexOf('/');
        int lastDotIndex = repositoryUrl.lastIndexOf('.');
        return repositoryUrl.substring(lastSlashIndex + 1, lastDotIndex);
    }

    public void preProcess() {
        ProcessFiles.run(clonePath);
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.buildFlows();
        
        // Entity Construction
        MsCache.mappedEntities = EntityContextAdapter.getMappedEntityContext(clonePath);
    }

    public static void main(String[] args) {
        Quarkus.run(SemanticAnalysisCommand.class, args);
    }
}
