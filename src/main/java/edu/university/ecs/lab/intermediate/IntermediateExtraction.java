package edu.university.ecs.lab.intermediate;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.university.ecs.lab.semantics.services.Cache;
import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.config.InputConfig;
import edu.university.ecs.lab.common.config.InputRepository;
import edu.university.ecs.lab.common.models.MsModel;
import edu.university.ecs.lab.common.writers.MsJsonWriter;
import edu.university.ecs.lab.intermediate.services.GitCloneService;
import edu.university.ecs.lab.intermediate.services.RepositoryService;
import edu.university.ecs.lab.intermediate.utils.MsFileUtils;
import edu.university.ecs.lab.semantics.services.CachingService;
import edu.university.ecs.lab.semantics.services.FlowService;
import edu.university.ecs.lab.semantics.services.VisitorService;

import javax.json.JsonObject;
import javax.naming.spi.DirectoryManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;

/**
 * IntermediateExtraction is the main entry point for the intermediate extraction process.
 *
 * <p>The IR extraction process is responsible for cloning remote services, scanning through each
 * local repo and extracting endpoints/dependencies, and writing each service and endpoints to
 * intermediate representation.
 *
 * <p>
 */
public class IntermediateExtraction {
  /** Exit code: error writing IR to json */
  private static final int BAD_IR_WRITE = 3;

  /** system property for user directory */
  private static final String SYS_USER_DIR = "user.dir";

  private static final RepositoryService repositoryService = new RepositoryService();

  /**
   * Main method entry point to intermediate extraction
   *
   * @param args (optional) /path/to/config/file, defaults to config.json in the project directory.
   */
  public static void main(String[] args) throws Exception {
    // Get input config
    String jsonFilePath = (args.length == 1) ? args[0] : "config.json";
    InputConfig inputConfig = ConfigUtil.validateConfig(jsonFilePath);

    // Clone remote repositories and scan through each cloned repo to extract endpoints/dependencies
    Map<String, MsModel> msDataMap = cloneAndScanServices(inputConfig);

    scanCodeClones(inputConfig.getClonePath(), msDataMap.keySet());


    //  Write each service and endpoints to IR
    try {
      writeToIntermediateRepresentation(inputConfig, msDataMap);
    } catch (IOException e) {
      System.err.println("Error writing to IR json: " + e.getMessage());
      System.exit(BAD_IR_WRITE);
    }
  }

  /**
   * Write each service and endpoints to intermediate representation
   *
   * @param inputConfig the config file object
   * @param msEndpointsMap a map of service to their information
   */
  private static void writeToIntermediateRepresentation(
      InputConfig inputConfig, Map<String, MsModel> msEndpointsMap) throws IOException {

    String outputPath = System.getProperty(SYS_USER_DIR) + inputConfig.getOutputPath();

    File outputDir = new File(outputPath);

    if (!outputDir.exists()) {
      if (outputDir.mkdirs()) {
        System.out.println("Successfully created output directory.");
      } else {
        System.err.println("Failed to create output directory.");
        return;
      }
    }

    Scanner scanner = new Scanner(System.in); // read system name from command line
    System.out.println("Enter system name: ");
    JsonObject jout =
        MsFileUtils.constructJsonMsSystem(scanner.nextLine(), "0.0.1", msEndpointsMap);

    MsJsonWriter.writeJsonToFile(
        jout, outputPath + "/intermediate-output-[" + (new Date()).getTime() + "].json");
  }

  /**
   * Clone remote repositories and scan through each local repo and extract endpoints/dependencies
   *
   * @param inputConfig the input config object
   * @return a map of services and their endpoints
   */
  private static Map<String, MsModel> cloneAndScanServices(InputConfig inputConfig)
      throws Exception {
    Map<String, MsModel> msEndpointsMap = new HashMap<>();

    // Clone remote repositories
    String clonePath = System.getProperty(SYS_USER_DIR) + inputConfig.getClonePath();

    File cloneDir = new File(clonePath);
    if (!cloneDir.exists()) {
      if (cloneDir.mkdirs()) {
        System.out.println("Successfully created \"" + clonePath + "\" directory.");
      } else {
        System.err.println("Could not create clone directory");
        return null;
      }
    }

    InputRepository[] inputRepositories =
        inputConfig.getRepositories().toArray(new InputRepository[0]);

    // first one
    InputRepository[] reposFirst = new InputRepository[1]; // Create a new array with size 1
    reposFirst[0] = inputRepositories[0];

    MsModel model;
    String commit = reposFirst[0].getBaseCommit();
    GitCloneService gitCloneService = new GitCloneService(clonePath);
    List<String> msPathRoots = gitCloneService.cloneRemotes(reposFirst);

    // Scan through each local repo and extract endpoints/dependencies
    for (String msPath : msPathRoots) {

      String path = msPath;

      if (msPath.contains(clonePath) && msPath.length() > clonePath.length() + 1) {
        path = msPath.substring(clonePath.length() + 1);
      }

      model =
          repositoryService.recursivelyScanFiles(clonePath, msPath.substring(clonePath.length()));
      model.setCommit(commit);
      model.setId(msPath.substring(msPath.lastIndexOf('/') + 1));
      msEndpointsMap.put(path, model);
    }
    return msEndpointsMap;
  }

  public static  void scanCodeClones(String clonePath, Set<String> services) {

    if (services == null) {
      return;
    }

    CachingService cachingService = new CachingService();

    for (String path: services) {

      try {
        String discoverPath = System.getProperty(SYS_USER_DIR) + clonePath + File.separator +  path;
        File f = new File(System.getProperty(SYS_USER_DIR) + clonePath + File.separator +  path);
        VisitorService.processRoot(f);
      }
      catch(Exception e) {
        System.err.println(e.getMessage());
      }

    }

    Cache cache = CachingService.getCache();
    System.out.println(cache);

    FlowService fs = new FlowService();
    fs.buildFlows();

    cache = CachingService.getCache();
    System.out.println(cache);


  }
}
