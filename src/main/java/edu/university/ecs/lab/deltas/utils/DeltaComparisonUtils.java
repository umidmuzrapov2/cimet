package edu.university.ecs.lab.deltas.utils;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.Statement;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.common.utils.MsFileUtils;
import edu.university.ecs.lab.deltas.models.ChangeInformation;
import edu.university.ecs.lab.rest.calls.services.RestModelService;
import org.eclipse.jgit.diff.DiffEntry;

import javax.json.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Utility class for comparing differences between two files. */
public class DeltaComparisonUtils {

  /**
   * Get the difference between two lines in string representation.
   *
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
   * Extract the differences between the decoded file from {@link
   * GitFetchUtils#fetchAndDecodeFile(String)} and the local file (serviceTLD/{@link
   * DiffEntry#getOldPath()}).
   *
   * @param pathToLocal the path to the local file (serviceTLD/{@link DiffEntry#getOldPath()})
   * @return the differences between the two files as a JSON array
   * @throws IOException if an I/O error occurs
   */
  public JsonObject extractDeltaChanges(String pathToLocal) {
    JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

    File localFile = new File(pathToLocal);

    JClass jClass = RestModelService.scanFile(localFile);

    if(jClass.getRole() == ClassRole.CONTROLLER) {
      jsonObjectBuilder.add("restControllers", MsFileUtils.buildRestControllers("", List.of(jClass)));
    } else if(jClass.getRole() == ClassRole.SERVICE) {
      jsonObjectBuilder.add("services", MsFileUtils.buildJavaClass(List.of(jClass)));
      jsonObjectBuilder.add("restCalls", MsFileUtils.buildRestCalls(jClass.getRestCalls()));
    } else if(jClass.getRole() == ClassRole.REPOSITORY) {
      jsonObjectBuilder.add("repositories", MsFileUtils.buildJavaClass(List.of(jClass)));
    } else if(jClass.getRole() == ClassRole.ENTITY) {
      jsonObjectBuilder.add("entities", MsFileUtils.buildJavaClass(List.of(jClass)));
    } else if(jClass.getRole() == ClassRole.DTO) {
      jsonObjectBuilder.add("dtos", MsFileUtils.buildJavaClass(List.of(jClass)));
    }

    return jsonObjectBuilder.build();
  }

  /**
   * Recursively determine if local statement contains or is equal to target (change)
   *
   * @param localStatement method statement potentially encompassing change
   * @param changeStatement changed statement to search for
   * @return true if provided local statement contains the change, false otherwise
   */
  private static boolean containsStatement(Statement localStatement, Statement changeStatement) {
    if (localStatement == null || changeStatement == null) {
      return false;
    }

    // check equality of current statement point
    if (StatementEqualityUtils.checkEquality(localStatement, changeStatement)) {
      return true;
    }

    for (Node child : localStatement.getChildNodes()) {
      // skip non-statement nodes (like comments and blank lines?)
      if (!(child instanceof Statement)) {
        continue;
      }

      // recursively check child statements
      if (containsStatement((Statement) child, changeStatement)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Extract changed lines between the remote and local file
   *
   * @param remoteFile decoded file
   * @param localFile local file object
   * @return list of local file changed lines
   * @throws IOException if an I/O error occurs
   */
  private List<ChangeInformation> findChangedLines(String[] remoteFile, File localFile)
      throws IOException {
    List<ChangeInformation> changedLines = new ArrayList<>();

    BufferedReader reader = new BufferedReader(new FileReader(localFile));
    String line;
    int i = 0;

    while ((line = reader.readLine()) != null) {
      // record each line-by-line difference
      if (i < remoteFile.length && !line.equals(remoteFile[i])) {
        changedLines.add(new ChangeInformation(line, remoteFile[i], i + 1));
      }

      i++;
    }

    return changedLines;
  }
}
