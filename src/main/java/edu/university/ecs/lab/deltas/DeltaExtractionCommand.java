package edu.university.ecs.lab.deltas;

import edu.university.ecs.lab.deltas.services.DeltaExtractionService;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import java.util.*;

public class DeltaExtractionCommand {
  /**
   * main method entry point to delta extraction
   *
   * @param args command line args list containing /path/to/repo(s)
   */
  public static void main(String[] args) throws Exception {
    DeltaExtractionService extractionService = new DeltaExtractionService();

    // iterate through each repository path
    for (String path : args) {
      // point to local repository
      Repository localRepo = extractionService.establishLocalEndpoint(path);

      // extract remote differences with local
      List<DiffEntry> differences = extractionService.fetchRemoteDifferences(localRepo, "main");

      // process/write differences to delta output
      extractionService.processDifferences(path, localRepo, differences);
    }
  }
}
