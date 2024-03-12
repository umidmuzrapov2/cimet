package edu.university.ecs.lab.codeclones;

import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.config.InputConfig;
import edu.university.ecs.lab.common.config.InputRepository;
import edu.university.ecs.lab.common.models.rest.RestController;
import edu.university.ecs.lab.common.utils.MsFileUtils;
import edu.university.ecs.lab.common.writers.MsJsonWriter;
import edu.university.ecs.lab.rest.calls.models.MsModel;
import edu.university.ecs.lab.rest.calls.services.GitCloneService;
import edu.university.ecs.lab.rest.calls.services.RestModelService;
import edu.university.ecs.lab.semantics.models.CodeClone;
import edu.university.ecs.lab.semantics.services.*;

import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class CloneExtraction {

    /** Exit code: error writing IR to json */
    private static final int BAD_IR_WRITE = 3;

    /** system property for user directory */
    private static final String SYS_USER_DIR = "user.dir";


    public static void main(String[] args) throws Exception {
        // Get input config
        String jsonFilePath = (args.length == 1) ? args[0] : "config.json";
        InputConfig inputConfig = ConfigUtil.validateConfig(jsonFilePath);

        // Clone remote repositories and scan through each cloned repo to extract endpoints
        Map<String, MsModel> msDataMap = cloneAndScanServices(inputConfig);
        assert msDataMap != null;


        // Scan through each endpoint to extract rest call destinations
        extractCallDestinations(msDataMap);


        Map<String, List<CodeClone>> msClones = scanCodeClones(inputConfig.getClonePath(), msDataMap);

        //  Write each service and endpoints to IR
        try {
            writeToClonesRepresentation(inputConfig, msClones);
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
    /**
     * Write each service and clones to json
     *
     * @param inputConfig the config file object
     * @param clonesMap a map of service to their code clones
     */
    private static void writeToClonesRepresentation(InputConfig inputConfig, Map<String, List<CodeClone>> clonesMap) throws IOException {

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
                MsFileUtils.constructJsonClonesSystem(scanner.nextLine(), "0.0.1", clonesMap);

        String outputName =
                outputPath
                        + File.separator
                        + "code-clones-output-["
                        + (new Date()).getTime()
                        + "].json";
        MsJsonWriter.writeJsonToFile(jout, outputName);
        System.out.println("Successfully wrote rest extraction to: \"" + outputName + "\"");
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
        String clonePath =
                System.getProperty(SYS_USER_DIR) + File.separator + inputConfig.getClonePath();

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

                model =
                        RestModelService.recursivelyScanFiles(clonePath, msPath.substring(clonePath.length()));
                assert model != null;

                model.setCommit(inputRepository.getBaseCommit());
                model.setId(msPath.substring(msPath.lastIndexOf('/') + 1));

                msModelMap.put(path, model);
            }
        }

        return msModelMap;
    }

    // todo: this might not be the best way to go about this
    private static void extractCallDestinations(Map<String, MsModel> msModelMap) {
        msModelMap.forEach(
                (name, model) -> {
                    model
                            .getRestCalls()
                            .forEach(
                                    call -> {
                                        String callUrl = call.getUrl();
                                        String httpMethod = call.getHttpMethod();

                                        RestController matchingController = null;

                                        // iterate until either endpoint is found OR entire URL has been scanned
                                        while (Objects.isNull(matchingController) && callUrl.contains("/")) {
                                            final String tmpCallUrl = callUrl;

                                            for (MsModel ms : msModelMap.values()) {
                                                matchingController =
                                                        ms.getRestControllers().stream()
                                                                .filter(
                                                                        controller ->
                                                                                controller.getRestEndpoints().stream()
                                                                                        .anyMatch(
                                                                                                endpoint ->
                                                                                                        (endpoint.getUrl().equals(tmpCallUrl)
                                                                                                                || endpoint
                                                                                                                .getUrl()
                                                                                                                .contains(tmpCallUrl))
                                                                                                                && endpoint
                                                                                                                .getHttpMethod()
                                                                                                                .equals(httpMethod)))
                                                                .findFirst()
                                                                .orElse(null);

                                                if (Objects.nonNull(matchingController)) {
                                                    break;
                                                }
                                            }

                                            // Endpoint still not found? Try chopping off beginning of url by each '/'
                                            if (Objects.isNull(matchingController)) {
                                                callUrl = callUrl.substring(1);

                                                int slashNdx = callUrl.indexOf("/");
                                                if (slashNdx > 0) {
                                                    callUrl = callUrl.substring(slashNdx);
                                                }
                                            }
                                        }

                                        if (Objects.nonNull(matchingController)) {
                                            call.setDestFile(matchingController.getSourceFile());
                                        }
                                    });
                });
    }



    public static Map<String, List<CodeClone>> scanCodeClones(String clonePath, Map<String, MsModel> services) {

        if (services == null) {
            return null;
        }

        CachingService cachingService = new CachingService();
        CodeCloneService codeCloneService = new CodeCloneService();

        for (String path: services.keySet()) {

            try {
                String discoverPath = System.getProperty(SYS_USER_DIR) + File.separator + clonePath + File.separator +  path;

                VisitorService visitorService = new VisitorService(path, new File(discoverPath));
                visitorService.processRoot();
            }
            catch(Exception e) {
                System.err.println(e.getMessage());
            }

        }

        FlowService fs = new FlowService();
        fs.buildFlows();

        codeCloneService.findCodeClones();

        Cache cache = CachingService.getCache();
        List<CodeClone> l = cache.getCodeCloneList().stream().filter(a -> a.getGlobalSimilarity() > .9).collect(Collectors.toList());


        Map<String, List<CodeClone>> clonesMap = new HashMap<>();

        for (String service : services.keySet()) {
            List<CodeClone> clones = new ArrayList<>();
            for(CodeClone clone : l) {

                String a = clone.getFlowA().getController().getId().getLocation();
                String b = clone.getFlowB().getController().getId().getLocation();

                if (a.contains(service) || b.contains(service)) {
                    clones.add(clone);
                }

            }
            clonesMap.put(service, clones);


        }

        return clonesMap;

    }




}
