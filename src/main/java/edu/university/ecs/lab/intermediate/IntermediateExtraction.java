package edu.university.ecs.lab.intermediate;

import edu.university.ecs.lab.common.models.MsModel;
import edu.university.ecs.lab.common.writers.MsJsonWriter;
import edu.university.ecs.lab.intermediate.services.GitCloneService;
import edu.university.ecs.lab.intermediate.services.RepositoryService;
import edu.university.ecs.lab.intermediate.utils.MsFileUtils;

import javax.json.JsonObject;
import java.util.*;

public class IntermediateExtraction {
  /**
   * main method entry point to intermediate extraction
   *
   * @param args /path/to/clone/folder 'comma,separated,list,of,remote,microservices' /path/to/output
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      System.err.println("Invalid # of args.");
      return;
    }

    Map<String, MsModel> msEndpointsMap = new HashMap<>();
    RepositoryService repositoryService = new RepositoryService();
    String outputPath = args[2];

    // clone remote services (ideal scenario: 1 service per repo)
    GitCloneService gitCloneService = new GitCloneService(args[0]);
    List<String> msPathRoots = gitCloneService.cloneRemotes(args[1].split(","));

    // scan through each local repo and extract endpoints/dependencies
    for (String msPath : msPathRoots) {
      msEndpointsMap.put(msPath, repositoryService.recursivelyScanFiles(msPath));
    }

    //  write each service and endpoints to intermediate representation
    Scanner scanner = new Scanner(System.in); // read system name from command line
    System.out.println("Enter system name: ");
    JsonObject jout = MsFileUtils.constructJsonMsSystem(scanner.nextLine(), "0.0.1", msEndpointsMap);

    MsJsonWriter.writeJsonToFile(jout, outputPath + "/intermediate-output-["+(new Date()).getTime() + "].json");
  }
}
