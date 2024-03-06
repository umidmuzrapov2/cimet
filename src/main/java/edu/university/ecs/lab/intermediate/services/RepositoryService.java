package edu.university.ecs.lab.intermediate.services;

import edu.university.ecs.lab.common.models.rest.MsModel;
import edu.university.ecs.lab.common.models.rest.RestCall;
import edu.university.ecs.lab.common.models.rest.RestEndpoint;

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

    List<RestEndpoint> restEndpoints = new ArrayList<>();
    List<RestCall> calls = new ArrayList<>();

    File localDir = new File(repoPath);
    if (!localDir.exists() || !localDir.isDirectory()) {
      System.err.println("Invalid path given: " + repoPath);
      return null;
    }

    // todo: find services (not just controllers)
    scanDirectory(localDir, restEndpoints, calls);

    model.setRestEndpoints(restEndpoints);
    model.setRestCalls(calls);

    System.out.println("Done!");
    return model;
  }

  /**
   * Recursively scan the given directory for files and extract the endpoints and dependencies.
   *
   * @param directory the directory to scan
   * @param restEndpoints the list of endpoints
   * @param calls the list of calls to other services
   */
  private void scanDirectory(File directory, List<RestEndpoint> restEndpoints, List<RestCall> calls) {
    File[] files = directory.listFiles();

    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          scanDirectory(file, restEndpoints, calls);
        } else if (file.getName().endsWith(".java")) {
          scanFile(file, restEndpoints, calls);
        }
      }
    }
  }

  /**
   * Scan the given file for endpoints and calls to other services.
   *
   * @param file the file to scan
   * @param restEndpoints the list of endpoints
   * @param calls the list of calls to other services
   */
  private void scanFile(File file, List<RestEndpoint> restEndpoints, List<RestCall> calls) {
    try {
      List<RestEndpoint> fileRestEndpoints = endpointExtractionService.parseEndpoints(file);
      restEndpoints.addAll(fileRestEndpoints);
    } catch (IOException e) {
      System.err.println("Could not extract endpoints from file: " + file.getName());
    }

    try {
      List<RestCall> restCalls = callExtractionService.parseCalls(file);
      calls.addAll(restCalls);
    } catch (IOException e) {
      System.err.println("Could not extract calls from file: " + file.getName());
    }
  }
}
