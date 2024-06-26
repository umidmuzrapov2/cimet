package edu.university.ecs.lab.deltas.services;

import edu.university.ecs.lab.common.writers.MsJsonWriter;
import edu.university.ecs.lab.deltas.utils.DeltaComparisonUtils;
import edu.university.ecs.lab.deltas.utils.GitFetchUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;

import javax.json.*;
import java.io.*;
import java.util.*;

/**
 * Service for extracting the differences between a local and remote repository and generating delta
 * son.
 */
public class DeltaExtractionService {
  /** The GitFetchUtils object for fetching git differences */
  private final GitFetchUtils gitFetchUtils = new GitFetchUtils();

  /** Service to compare service dependency model to the git differences */
  private final DeltaComparisonUtils comparisonUtils = new DeltaComparisonUtils();

  /**
   * Wrapper of {@link GitFetchUtils#establishLocalEndpoint(String)} a local endpoint for the given
   * repository path.
   *
   * @param path the path to the repository
   * @return the repository object
   * @throws IOException if an I/O error occurs
   */
  public Repository establishLocalEndpoint(String path) throws IOException {
    return gitFetchUtils.establishLocalEndpoint(path);
  }

  /**
   * Wrapper of {@link GitFetchUtils#fetchRemoteDifferences(Repository, String)} fetch the
   * differences between the local repository (established from {@link
   * #establishLocalEndpoint(String)} and remote repository.
   *
   * @param repo the repository object established by {@link #establishLocalEndpoint(String)}
   * @param branch the branch name to compare to the local repository
   * @return the list of differences
   * @throws Exception if an error from {@link GitFetchUtils#fetchRemoteDifferences(Repository,
   *     String)}
   */
  public List<DiffEntry> fetchRemoteDifferences(Repository repo, String branch) throws Exception {
    return gitFetchUtils.fetchRemoteDifferences(repo, branch);
  }

  /**
   * Process the differences between the local and remote repository and write the differences to a
   * file. Differences can be generated from {@link #fetchRemoteDifferences(Repository, String)}
   *
   * @param path the path to the microservice TLD
   * @param repo the repository object established by {@link #establishLocalEndpoint(String)}
   * @param diffEntries the list of differences extracted from {@link
   *     #fetchRemoteDifferences(Repository, String)}
   * @throws IOException if an I/O error occurs
   */
  public void processDifferences(String path, Repository repo, List<DiffEntry> diffEntries)
      throws IOException {
    JsonArrayBuilder outputBuilder = Json.createArrayBuilder();

    // process each difference
    for (DiffEntry entry : diffEntries) {
      // skip non-Java files
      if (!entry.getNewPath().endsWith(".java")) {
        continue;
      }

      // String changeURL = gitFetchUtils.getGithubFileUrl(repo, entry);
      System.out.println("Extracting changes from: " + path);

      String oldPath = path + "/" + entry.getOldPath();
      String newPath = path + "/" + entry.getNewPath();

      javax.json.JsonObject deltaChanges;

      switch (entry.getChangeType()) {
        case MODIFY:
          // fetch changed file
          // String fileContents = gitFetchUtils.fetchAndDecodeFile(changeURL);

          // compare differences with local path
          deltaChanges = comparisonUtils.extractDeltaChanges(oldPath);

          // no changes found (likely an extra tail line not shown remotely)
          if (deltaChanges.isEmpty()) {
            continue;
          }

          break;
        case DELETE:
        case COPY:
        case RENAME:
        case ADD:
        default:
          deltaChanges = Json.createObjectBuilder().build();
          break;
      }

      System.out.println(
          "Change impact of type " + entry.getChangeType() + " detected in " + entry.getNewPath());

      JsonObjectBuilder jout = Json.createObjectBuilder();
      jout.add("localPath", newPath);
      jout.add("changeType", entry.getChangeType().name());
      jout.add("commitId", entry.getNewId().name());
      jout.add("changes", deltaChanges);

      outputBuilder.add(jout.build());
    }

    // write differences to output file
    String outputName = "./out/delta-changes-[" + (new Date()).getTime() + "].json";
    MsJsonWriter.writeJsonToFile(outputBuilder.build(), outputName);

    System.out.println("Delta extracted: " + outputName);
  }
}
