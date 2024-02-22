package edu.university.ecs.lab.deltas.utils;

import org.eclipse.jgit.diff.DiffEntry;

import javax.json.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/** Utility class for comparing differences between two files. */
public class DeltaComparisonUtils {

  /**
   * Get the difference between two lines in string representation.
   * @param line1 original line
   * @param line2 new line
   * @return the difference between the two lines
   */
  private static String getExactDifference(String line1, String line2) {
    int minLength = Math.min(line1.length(), line2.length());
    int diffIndex = -1;

    for (int i = 0; i < minLength; i++) {
      if (line1.charAt(i) != line2.charAt(i)) {
        diffIndex = i;
        break;
      }
    }

    if (diffIndex == -1) {
      if (line1.length() != line2.length()) {
        return "Lengths differ";
      } else {
        return "Unknown difference";
      }
    } else {
      return "'" + line1.substring(diffIndex) + "' vs '" + line2.substring(diffIndex) + "'";
    }
  }

    /**
     * Extract the differences between the decoded file from {@link GitFetchUtils#fetchAndDecodeFile(String)} and
     * the local file (serviceTLD/{@link DiffEntry#getOldPath()}).
     * @param decodedFile the decoded file JSON
     * @param pathToLocal the path to the local file (serviceTLD/{@link DiffEntry#getOldPath()})
     * @return the differences between the two files as a JSON array
     * @throws IOException if an I/O error occurs
     */
  public JsonArray extractDeltaChanges(String decodedFile, String pathToLocal) throws IOException {
    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

    BufferedReader reader = new BufferedReader(new FileReader(pathToLocal));
    String line;
    int i = 0;

    String[] decodedLines = decodedFile.split("\n");

    while ((line = reader.readLine()) != null) {
      // record each line-by-line difference
      if (i < decodedLines.length && !line.equals(decodedLines[i])) {
        JsonObjectBuilder differenceBuilder =
            Json.createObjectBuilder()
                .add("line-index", i)
                .add("exact", getExactDifference(line, decodedLines[i]));

        jsonArrayBuilder.add(differenceBuilder);
      }

      i++;
    }

    return jsonArrayBuilder.build();
  }
}
