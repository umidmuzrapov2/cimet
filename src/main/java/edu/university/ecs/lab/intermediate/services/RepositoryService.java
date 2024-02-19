package edu.university.ecs.lab.intermediate.services;

import edu.university.ecs.lab.common.models.Endpoint;
import edu.university.ecs.lab.common.models.MsModel;
import edu.university.ecs.lab.common.models.RestCallAnnotation;
import edu.university.ecs.lab.common.models.RestDeclarationAnnotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RepositoryService {
  public MsModel recursivelyScanFiles(String repoPath) {
    System.out.println("Scanning repository '" + repoPath + "'...");
    MsModel model = new MsModel();

    List<Endpoint> endpoints = new ArrayList<>();
    List<Endpoint> dependencies = new ArrayList<>();

    File localDir = new File(repoPath);
    if (!localDir.exists() || !localDir.isDirectory()) {
      System.err.println("Invalid path given: "  + repoPath);
      return null;
    }

    scanDirectory(localDir, endpoints, dependencies);
    model.setEndpoints(endpoints);
    model.setDependencies(dependencies);

    System.out.println("Done!");
    return model;
  }

  private static void scanDirectory(File directory, List<Endpoint> endpoints, List<Endpoint> dependencies) {
    File[] files = directory.listFiles();

    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          scanDirectory(file, endpoints, dependencies);
        } else if (file.getName().endsWith(".java")) {
          findEndpointsInFile(file, endpoints, dependencies);
        }
      }
    }
  }

  private static void findEndpointsInFile(File file, List<Endpoint> endpoints, List<Endpoint> dependencies) {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        // scan for rest declarations (endpoints)
        for (RestDeclarationAnnotation annotation : RestDeclarationAnnotation.values()) {
          String url = extractAnnotationAPI(line, annotation.getAnnotation());
          if (url == null) {
            continue;
          }

          addEndpoint(endpoints, url, file.getPath().replaceAll("\\\\", "/"));
        }

        // scan for rest declarations (endpoints)
        for (RestCallAnnotation annotation : RestCallAnnotation.values()) {
          String url = extractAnnotationAPI(line, annotation.getAnnotation());
          if (url == null) {
            continue;
          }

          addDependency(dependencies, url, endpoints);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String extractAnnotationAPI(String line, String annotation) {
    int callIndex = line.indexOf(annotation);

    if (callIndex < 0) {
      return null;
    }

    String lineCpy = line.substring(callIndex);

    int apiIndex = lineCpy.indexOf("/");
    if (apiIndex < 0) {
      return null;
    }
    lineCpy = lineCpy.substring(apiIndex);

    int endQuoteIndex = lineCpy.indexOf("\"");
    if (endQuoteIndex < 0) {
      return null;
    }

    return lineCpy.substring(0, endQuoteIndex);
  }

  private static void addEndpoint(List<Endpoint> endpoints, String url, String sourceFile) {
    if (url == null) {
      url = "";
    }

    endpoints.add(new Endpoint(url, sourceFile));
  }

  private static void addDependency(List<Endpoint> dependencies, String url, List<Endpoint> endpoints) {
    if (url == null) {
      url = "";
    }

    String sourceFile = "";

    // search for source file in endpoints list
    for (Endpoint endpoint : endpoints) {
      if (endpoint.getUrl().contains(url)) {
        sourceFile = endpoint.getSourceFile();
        break;
      }
    }

    dependencies.add(new Endpoint(url, sourceFile));
  }
}
