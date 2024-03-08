package edu.university.ecs.lab.rest.calls.services;

import edu.university.ecs.lab.common.models.rest.*;
import edu.university.ecs.lab.rest.calls.models.*;
import edu.university.ecs.lab.rest.calls.parsers.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Service for extracting REST endpoints and dependencies for a given microservice. */
public class RestModelService {
  /**
   * Recursively scan the files in the given repository path and extract the endpoints and
   * dependencies for a single microservice.
   *
   * @param rootPath root path to cloned repository folder
   * @param pathToMs the path to the microservice TLD
   * @return model of a single service containing the extracted endpoints and dependencies
   */
  public static MsModel recursivelyScanFiles(String rootPath, String pathToMs) {
    String repoPath = rootPath + pathToMs;
    System.out.println("Scanning repository '" + repoPath + "'...");
    MsModel model = new MsModel();

    List<RestEndpoint> restEndpoints = new ArrayList<>();
    List<RestService> restServices = new ArrayList<>();
    List<RestDTO> restDTOs = new ArrayList<>();
    List<RestRepository> restRepositories = new ArrayList<>();
    List<RestEntity> restEntities = new ArrayList<>();

    List<RestCall> calls = new ArrayList<>();

    File localDir = new File(repoPath);
    if (!localDir.exists() || !localDir.isDirectory()) {
      System.err.println("Invalid path given: " + repoPath);
      return null;
    }

    scanDirectory(localDir, restEndpoints, restServices, restDTOs, restRepositories, restEntities, calls);

    model.setRestEndpoints(restEndpoints);
    model.setRestServices(restServices);
    model.setRestDTOs(restDTOs);
    model.setRestRepositories(restRepositories);
    model.setRestEntities(restEntities);
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
  public static void scanDirectory(File directory, List<RestEndpoint> restEndpoints, List<RestService> restServices,
                             List<RestDTO> restDTOs, List<RestRepository> restRepos, List<RestEntity> restEntities,
                             List<RestCall> calls) {
    File[] files = directory.listFiles();

    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          scanDirectory(file, restEndpoints, restServices, restDTOs, restRepos, restEntities, calls);
        } else if (file.getName().endsWith(".java")) {
          scanFile(file, restEndpoints, restServices, restDTOs, restRepos, restEntities, calls);
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
  public static void scanFile(File file, List<RestEndpoint> restEndpoints, List<RestService> restServices,
                        List<RestDTO> restDTOs, List<RestRepository> restRepos, List<RestEntity> restEntities,
                        List<RestCall> calls) {
    try {
      if (file.getName().contains("Controller")) {
        List<RestEndpoint> fileRestEndpoints = EndpointParser.parseEndpoints(file);
        restEndpoints.addAll(fileRestEndpoints);
      }

      if (file.getName().contains("Service")) {
        List<RestService> fileRestServices = ServiceParser.parseServices(file);
        restServices.addAll(fileRestServices);
      }

      if (file.getName().toLowerCase().contains("dto")) {
        List<RestDTO> fileRestDtos = DTOParser.parseDTOs(file);
        restDTOs.addAll(fileRestDtos);
      }

      if (file.getName().contains("Repository")) {
        List<RestRepository> fileRestRepos = RepositoryParser.parseRepos(file);
        restRepos.addAll(fileRestRepos);
      }

      if (file.getParent().toLowerCase().contains("entity") || file.getParent().toLowerCase().contains("model")) {
        List<RestEntity> fileRestEntities = RepositoryParser.parseEntities(file);
        restEntities.addAll(fileRestEntities);
      }

      // todo: configs? utils? (everything else? -_-)
    } catch (IOException e) {
      System.err.println("Could not extract endpoints from file: " + file.getName());
    }

    try {
      List<RestCall> restCalls = CallParser.parseCalls(file);
      calls.addAll(restCalls);
    } catch (IOException e) {
      System.err.println("Could not extract calls from file: " + file.getName());
    }
  }
}
