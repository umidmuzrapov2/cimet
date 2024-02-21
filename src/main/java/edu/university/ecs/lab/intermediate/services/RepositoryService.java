package edu.university.ecs.lab.intermediate.services;

import edu.university.ecs.lab.common.models.*;
import edu.university.ecs.lab.intermediate.IntermediateExtraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepositoryService {
  private final RestEndpointService restEndpointService = new RestEndpointService();
  private final RestDependencyService restDependencyService = new RestDependencyService();

  public MsModel recursivelyScanFiles(String clonePath, String relativePath) {
    String repoPath = clonePath + relativePath;
    System.out.println("Scanning repository '" + repoPath + "'...");
    MsModel model = new MsModel();

    List<Endpoint> endpoints = new ArrayList<>();
    List<Dependency> dependencies = new ArrayList<>();

    File localDir = new File(repoPath);
    if (!localDir.exists() || !localDir.isDirectory()) {
      System.err.println("Invalid path given: " + repoPath);
      return null;
    }

    scanDirectory(localDir, endpoints, dependencies);

    // scan for internal dependency destinations
    scanInternalDependencies(dependencies, endpoints);

    model.setEndpoints(endpoints);
    model.setDependencies(dependencies);

    System.out.println("Done!");
    return model;
  }

  private void scanDirectory(File directory, List<Endpoint> endpoints, List<Dependency> dependencies) {
    File[] files = directory.listFiles();

    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          scanDirectory(file, endpoints, dependencies);
        } else if (file.getName().endsWith(".java")) {
          scanFile(file, endpoints, dependencies);
        }
      }
    }
  }

  private void scanFile(File file, List<Endpoint> endpoints, List<Dependency> dependencies) {
    try {
      List<Endpoint> fileEndpoints = restEndpointService.parseEndpoints(file);
      endpoints.addAll(fileEndpoints);
    } catch (IOException e) {
      System.err.println("Could not extract endpoints from file: " + file.getName());
    }

    try {
      List<Dependency> fileDependencies = restDependencyService.parseDependencies(file);
      dependencies.addAll(fileDependencies);
    } catch (IOException e) {
      System.err.println("Could not extract dependencies from file: " + file.getName());
    }
  }

  private void scanInternalDependencies(List<Dependency> dependencies, List<Endpoint> endpoints) {
    for (Dependency dependency : dependencies) {
      String url = dependency.getUrl();

      for (Endpoint endpoint : endpoints) {
        if (endpoint.getUrl().contains(url)) {
          dependency.setDestFile(endpoint.getSourceFile());
          break;
        }
      }

      if (dependency.getDestFile() == null) {
        dependency.setDestFile("UNKNOWN");
      }
    }
  }
}
