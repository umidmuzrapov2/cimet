package edu.university.ecs.lab.intermediate.merger;

import edu.university.ecs.lab.common.utils.MsFileUtils;
import edu.university.ecs.lab.common.writers.MsJsonWriter;
import edu.university.ecs.lab.intermediate.merger.models.Delta;
import edu.university.ecs.lab.intermediate.merger.models.MsSystem;
import edu.university.ecs.lab.intermediate.merger.services.MergeService;
import edu.university.ecs.lab.intermediate.merger.utils.IRParserUtils;
import edu.university.ecs.lab.rest.calls.models.MsModel;

import javax.json.JsonObject;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class IRMergeRunner {

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println("Invalid # of args, 2 expected: <path/to/intermediate-json> <path/to/delta-json>");
      return;
    }

    MergeService mergeService = new MergeService();

    MsSystem msSystem = IRParserUtils.parseIRSystem(args[0]);
    Map<String, MsModel> msModelMap = msSystem.getServiceMap();
    List<Delta> deltas = IRParserUtils.parseDelta(args[1]);

    // iterate through delta changes
    for (Delta delta : deltas) {
      String localPath = delta.getLocalPath();

      // check change type
      switch (delta.getChangeType()) {
        case "ADD":
          MsModel addModel = mergeService.extractNewModel(localPath);
          if (addModel == null) {
            continue;
          }

          msModelMap.put(localPath, addModel);
          break;
        case "DELETE":
          // TODO: logic to scan for affected files
          msModelMap.remove(localPath);
          break;
        case "MODIFY":
          MsModel changeModel = mergeService.extractNewModel(localPath);
          if (changeModel == null) {
            // TODO: same remove logic?
            msModelMap.remove(localPath);
            continue;
          }

          msModelMap.put(localPath, changeModel);
          break;
        default:
          break;
      }
    }

    // increment system version
    String systemName = msSystem.getSystemName();
    String version = mergeService.incrementVersion(msSystem.getVersion());

    // save new system representation
    writeNewIntermediate(systemName, version, msModelMap);
  }

  private static void writeNewIntermediate(String systemname, String version,
                                           Map<String, MsModel> msModelMap) throws IOException {
    JsonObject jout =
            MsFileUtils.constructJsonMsSystem(systemname, version, msModelMap);

    String outputPath =
            System.getProperty("user.dir") + File.separator + "out";

    String outputName =
            outputPath
                    + File.separator
                    + "rest-extraction-new-["
                    + (new Date()).getTime()
                    + "].json";

    MsJsonWriter.writeJsonToFile(jout, outputName);
    System.out.println("Successfully wrote updated extraction to: \"" + outputName + "\"");
  }
}
