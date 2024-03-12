// package edu.university.ecs.lab.intermediate;
//
// import edu.university.ecs.lab.rest.calls.services.RestModelService;
// import edu.university.ecs.lab.semantics.models.CodeClone;
// import edu.university.ecs.lab.semantics.services.*;
// import edu.university.ecs.lab.common.config.ConfigUtil;
// import edu.university.ecs.lab.common.config.InputConfig;
// import edu.university.ecs.lab.common.config.InputRepository;
// import edu.university.ecs.lab.common.models.MsModel;
// import edu.university.ecs.lab.common.writers.MsJsonWriter;
// import edu.university.ecs.lab.intermediate.utils.MsFileUtils;
// import edu.university.ecs.lab.rest.calls.services.GitCloneService;
//
// import javax.json.JsonObject;
// import java.io.File;
// import java.io.IOException;
// import java.util.*;
// import java.util.stream.Collectors;
//
/// **
// * IntermediateExtraction is the main entry point for the intermediate extraction process.
// *
// * <p>The IR extraction process is responsible for cloning remote services, scanning through each
// * local repo and extracting endpoints/dependencies, and writing each service and endpoints to
// * intermediate representation.
// *
// * <p>
// */
// public class IntermediateExtraction {
//  /** Exit code: error writing IR to json */
//  private static final int BAD_IR_WRITE = 3;
//
//  /** system property for user directory */
//  private static final String SYS_USER_DIR = "user.dir";
//
//  private static String systemName = "";
//  private static String timeStamp = "";
//
//  /**
//   * Main method entry point to intermediate extraction
//   *
//   * @param args (optional) /path/to/config/file, defaults to config.json in the project
// directory.
//   */
//  public static void main(String[] args) throws Exception {
//    // Get input config
//    String jsonFilePath = (args.length == 1) ? args[0] : "config.json";
//    InputConfig inputConfig = ConfigUtil.validateConfig(jsonFilePath);
//
//    // Clone remote repositories and scan through each cloned repo to extract
// endpoints/dependencies
//    Map<String, MsModel> msDataMap = cloneAndScanServices(inputConfig);
//
//
//    Map<String, List<CodeClone>> msClones = scanCodeClones(inputConfig.getClonePath(), msDataMap);
//
//    //  Write each service and endpoints to IR
//    try {
//      writeToIntermediateRepresentation(inputConfig, msDataMap);
//    } catch (IOException e) {
//      System.err.println("Error writing to IR json: " + e.getMessage());
//      System.exit(BAD_IR_WRITE);
//    }
//
//
//    //  Write each service and endpoints to IR
//    try {
//      writeToClonesRepresentation(inputConfig, msClones);
//    } catch (IOException e) {
//      System.err.println("Error writing to clone json: " + e.getMessage());
//      System.exit(BAD_IR_WRITE);
//    }
//  }
//
//  /**
//   * Write each service and endpoints to intermediate representation
//   *
//   * @param inputConfig the config file object
//   * @param msEndpointsMap a map of service to their information
//   */
//  private static void writeToIntermediateRepresentation(
//      InputConfig inputConfig, Map<String, MsModel> msEndpointsMap) throws IOException {
//
//    String outputPath = System.getProperty(SYS_USER_DIR) + inputConfig.getOutputPath();
//
//    File outputDir = new File(outputPath);
//
//    if (!outputDir.exists()) {
//      if (outputDir.mkdirs()) {
//        System.out.println("Successfully created output directory.");
//      } else {
//        System.err.println("Failed to create output directory.");
//        return;
//      }
//    }
//
//    Scanner scanner = new Scanner(System.in); // read system name from command line
//    System.out.println("Enter system name: ");
//    systemName = scanner.nextLine();
//    JsonObject jout =
//        MsFileUtils.constructJsonMsSystem(systemName, "0.0.1", msEndpointsMap);
//
//    timeStamp = String.valueOf((new Date()).getTime());
//    MsJsonWriter.writeJsonToFile(
//        jout, outputPath + "/intermediate-output-[" + timeStamp + "].json");
//  }
//
//
//  /**
//   * Write each service and clones to json
//   *
//   * @param inputConfig the config file object
//   * @param clonesMap a map of service to their code clones
//   */
//  private static void writeToClonesRepresentation(InputConfig inputConfig, Map<String,
// List<CodeClone>> clonesMap) throws IOException {
//
//    String outputPath = System.getProperty(SYS_USER_DIR) + inputConfig.getOutputPath();
//
//    File outputDir = new File(outputPath);
//
//    if (!outputDir.exists()) {
//      if (outputDir.mkdirs()) {
//        System.out.println("Successfully created output directory.");
//      } else {
//        System.err.println("Failed to create output directory.");
//        return;
//      }
//    }
//
//    JsonObject jout =
//            MsFileUtils.constructJsonClonesSystem(systemName, "0.0.1", clonesMap);
//
//    MsJsonWriter.writeJsonToFile(
//            jout, outputPath + "/code-clones-output-[" + timeStamp + "].json");
//  }
//
////  /**
////   * Clone remote repositories and scan through each local repo and extract
// endpoints/dependencies
////   *
////   * @param inputConfig the input config object
////   * @return a map of services and their endpoints
////   */
////  private static Map<String, MsModel> cloneAndScanServices(InputConfig inputConfig)
////      throws Exception {
////    Map<String, MsModel> msEndpointsMap = new HashMap<>();
////
////    // Clone remote repositories
////    String clonePath = System.getProperty(SYS_USER_DIR) + inputConfig.getClonePath();
////
////    File cloneDir = new File(clonePath);
////    if (!cloneDir.exists()) {
////      if (cloneDir.mkdirs()) {
////        System.out.println("Successfully created \"" + clonePath + "\" directory.");
////      } else {
////        System.err.println("Could not create clone directory");
////        return null;
////      }
////    }
////
////    InputRepository[] inputRepositories =
////        inputConfig.getRepositories().toArray(new InputRepository[0]);
////
////    // first one
////    InputRepository[] reposFirst = new InputRepository[1]; // Create a new array with size 1
////    reposFirst[0] = inputRepositories[0];
////
////    MsModel model;
////    String commit = reposFirst[0].getBaseCommit();
////    GitCloneService gitCloneService = new GitCloneService(clonePath);
////    List<String> msPathRoots = gitCloneService.cloneRemotes(reposFirst);
////
////    // Scan through each local repo and extract endpoints/dependencies
////    for (String msPath : msPathRoots) {
////
////      String path = msPath;
////
////      if (msPath.contains(clonePath) && msPath.length() > clonePath.length() + 1) {
////        path = msPath.substring(clonePath.length() + 1);
////      }
////
////      model =
////          restModelService.recursivelyScanFiles(clonePath,
// msPath.substring(clonePath.length()));
////      model.setCommit(commit);
////      model.setId(msPath.substring(msPath.lastIndexOf('/') + 1));
////      msEndpointsMap.put(path, model);
////    }
////    return msEndpointsMap;
////  }
//
//  public static Map<String, List<CodeClone>> scanCodeClones(String clonePath, Map<String, MsModel>
// services) {
//
//    if (services == null) {
//      return null;
//    }
//
//    CachingService cachingService = new CachingService();
//    CodeCloneService codeCloneService = new CodeCloneService();
//
//    for (String path: services.keySet()) {
//
//      try {
//        String discoverPath = System.getProperty(SYS_USER_DIR) + clonePath + File.separator +
// path;
//        File f = new File(System.getProperty(SYS_USER_DIR) + clonePath + File.separator +  path);
//        VisitorService.processRoot(f);
//      }
//      catch(Exception e) {
//        System.err.println(e.getMessage());
//      }
//
//    }
//
//    FlowService fs = new FlowService();
//    fs.buildFlows();
//
//    codeCloneService.findCodeClones();
//
//    List<CodeClone> l = CachingService.getCache().getCodeCloneList().stream().filter(a ->
// a.getGlobalSimilarity() > .9).collect(Collectors.toList());
//
//
//    Map<String, List<CodeClone>> clonesMap = new HashMap<>();
//
//      for (String service : services.keySet()) {
//        List<CodeClone> clones = new ArrayList<>();
//        for(CodeClone clone : l) {
//
//          String a = clone.getFlowA().getController().getId().getLocation();
//          String b = clone.getFlowB().getController().getId().getLocation();
//
//          if (a.contains(service) || b.contains(service)) {
//            clones.add(clone);
//          }
//
//        }
//        clonesMap.put(service, clones);
//
//
//      }
//
//      return clonesMap;
//
//  }
// }
