package edu.university.ecs.lab.intermediate;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.university.ecs.lab.common.config.InputConfig;
import edu.university.ecs.lab.common.config.Microservice;
import edu.university.ecs.lab.common.models.MsModel;
import edu.university.ecs.lab.common.writers.MsJsonWriter;
import edu.university.ecs.lab.intermediate.services.GitCloneService;
import edu.university.ecs.lab.intermediate.services.RepositoryService;
import edu.university.ecs.lab.intermediate.utils.MsFileUtils;

import javax.json.JsonObject;
import java.io.FileReader;
import java.util.*;

public class IntermediateExtraction {

  public static String clonePath;

  /**
   * main method entry point to intermediate extraction
   *
   * @param args (optional) /path/to/config/file
   */
  public static void main(String[] args) throws Exception {
    String jsonFilePath = (args.length == 1) ? args[0] : "config.json";

    JsonReader jsonReader = new JsonReader(new FileReader(jsonFilePath));
    Gson gson = new Gson();
    InputConfig inputConfig = gson.fromJson(jsonReader, InputConfig.class);

    if (inputConfig.getClonePath() == null) {
      System.err.println("Config file requires attribute \"clonePath\"");
    } else if (inputConfig.getOutputPath() == null) {
      System.err.println("Config file requires attribute \"outputPath\"");
    } else if (inputConfig.getMicroservices() == null) {
      System.err.println("Config file requires attribute \"microservices\"");
    }

    Map<String, MsModel> msEndpointsMap = new HashMap<>();

    String outputPath = System.getProperty("user.dir") + inputConfig.getOutputPath();

    clonePath = System.getProperty("user.dir") + inputConfig.getClonePath();

    // clone remote services (ideal scenario: 1 service per repo)
    Microservice[] microservices = inputConfig.getMicroservices().toArray(new Microservice[0]);
    GitCloneService gitCloneService = new GitCloneService(clonePath);
    List<String> msPathRoots = gitCloneService.cloneRemotes(microservices);

    RepositoryService repositoryService = new RepositoryService();

    // scan through each local repo and extract endpoints/dependencies
    for (String msPath : msPathRoots) {
      String path = msPath;

      if (msPath.contains(clonePath) && msPath.length() > clonePath.length() + 1) {
        path = msPath.substring(clonePath.length() + 1);
      }

      msEndpointsMap.put(
          path,
          repositoryService.recursivelyScanFiles(clonePath, msPath.substring(clonePath.length())));
    }

    //    List<Dependency> externalDependencies = new ArrayList<>();

    // find dependency endpoints

    //  write each service and endpoints to intermediate representation
    Scanner scanner = new Scanner(System.in); // read system name from command line
    System.out.println("Enter system name: ");
    JsonObject jout =
        MsFileUtils.constructJsonMsSystem(scanner.nextLine(), "0.0.1", msEndpointsMap);

    MsJsonWriter.writeJsonToFile(
        jout, outputPath + "/intermediate-output-[" + (new Date()).getTime() + "].json");
  }
}
