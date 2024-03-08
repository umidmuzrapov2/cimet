package edu.university.ecs.lab.semantics.services;

import edu.university.ecs.lab.common.config.ConfigUtil;
import edu.university.ecs.lab.common.config.InputConfig;
import edu.university.ecs.lab.common.config.InputRepository;
import edu.university.ecs.lab.rest.calls.services.GitCloneService;

import java.io.File;

public class SetupService {

  public static InputConfig loadConfig(String configPath) throws Exception {
    InputConfig config = ConfigUtil.validateConfig(configPath);
    initConfigPaths(config);
    return ConfigUtil.validateConfig(configPath);
  }

  private static void initConfigPaths(InputConfig inputConfig) {
    File outputFilePath = new File(inputConfig.getOutputPath());
    File clonePathFile = new File(inputConfig.getClonePath());

    if (!outputFilePath.exists()) {
      boolean success = outputFilePath.mkdirs();
      if (!success) {
        throw new RuntimeException("Failed to create directory " + outputFilePath);
      }
    }

    if (!clonePathFile.exists()) {
      boolean success = clonePathFile.mkdirs();
      if (!success) {
        throw new RuntimeException("Failed to create directory " + clonePathFile);
      }
    }
  }

  public static void cloneRepositories(InputConfig inputConfig) throws Exception {
    GitCloneService cloneService = new GitCloneService(inputConfig.getClonePath());

    for (InputRepository inputRepository : inputConfig.getRepositories()) {
      cloneService.cloneRemote(inputRepository);
    }
  }
}
