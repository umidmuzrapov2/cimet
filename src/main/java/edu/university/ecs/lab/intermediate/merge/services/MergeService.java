package edu.university.ecs.lab.intermediate.merge.services;

import edu.university.ecs.lab.common.models.*;
import edu.university.ecs.lab.intermediate.create.services.RestModelService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MergeService {
  public MsModel extractNewModel(String path) {
    List<JController> controllers = new ArrayList<>();
    List<JService> services = new ArrayList<>();
    List<JClass> dtos = new ArrayList<>();
    List<JClass> repositories = new ArrayList<>();
    List<JClass> entities = new ArrayList<>();

    File localFile = new File(path);

    RestModelService.scanFile(localFile, controllers, services, dtos, repositories, entities);

    if (controllers.isEmpty()
        && services.isEmpty()
        && dtos.isEmpty()
        && repositories.isEmpty()
        && entities.isEmpty()) {
      return null;
    }

    MsModel model = new MsModel();
    model.setControllers(controllers);
    model.setServices(services);
    model.setDtos(dtos);
    model.setRepositories(repositories);
    model.setEntities(entities);

    return model;
  }

  public String incrementVersion(String version) {
    // split version by '.'
    String[] parts = version.split("\\.");

    // cast version string parts to integer
    int[] versionParts = new int[parts.length];
    for (int i = 0; i < parts.length; i++) {
      versionParts[i] = Integer.parseInt(parts[i]);
    }

    // increment end digit
    versionParts[versionParts.length - 1]++;

    // end digit > 9? increment middle and reset end digit to 0
    if (versionParts[versionParts.length - 1] == 10) {
      versionParts[versionParts.length - 1] = 0;
      versionParts[versionParts.length - 2]++;

      // middle digit > 9, increment start digit (major version) and reset middle to 0
      if (versionParts[versionParts.length - 2] == 10) {
        versionParts[versionParts.length - 2] = 0;
        versionParts[0]++;
      }
    }

    StringBuilder newVersion = new StringBuilder();
    for (int i = 0; i < versionParts.length; i++) {
      newVersion.append(versionParts[i]);
      if (i < versionParts.length - 1) {
        newVersion.append('.');
      }
    }

    return newVersion.toString();
  }
}
