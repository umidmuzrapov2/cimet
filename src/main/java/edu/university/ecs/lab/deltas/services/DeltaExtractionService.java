package edu.university.ecs.lab.deltas.services;

import edu.university.ecs.lab.deltas.utils.DeltaComparisonUtils;
import edu.university.ecs.lab.deltas.utils.GitFetchUtils;
import edu.university.ecs.lab.deltas.writers.DeltaWriter;
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
    // process each difference
    for (DiffEntry entry : diffEntries) {
      System.out.println(
          "Change impact of type " + entry.getChangeType() + " detected in " + entry.getNewPath());

      String changeURL = gitFetchUtils.getGithubFileUrl(repo, entry);
      System.out.println("Extracting changes from: " + changeURL);

      // fetch changed file
      String fileContents = gitFetchUtils.fetchAndDecodeFile(changeURL);

      // compare differences with local path
      String localPath = path + "/" + entry.getOldPath();
      javax.json.JsonArray deltaChanges =
          comparisonUtils.extractDeltaChanges(fileContents, localPath);

      JsonObjectBuilder jout = Json.createObjectBuilder();
      jout.add("local-file", localPath);
      jout.add("remote-api", changeURL);
      jout.add("changes", deltaChanges);

      // write differences to output file
      String outputName =
          "delta-changes-[" + (new Date()).getTime() + "]-" + entry.getNewId().name() + ".json";
      DeltaWriter.writeJsonToFile(jout.build(), outputName);

      System.out.println("Delta extracted: " + outputName);
    }
  }
}
