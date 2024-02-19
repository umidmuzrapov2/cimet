package edu.university.ecs.lab.intermediate.services;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class GitCloneService {
  private final String clonePath;

  /**
   * This method clones remote repositories to the local file system
   *
   * @param urls the urls of repositories to be cloned
   * @throws Exception if Git clone failed
   */
  public List<String> cloneRemotes(String[] urls) throws Exception {
    List<String> repoNames = new ArrayList<>();

    for (String url : urls) {
      String output = clonePath + File.separator + getRepositoryName(url);
      ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", url, output);
      processBuilder.redirectErrorStream(true);
      Process process = processBuilder.start();

      int exitCode = process.waitFor();

      if (exitCode < 400) {
        System.out.println("Git clone of " + url + " successful ");
      } else {
        throw new Exception("Git clone of " + url + " failed with status code: " + exitCode);
      }

      repoNames.add(output.replaceAll("\\\\", "/"));
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
