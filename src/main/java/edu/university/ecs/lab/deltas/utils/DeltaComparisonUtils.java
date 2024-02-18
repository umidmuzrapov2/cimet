package edu.university.ecs.lab.deltas.utils;

import javax.json.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DeltaComparisonUtils {
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
      return  "'" + line1.substring(diffIndex) + "' vs '" + line2.substring(diffIndex) + "'";
    }
  }


  public JsonArray extractDeltaChanges(String decodedFile, String pathToLocal) throws IOException {
    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

    BufferedReader reader = new BufferedReader(new FileReader(pathToLocal));
    String line;
    int i = 0;

    String[] decodedLines = decodedFile.split("\n");

    while ((line = reader.readLine()) != null) {
      // record each line-by-line difference
      if (i < decodedLines.length && !line.equals(decodedLines[i])) {
        JsonObjectBuilder differenceBuilder = Json.createObjectBuilder()
                .add("line-index", i)
                .add("exact", getExactDifference(line, decodedLines[i]));

        jsonArrayBuilder.add(differenceBuilder);
      }

      i++;
    }

    return jsonArrayBuilder.build();
  }
}
