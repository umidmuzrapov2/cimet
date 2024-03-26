package edu.university.ecs.lab.deltas;

import edu.university.ecs.lab.deltas.services.DeltaExtractionService;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import java.util.*;

/** Service for extracting the differences between a local and remote repository. */
public class DeltaExtraction {
  /**
   * main method entry point to delta extraction
   *
   * @param args command line args list containing /path/to/repo(s)
   */
  public static void main(String[] args) throws Exception {
    DeltaExtractionService deltaService = new DeltaExtractionService();

    // iterate through each repository path
    for (String path : args) {
      // point to local repository
      Repository localRepo = deltaService.establishLocalEndpoint(path);

      // extract remote differences with local
      List<DiffEntry> differences = deltaService.fetchRemoteDifferences(localRepo, "master");

      // process/write differences to delta output
      deltaService.processDifferences(path, localRepo, differences);

      // close repository after use
      localRepo.close();
    }
  }
}
