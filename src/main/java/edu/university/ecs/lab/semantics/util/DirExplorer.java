package edu.university.ecs.lab.semantics.util;

import java.io.File;

/**
 * Utility class which provides dynamic implementation through the use of fuctional interfaces
 * allowing the user to filter and handle files however they define
 */
public class DirExplorer {
  /** Functional interface for handling files */
  @FunctionalInterface
  public interface FileHandler {
    void handle(int level, String path, File file);
  }

  /** Fuctional interface for filtering files */
  @FunctionalInterface
  public interface Filter {
    boolean interested(int level, String path, File file);
  }

  private FileHandler fileHandler;
  private Filter filter;

  /**
   * Constructs a valid DirExplorer
   *
   * @param filter the filter used on file's
   * @param fileHandler
   */
  public DirExplorer(Filter filter, FileHandler fileHandler) {
    this.filter = filter;
    this.fileHandler = fileHandler;
  }

  /**
   * This specific implementation is called once and starts file crawl, the overloaded "private void
   * explore(int level, String path, File file)" is used on all subdirectories/files
   *
   * @param root filepath to root, where crawl begins
   */
  public void explore(File root) {
    explore(0, "", root);
  }

  /**
   * Iterative function that utilizes Filter implementation and FileHandler implementation to crawl
   * through and filter/handle files
   *
   * @param level level's down from root (0)
   * @param path relative path from root folder, not including root folder name
   * @param file file object representing the current file
   */
  private void explore(int level, String path, File file) {
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        explore(level + 1, path + "/" + child.getName(), child);
      }
    } else {
      if (filter.interested(level, path, file)) {
        fileHandler.handle(level, path, file);
      }
    }
  }
}
