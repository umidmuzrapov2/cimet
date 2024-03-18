package edu.university.ecs.lab.rest.calls.services;

import edu.university.ecs.lab.common.ParserService;
import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.enums.ClassRole;
import edu.university.ecs.lab.rest.calls.models.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    List<JClass> jClasses = new ArrayList<>();

    File localDir = new File(repoPath);
    if (!localDir.exists() || !localDir.isDirectory()) {
      System.err.println("Invalid path given: " + repoPath);
      return null;
    }

    scanDirectory(localDir, jClasses);
    model.setClassList(jClasses);

    System.out.println("Done!");
    return model;
  }

  /**
   * Recursively scan the given directory for files and extract the endpoints and dependencies.
   *
   * @param directory the directory to scan
   * @param restControllers the list of endpoints
   * @param calls the list of calls to other services
   */
  public static void scanDirectory(
      File directory,
      List<JClass> jClasses) {
    File[] files = directory.listFiles();

    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          scanDirectory(
              file, jClasses);
        } else if (file.getName().endsWith(".java")) {
          JClass jClass = scanFile(file);
          if(Objects.nonNull(jClass)) {
            jClasses.add(jClass);
          }
        }
      }
    }
  }

  /**
   * Scan the given file for endpoints and calls to other services.
   *
   * @param file the file to scan
   * @param restControllers the list of endpoints
   * @param calls the list of calls to other services
   */
  public static JClass scanFile(
      File file) {
    ClassRole role = null;
    try {
      if (file.getName().contains("Controller")) {
        role = ClassRole.CONTROLLER;
      } else if (file.getName().contains("ServiceImpl")) {
        role = ClassRole.SERVICE;
      } else if (file.getName().toLowerCase().contains("dto")) {
        role = ClassRole.REPOSITORY;
      } else if (file.getName().contains("Repository")) {
        role = ClassRole.REPOSITORY;
      } else if (file.getParent().toLowerCase().contains("entity")
          || file.getParent().toLowerCase().contains("model")) {
        role = ClassRole.ENTITY;
      }
      if(role != null) {
        JClass jClass = ParserService.parseClass(file, role);
        if (Objects.nonNull(jClass)) {
          return jClass;
        }
      }


      // todo: configs? utils? (everything else? -_-)
    } catch (IOException e) {
      System.err.println("Could not extract endpoints from file: " + file.getName());
    }
    return null;
  }
}
