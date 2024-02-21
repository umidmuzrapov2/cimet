package edu.university.ecs.lab.intermediate.services;

import edu.university.ecs.lab.common.config.Microservice;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.*;

@Data
@AllArgsConstructor
public class GitCloneService {
  private final String clonePath;

  /**
   * This method clones remote repositories to the local file system
   *
   * @param microservices the microservices to be cloned
   * @throws Exception if Git clone failed
   */
  public List<String> cloneRemotes(Microservice[] microservices) throws Exception {
    List<String> repoNames = new ArrayList<>();

    for (Microservice ms : microservices) {
      String output = clonePath + File.separator + getRepositoryName(ms.getRepoUrl());
      ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", ms.getRepoUrl(), output);
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();

      int exitCode = process.waitFor();

      if (exitCode < 400) {
        System.out.println("Git clone of " + ms.getRepoUrl() + " successful ");

        if (ms.getBaseCommit() != null && !ms.getBaseCommit().isEmpty()) {
          processBuilder = new ProcessBuilder("git", "reset", "--hard", ms.getBaseCommit());
          processBuilder.directory(new File(output));
          processBuilder.redirectErrorStream(true);
          process = processBuilder.start();

          exitCode = process.waitFor();

          // TODO exit code not working
          if (exitCode < 400) {
            System.out.println("Git reset of " + ms.getRepoUrl() + " successful ");
          } else {
            throw new Exception(
                "Git reset of " + ms.getRepoUrl() + " failed with status code: " + exitCode);
          }
        }
      } else {
        throw new Exception(
            "Git clone of " + ms.getRepoUrl() + " failed with status code: " + exitCode);
      }

      output = output.replaceAll("\\\\", "/");

      // add microservices to path
      if (ms.getPaths() != null && ms.getPaths().length > 0) {
        for (String subPath : ms.getPaths()) {
          String path;
          if (subPath.substring(0, 1).equals(File.separator)) {
            path = output + subPath;
          } else {
            path = output + File.separator + subPath;
          }

          File f = new File(path);
          if (f.isDirectory()) {
            repoNames.add(path);
          }
        }
      } else {
        repoNames.add(output);
      }
    }

    return repoNames;
  }

  /**
   * This method parses a repository url and extracts the repository name
   *
   * @param repositoryUrl the repository url to parsing
   * @return the repository name
   */
  private String getRepositoryName(String repositoryUrl) {
    System.out.println("Extracting repo from url: " + repositoryUrl);

    // Extract repository name from the URL
    int lastSlashIndex = repositoryUrl.lastIndexOf('/');
    int lastDotIndex = repositoryUrl.lastIndexOf('.');
    return repositoryUrl.substring(lastSlashIndex + 1, lastDotIndex);
  }
}
