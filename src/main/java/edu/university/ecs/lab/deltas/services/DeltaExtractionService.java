package edu.university.ecs.lab.deltas.services;

import edu.university.ecs.lab.common.writers.MsJsonWriter;
import edu.university.ecs.lab.deltas.utils.DeltaComparisonUtils;
import edu.university.ecs.lab.deltas.utils.GitFetchUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;

import javax.json.*;
import java.io.*;
import java.util.*;

public class DeltaExtractionService {
  private final GitFetchUtils gitFetchUtils = new GitFetchUtils();
  private final DeltaComparisonUtils comparisonUtils = new DeltaComparisonUtils();

  public Repository establishLocalEndpoint(String path) throws IOException {
    return gitFetchUtils.establishLocalEndpoint(path);
  }

  public List<DiffEntry> fetchRemoteDifferences(Repository repo, String branch) throws Exception {
    return gitFetchUtils.fetchRemoteDifferences(repo, branch);
  }

  public void processDifferences(String path, Repository repo, List<DiffEntry> diffEntries)
      throws IOException {
    JsonObjectBuilder outputBuilder = Json.createObjectBuilder();

    // process each difference
    for (DiffEntry entry : diffEntries) {
      System.out.println(
          "Change impact of type " + entry.getChangeType() + " detected in " + entry.getNewPath());

      String changeURL = gitFetchUtils.getGithubFileUrl(repo, entry);
      System.out.println("Extracting changes from: " + changeURL);

      String localPath = path + "/" + entry.getOldPath();

      javax.json.JsonArray deltaChanges;

      switch (entry.getChangeType()) {
        case MODIFY:
          // fetch changed file
          String fileContents = gitFetchUtils.fetchAndDecodeFile(changeURL);

          // compare differences with local path
          deltaChanges = comparisonUtils.extractDeltaChanges(fileContents, localPath);
          break;
        case DELETE:
        case COPY:
        case RENAME:
        case ADD:
        default:
          deltaChanges = Json.createArrayBuilder().build();
          break;
      }

      JsonObjectBuilder jout = Json.createObjectBuilder();
      jout.add("local-previous", localPath);
      jout.add("remote-api", changeURL);
      jout.add("change-type", entry.getChangeType().name());
      jout.add("changes", deltaChanges);

      outputBuilder.add(entry.getNewId().name(), jout);
    }

    // write differences to output file
    String outputName = "delta-changes-[" + (new Date()).getTime() + "].json";
    MsJsonWriter.writeJsonToFile(outputBuilder.build(), outputName);

    System.out.println("Delta extracted: " + outputName);
  }
}
