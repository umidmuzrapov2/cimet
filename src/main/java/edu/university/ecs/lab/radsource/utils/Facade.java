package edu.university.ecs.lab.radsource.utils;

import edu.university.ecs.lab.radsource.mscontext.MsModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Facade {
  /**
   * Uses the RAD source analyzer to get an MsModel from a directory
   * @param path Path to the ms roots
   * @return MsModel of the microservice communication
   */
  public static MsModel getMsModel(String path) throws IOException {
    // get a parser instance
    SourceParser parser = new SourceParser();

    // get the full paths to the microservice directories
    // List<String> msPaths = Arrays.asList(DirectoryUtils.getMsPaths(path)).stream().map(ms -> path + "/" + ms).collect(Collectors.toList());
    List<String> msPaths = Arrays.asList(DirectoryUtils.getMsFullPaths(path));

    // get the microservice communication model
    return parser.createMsModel(msPaths);
  }

  public static MsModel getMsModel(List<String> msFullPaths) throws IOException {
    SourceParser parser = new SourceParser();
    return parser.createMsModel(msFullPaths);
  }
}
