package edu.university.ecs.lab.deltas.utils;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.jgit.diff.DiffEntry;

import javax.json.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
   * @param decodedFile the decoded file JSON
   * @param pathToLocal the path to the local file (serviceTLD/{@link DiffEntry#getOldPath()})
   * @return the differences between the two files as a JSON array
   * @throws IOException if an I/O error occurs
   */
  public JsonArray extractDeltaChanges(String decodedFile, String pathToLocal) throws IOException {
    JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

    File localFile = new File(pathToLocal);

    CompilationUnit localCu = StaticJavaParser.parse(localFile);

    List<ChangeInformation> changedLines = findChangedLines(decodedFile.split("\n"), localFile);

    // iterate through local file class and methods
    for (ClassOrInterfaceDeclaration localCid : localCu.findAll(ClassOrInterfaceDeclaration.class)) {
      String className = localCid.getNameAsString();

      // iterate through method declarations
      for (MethodDeclaration localMd : localCid.findAll(MethodDeclaration.class)) {
        String methodName = localMd.getNameAsString();

        // get method body statements
        List<Statement> localStatements = Objects.requireNonNull(localMd.getBody().orElse(null)).getStatements();

        for (Statement statement : localStatements) {
          for (ChangeInformation ci : changedLines) {
            Statement checkAgainstStatement;

            try {
              checkAgainstStatement = StaticJavaParser.parseStatement(ci.getLocalLine());
            } catch (ParseProblemException e) {
              continue;
            }

            // statement matches local line statement?
            if (checkAgainstStatement.equals(statement)) {
              JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();

              jsonObjectBuilder.add("className", className);
              jsonObjectBuilder.add("methodName", methodName);
              jsonObjectBuilder.add("remote-line", ci.getRemoteLine());
              jsonObjectBuilder.add("local-line", ci.getLocalLine());
              jsonObjectBuilder.add("line-index", ci.getLineIndex());

              jsonArrayBuilder.add(jsonObjectBuilder.build());
            }
          }
        }
      }
    }

    return jsonArrayBuilder.build();
  }

  /**
   * Extract changed lines between the remote and local file
   *
   * @param remoteFile decoded file
   * @param localFile local file object
   * @return list of local file changed lines
   * @throws IOException if an I/O error occurs
   */
  private List<ChangeInformation> findChangedLines(String[] remoteFile, File localFile) throws IOException {
    List<ChangeInformation> changedLines = new ArrayList<>();

    BufferedReader reader = new BufferedReader(new FileReader(localFile));
    String line;
    int i = 0;

    while ((line = reader.readLine()) != null) {
      // record each line-by-line difference
      if (i < remoteFile.length && !line.equals(remoteFile[i])) {
        changedLines.add(new ChangeInformation(line, remoteFile[i], i));
      }

      i++;
    }

    return changedLines;
  }
}

@Data
@AllArgsConstructor
class ChangeInformation {
  private String localLine;
  private String remoteLine;
  private int lineIndex;
}