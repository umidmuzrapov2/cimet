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



/**
 * SemanticAnalysisCommand serves as the entrypoint for the program
 * see https://quarkus.io/guides/lifecycle
 */
@QuarkusMain
public class SemanticAnalysisCommand implements QuarkusApplication {
    public static String DEFAULT_RESULT_DIR = "/results";
    public static String DEFAULT_CLONE_DIR = "/repos";

    /**
     * The remote git repository urls
     */
    public static String[] repoUrls;

    /**
     * Path to the output folder
     */
    public static String resultPath;

    /**
     * Path to the clone output
     */
    private static String clonePath;

    /**
     * This method serves as the main point of control for this application. It calls several functions that
     * separate each step of the Prophet Code Analysis Tool.
     *
     * If no arguments are specific for the repoOutputDir and resultOutputDir then the current executable's directory
     * is used and repos and results will be output in default directories
     *
     * @param args commandline args, can be in one of the following formats:
     *            <ul>
     *              <li> <repo1, repo2, repo3, ...> </li>
     *              <li> <repoOutputDir> <repo1, repo2, repo3, ...> <resultOutputDir> </li>
     *            </ul>
     * @return 0 for successful completion, otherwise an error has occured
     * @throws Exception
     */
    @Override
    public int run(String... args) throws Exception {
        long start = System.currentTimeMillis();
        initCache();
        initAndValidatePaths(args);
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
        cacheManager.persistCache(resultPath);
    }

    /**
     * This method sets the paths from the arguments passed into this application, parses them dependent on
     * input format, and verifies they exist or creates them if they don't
     *
     * @param args commandline args, can be in one of the following formats:
     *            <ul>
     *              <li> <repo1, repo2, repo3, ...> </li>
     *              <li> <repoOutputDir> <repo1, repo2, repo3, ...> <resultOutputDir> </li>
     *            </ul>
     * @throws IllegalArgumentException if the args do not follow a particular format
     * @throws RuntimeException if the directory's did not exist but could not be created
     */
    private void initAndValidatePaths(String... args) throws Exception {
        if(args.length == 1) {
            resultPath = System.getProperty("user.dir") + DEFAULT_RESULT_DIR;
            clonePath = System.getProperty("user.dir") + DEFAULT_CLONE_DIR;
            repoUrls = args[0].split(",");

        } else if (args.length == 3) {
            clonePath = args[0];
            repoUrls = args[1].split(",");
            resultPath = args[2];

        } else {
            throw new IllegalArgumentException("Usage: <repo1, repo2, repo3, ...> OR <repoOutputDir> <repo1, repo2, repo3, ...> <resultOutputDir>");
        }

        File resultPathFile = new File(resultPath);
        File clonePathFile = new File(clonePath);

        if (!resultPathFile.exists()) {
            boolean success = resultPathFile.mkdirs();
            if (!success) {
                throw new RuntimeException("Failed to create directory " + resultPathFile);
            }
        }

        if (!clonePathFile.exists()) {
            boolean success = clonePathFile.mkdirs();
            if (!success) {
                throw new RuntimeException("Failed to create directory " + clonePathFile);
            }
        }
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
     * @param clonePath the path the repositories will be cloned to
     * @param urls the urls of repositories to be cloned
     * @throws Exception if Git clone failed
     */
    private void cloneRemotes(String clonePath, String[] urls) throws Exception {
        for (String url : urls) {
            String output = clonePath + File.separator + getRepositoryName(url);
            ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", url, output);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if (exitCode < 400) {
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
     * main method entry, errors occuring within SemanticAnalysisCommand run() will be caught
     * by (exitCode, exception) -> {} and output here
     * @param args command line arguments passed to run() method
     */
    public static void main(String[] args) {
        Quarkus.run(SemanticAnalysisCommand.class, (exitCode, exception) -> {
            System.err.println(exception.getMessage());
            // System.err.println(exception.getStackTrace());
            System.exit(exitCode);
        }, args);
    }
}
