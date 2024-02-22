package edu.university.ecs.lab.intermediate.services;

import edu.university.ecs.lab.common.models.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Service for extracting REST endpoints and dependencies for a given microservice. */
// TODO probably rename this to something like "RestModelService" or something
public class RepositoryService {
  /** Service for rest endpoints from services */
  private final EndpointExtractionService endpointExtractionService =
      new EndpointExtractionService();

  /** Service for extracting rest calls to other services */
  private final CallExtractionService callExtractionService = new CallExtractionService();

  /**
   * Recursively scan the files in the given repository path and extract the endpoints and
   * dependencies for a single microservice.
   *
   * @param rootPath root path to cloned repository folder
   * @param pathToMs the path to the microservice TLD
   * @return model of a single service containing the extracted endpoints and dependencies
   */
  public MsModel recursivelyScanFiles(String rootPath, String pathToMs) {
    String repoPath = rootPath + pathToMs;
    System.out.println("Scanning repository '" + repoPath + "'...");
    MsModel model = new MsModel();

    List<Endpoint> endpoints = new ArrayList<>();
    List<RestDependency> calls = new ArrayList<>();

    File localDir = new File(repoPath);
    if (!localDir.exists() || !localDir.isDirectory()) {
      System.err.println("Invalid path given: " + repoPath);
      return null;
    }

    scanDirectory(localDir, endpoints, calls);

    // scan for internal dependency destinations
    scanInternalDependencies(calls, endpoints);

    model.setEndpoints(endpoints);
    model.setDependencies(calls);

    System.out.println("Done!");
    return model;
  }

  /**
   * Recursively scan the given directory for files and extract the endpoints and dependencies.
   *
   * @param directory the directory to scan
   * @param endpoints the list of endpoints
   * @param calls the list of calls to other services
   */
  private void scanDirectory(File directory, List<Endpoint> endpoints, List<RestDependency> calls) {
    File[] files = directory.listFiles();

    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          scanDirectory(file, endpoints, calls);
        } else if (file.getName().endsWith(".java")) {
          scanFile(file, endpoints, calls);
        }
      }
    }
  }

  /**
   * Scan the given file for endpoints and calls to other services.
   *
   * @param file the file to scan
   * @param endpoints the list of endpoints
   * @param calls the list of calls to other services
   */
  private void scanFile(File file, List<Endpoint> endpoints, List<RestDependency> calls) {
    try {
      List<Endpoint> fileEndpoints = endpointExtractionService.parseEndpoints(file);
      endpoints.addAll(fileEndpoints);
    } catch (IOException e) {
      System.err.println("Could not extract endpoints from file: " + file.getName());
    }

    try {
      List<RestDependency> fileDependencies = callExtractionService.parseCalls(file);
      calls.addAll(fileDependencies);
    } catch (IOException e) {
      System.err.println("Could not extract calls from file: " + file.getName());
    }
  }

  // TODO internal dependencies does not make sense for our purposes, probably
  // can remove, we likely want to only refer to "dependencies" as items between two
  // services, not within
  private void scanInternalDependencies(
      List<RestDependency> dependencies, List<Endpoint> endpoints) {
    for (RestDependency restDependency : dependencies) {
      String url = restDependency.getUrl();

      for (Endpoint endpoint : endpoints) {
        if (endpoint.getUrl().contains(url)) {
          restDependency.setDestFile(endpoint.getSourceFile());
          break;
        }
      }

      if (restDependency.getDestFile() == null) {
        restDependency.setDestFile("UNKNOWN");
      }
    }
  }
}
