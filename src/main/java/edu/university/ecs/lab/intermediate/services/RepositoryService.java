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
  public MsModel recursivelyScanFiles(String clonePath, String relativePath) {
    String repoPath = clonePath + relativePath;
    System.out.println("Scanning repository '" + repoPath + "'...");
    MsModel model = new MsModel();

    List<Endpoint> endpoints = new ArrayList<>();
    List<Dependency> dependencies = new ArrayList<>();
    List<Dependency> externalDependencies = new ArrayList<>();

    File localDir = new File(repoPath);
    if (!localDir.exists() || !localDir.isDirectory()) {
      System.err.println("Invalid path given: " + repoPath);
      return null;
    }

    scanDirectory(localDir, endpoints, dependencies);

    int i = 0;
    for (Dependency dependency : dependencies) {
      // dest file was not recursively found (or added yet)
      if (dependency.getDestFile().equals("UNKNOWN")) {
        dependency.setDestFile(scanForDestination(dependency.getUrl(), endpoints));
      }

      if (!dependency.getSourceFile().contains(repoPath)) {
        externalDependencies.add(dependency);
      }
    }

    model.setEndpoints(endpoints);
    model.setDependencies(dependencies);

    System.out.println("Done!");
    return model;
  }

  private static void scanDirectory(
      File directory, List<Endpoint> endpoints, List<Dependency> dependencies) {
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

  private static void scanFile(File file, List<Endpoint> endpoints, List<Dependency> dependencies) {
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      String fileName = file.getPath().replaceAll("\\\\", "/");

      while ((line = reader.readLine()) != null) {
        if (line.startsWith("//")) {
          continue;
        }

        String httpMethod = "";
        String decAnnotation;
        // scan for rest declarations (endpoints)
        for (RestDeclarationAnnotation declarationAnnotation : RestDeclarationAnnotation.values()) {
          decAnnotation = declarationAnnotation.getAnnotation();
          switch (decAnnotation) {
            case "@GetMapping":
              httpMethod = "GET";
              break;
            case "@PostMapping":
              httpMethod = "POST";
              break;
            case "@DeleteMapping":
              httpMethod = "DELETE";
              break;
            case "@PutMapping":
              httpMethod = "PUT";
              break;
            case "@RequestMapping":
              if (decAnnotation.contains("RequestMethod.POST")) {
                httpMethod = "POST";
              } else if (decAnnotation.contains("RequestMethod.DELETE")) {
                httpMethod = "DELETE";
              } else if (decAnnotation.contains("RequestMethod.PUT")) {
                httpMethod = "PUT";
              } else {
                httpMethod = "GET";
              }
              break;
          }
          String url = extractAnnotationAPI(line, decAnnotation);
          if (url == null) {
            continue;
          }

          // url = trimUrlApi(url);
          addEndpoint(endpoints, url, fileName, declarationAnnotation.getAnnotation(), httpMethod);
        }

        // scan for rest declarations (endpoints)
        for (RestCallAnnotation callAnnotation : RestCallAnnotation.values()) {

          String url = extractAnnotationAPI(line, callAnnotation.getAnnotation());
          if (url == null) {
            continue;
          }

          // url = trimUrlApi(url);
          addDependency(dependencies, url, fileName, endpoints, callAnnotation.getAnnotation());
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

  private static String trimUrlApi(String api) {
    String commonPrefix = "/api";

    if (api.startsWith(commonPrefix)) {
      api = api.substring(commonPrefix.length());
    }

    // check any version number (ex: /v1, /v2, ...) following
    Pattern pattern = Pattern.compile("^/v[0-9]+");
    Matcher matcher = pattern.matcher(api);

    if (matcher.find()) {
      return api.substring(matcher.end());
    }

    return api;
  }

  private static void addEndpoint(
      List<Endpoint> endpoints,
      String url,
      String sourceFile,
      String restAnnotiation,
      String httpMethod) {
    if (url == null) {
      url = "";
    }

    if (sourceFile.contains(IntermediateExtraction.clonePath) && sourceFile.length() > IntermediateExtraction.clonePath.length() + 1) {
      sourceFile = sourceFile.substring(IntermediateExtraction.clonePath.length() + 1);
    }

    endpoints.add(new Endpoint(url, sourceFile, restAnnotiation, httpMethod));
  }

  private static void addDependency(
      List<Dependency> dependencies,
      String url,
      String sourceFile,
      List<Endpoint> endpoints,
      String restAnnotation) {
    if (url == null) {
      url = "";
    }

    // cut off ending '/' if exists
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }

    // cut source file
    if (sourceFile.contains(IntermediateExtraction.clonePath) && sourceFile.length() > IntermediateExtraction.clonePath.length() + 1) {
      sourceFile = sourceFile.substring(IntermediateExtraction.clonePath.length() + 1);
    }

    // search for source file in endpoints list

    String destFile = scanForDestination(url, endpoints);

    if (destFile.contains(IntermediateExtraction.clonePath) && destFile.length() > IntermediateExtraction.clonePath.length() + 1) {
      destFile = destFile.substring(IntermediateExtraction.clonePath.length() + 1);
    }

    dependencies.add(
        new Dependency(url, sourceFile, destFile, restAnnotation));
  }

  private static String scanForDestination(String url, List<Endpoint> endpoints) {
    String destFile = "UNKNOWN";

    // search for source file in endpoints list
    for (Endpoint endpoint : endpoints) {
      if (endpoint.getUrl().contains(url)) {
        destFile = endpoint.getSourceFile();
        break;
      }
    }

    return destFile;
  }
}
