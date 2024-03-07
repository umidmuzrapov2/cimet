package edu.university.ecs.lab.rest.calls;

import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.config.InputConfig;
import edu.university.ecs.lab.common.config.InputRepository;
import edu.university.ecs.lab.rest.calls.models.MsModel;
import edu.university.ecs.lab.rest.calls.utils.MsFileUtils;
import edu.university.ecs.lab.common.writers.MsJsonWriter;
import edu.university.ecs.lab.rest.calls.services.GitCloneService;
import edu.university.ecs.lab.rest.calls.services.RestModelService;

import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * IntermediateExtraction is the main entry point for the intermediate extraction process.
 *
 * <p>The IR extraction process is responsible for cloning remote services, scanning through each
 * local repo and extracting rest endpoints/calls, and writing each service and endpoints to
 * intermediate representation.
 *
 * <p>
 */
public class IntermediateExtraction {
  /** Exit code: error writing IR to json */
  private static final int BAD_IR_WRITE = 3;

  /** system property for user directory */
  private static final String SYS_USER_DIR = "user.dir";

  private static final RestModelService REST_MODEL_SERVICE = new RestModelService();

  /**
   * Main method entry point to intermediate extraction
   *
   * @param args (optional) /path/to/config/file, defaults to config.json in the project directory.
   */
  public static void main(String[] args) throws Exception {
    // Get input config
    String jsonFilePath = (args.length == 1) ? args[0] : "config.json";
    InputConfig inputConfig = ConfigUtil.validateConfig(jsonFilePath);

    // Clone remote repositories and scan through each cloned repo to extract endpoints
    Map<String, MsModel> msDataMap = cloneAndScanServices(inputConfig);
    assert msDataMap != null;

    // Scan through each endpoint to extract rest call destinations
    extractCallDestinations(msDataMap);

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

    String outputPath = System.getProperty(SYS_USER_DIR) + File.separator + inputConfig.getOutputPath();

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

    String outputName = outputPath + File.separator + "intermediate-output-[" + (new Date()).getTime() + "].json";

    MsJsonWriter.writeJsonToFile(jout, outputName);
    System.out.println("Successfully wrote intermediate to: \"" + outputName + "\"");
  }

  /**
   * Clone remote repositories and scan through each local repo and extract endpoints/calls
   *
   * @param inputConfig the input config object
   * @return a map of services and their endpoints
   */
  private static Map<String, MsModel> cloneAndScanServices(InputConfig inputConfig)
      throws Exception {
    Map<String, MsModel> msModelMap = new HashMap<>();

    // Clone remote repositories
    String clonePath = System.getProperty(SYS_USER_DIR) + File.separator + inputConfig.getClonePath();

    File cloneDir = new File(clonePath);
    if (!cloneDir.exists()) {
      if (cloneDir.mkdirs()) {
        System.out.println("Successfully created \"" + clonePath + "\" directory.");
      } else {
        System.err.println("Could not create clone directory");
        return null;
      }
    }

    for (InputRepository inputRepository : inputConfig.getRepositories()) {
      MsModel model;
      GitCloneService gitCloneService = new GitCloneService(clonePath);
      List<String> msPathRoots = gitCloneService.cloneRemote(inputRepository);

      // Scan through each local repo and extract endpoints/calls
      for (String msPath : msPathRoots) {
        String path = msPath;

        if (msPath.contains(clonePath) && msPath.length() > clonePath.length() + 1) {
          path = msPath.substring(clonePath.length() + 1);
        }

        model = REST_MODEL_SERVICE.recursivelyScanFiles(clonePath, msPath.substring(clonePath.length()));
        model.setCommit(inputRepository.getBaseCommit());
        model.setId(msPath.substring(msPath.lastIndexOf('/') + 1));

        msModelMap.put(path, model);
      }
    }

    return msModelMap;
  }

  private static void extractCallDestinations(Map<String, MsModel> msModelMap) {
    // TODO: scan and find source file matching url

    msModelMap.forEach((name, model) -> {
      model.getRestCalls().forEach(call -> {
        String callMethod = call.getCallMethod();
      });
    });
  }
}
