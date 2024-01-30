package edu.university.ecs.lab.semantics;

import edu.university.ecs.lab.semantics.util.MsCache;
import edu.university.ecs.lab.semantics.util.ProcessFiles;
import edu.university.ecs.lab.semantics.util.entityextraction.EntityContextAdapter;
import edu.university.ecs.lab.semantics.util.entitysimilarity.strategies.EntitySematicSimilarityCheckStrategy;
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
    public static String cachePath;
    private static String sutPath;
    private static final String REPO_DESTINATION_DIRECTORY = "../repos";

    /**
     * This method serves as the main point of control for this application. It calls several functions that
     * separate each step of the Prophet Code Analysis Tool.
     *
     * @param args The first is a comma separated list of git repo urls and the second is a path to the output folder
     * @return 0 for successful completion
     * @throws Exception
     */
    @Override
    public int run(String... args) throws Exception {
        long start = System.currentTimeMillis();
        initCache();
        initPaths(args);
        cloneRemotes(repoUrls);
        preProcess();
        processCodeClonesFromCache();
        conductCalculation();
        persistCache();
        System.out.println(System.currentTimeMillis() - start);
        return 0;
    }

    /**
     * This method produces json files from the MsCache class that contains all the output data
     */
    private void persistCache() {
        CacheManager cacheManager = new CacheManager();
        cacheManager.persistCache(cachePath);
    }

    private void conductCalculation() {
        ModuleCloneFactory moduleCloneFactory = new ModuleCloneFactory();
        moduleCloneFactory.createData();
    }

    /**
     * This method sets the paths from the arguments passed into this application
     * @param args The first is a comma separated list of git repo urls and the second is a path to the output folder
     */
    private void initPaths(String... args) {
        repoUrls = args[0].split(",");
        cachePath = args[1];
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
    private void cloneRemotes(String[] urls) throws Exception {
        File destinationDir = new File(REPO_DESTINATION_DIRECTORY);

        if (!destinationDir.exists()) {
            boolean success = destinationDir.mkdirs();
            if (!success) {
                System.err.println("Failed to create destination directory");
                System.exit(1);
            }
        }

        for (String url : urls) {
            String output = REPO_DESTINATION_DIRECTORY + File.separator + getRepositoryName(url);
            ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", url, output);

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            if (exitCode < 300) {
                System.out.println("Git clone successful for repo: " + output);
            } else {
                throw new Exception("Git clone failed with status code: " + exitCode);
            }
        }

        sutPath = REPO_DESTINATION_DIRECTORY;
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
        ProcessFiles.run(sutPath);
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.buildFlows();
        
        // Entity Construction
        MsCache.mappedEntities = EntityContextAdapter.getMappedEntityContext(sutPath);
    }


    public void processCodeClonesFromCache() {
//        CacheManager cacheManager = new CacheManager();
//        cacheManager.recreateCache(cachePath);
//        CodeClonesFactory codeClonesFactory = new CodeClonesFactory(new EntityLiteralSimilarityCheckStrategy());
    	CodeClonesFactory codeClonesFactory = new CodeClonesFactory(new EntitySematicSimilarityCheckStrategy(true));
      codeClonesFactory.findCodeClones();
      ModuleClonePairFactory mcpf = new ModuleClonePairFactory();
      mcpf.printModuleClonePairs();
    }

    /**
     * Main function of the application
     * @param args The first is a comma separated list of git repo urls and the second is a path to the output folder
     */
    public static void main(String[] args) {
        Quarkus.run(SemanticAnalysisCommand.class, args);
    }
}
