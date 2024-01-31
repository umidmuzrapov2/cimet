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

    /**
     * The remote git repository urls
     */
    public static String[] repoUrls;

    /**
     * Path to the output folder
     */
    public static String outputPath;
    private static String clonePath;

    /**
     * This method serves as the main point of control for this application. It calls several functions that
     * separate each step of the Prophet Code Analysis Tool.
     *
     * @param args The first is a destination path of where to clone the repos the second is a comma separated
     *             list of git repo urls and the third is a path to the output folder
     * @return 0 for successful completion
     * @throws Exception
     */
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

    /**
     * This method produces json files from the MsCache class that contains all the output data
     */
    private void persistCache() {
        CacheManager cacheManager = new CacheManager();
        cacheManager.persistCache(outputPath);
    }

    /**
     * This method sets the paths from the arguments passed into this application
     * @param args The first is a destination path of where to clone the repos the second is a comma separated
     *             list of git repo urls and the third is a path to the output folder
     */
    private void initPaths(String... args) {
        clonePath = args[0];
        repoUrls = args[1].split(",");
        outputPath = args[2];
    }

    /**
     * This method calls the initialization function of the MsCache class
     */
    public void initCache(){
        MsCache.init();
    }

    /**
     * This method clones remote repositories to the local file system
     *
     * @param urls the urls of repositories to be cloned
     * @throws Exception if Git clone failed
     */
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

    /**
     * This method parses a repository url and extracts the repository name
     * @param repositoryUrl the repository url to parsing
     * @return the repository name
     */
    private String getRepositoryName(String repositoryUrl) {
        System.out.println("Extracting repo from url: " + repositoryUrl);

        // Extract repository name from the URL
        int lastSlashIndex = repositoryUrl.lastIndexOf('/');
        int lastDotIndex = repositoryUrl.lastIndexOf('.');
        return repositoryUrl.substring(lastSlashIndex + 1, lastDotIndex);
    }

    /**
     * This method does all the data preprocessing from the cloned repositories and stores it into MsCache.
     * First it parses all the files that were cloned, then it builds flows off of that data,
     * and lastly does entity construction
     */
    public void preProcess() {
        ProcessFiles.run(clonePath);
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.buildFlows();
        
        // Entity Construction
        MsCache.mappedEntities = EntityContextAdapter.getMappedEntityContext(clonePath);
    }

    /**
     * Main function of the application
     * @param args The first is a destination path of where to clone the repos the second is a comma separated
     *             list of git repo urls and the third is a path to the output folder
     */
    public static void main(String[] args) {
        Quarkus.run(SemanticAnalysisCommand.class, args);
    }
}
