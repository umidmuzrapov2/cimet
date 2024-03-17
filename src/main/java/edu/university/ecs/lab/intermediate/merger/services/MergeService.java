package edu.university.ecs.lab.intermediate.merger.services;

import edu.university.ecs.lab.common.models.rest.*;
import edu.university.ecs.lab.rest.calls.models.MsModel;
import edu.university.ecs.lab.rest.calls.services.RestModelService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MergeService {
  public MsModel extractNewModel(String path) {
    List<RestController> restControllers = new ArrayList<>();
    List<RestService> restServices = new ArrayList<>();
    List<RestDTO> restDTOs = new ArrayList<>();
    List<RestRepository> restRepositories = new ArrayList<>();
    List<RestEntity> restEntities = new ArrayList<>();
    List<RestCall> restCalls = new ArrayList<>();

    File localFile = new File(path);

    RestModelService.scanFile(
            localFile,
            restControllers,
            restServices,
            restDTOs,
            restRepositories,
            restEntities,
            restCalls);

    if (restControllers.isEmpty() && restServices.isEmpty() && restDTOs.isEmpty()
            && restRepositories.isEmpty() && restEntities.isEmpty() && restCalls.isEmpty()) {
      return null;
    }

    MsModel model = new MsModel();
    model.setRestControllers(restControllers);
    model.setRestServices(restServices);
    model.setRestDTOs(restDTOs);
    model.setRestRepositories(restRepositories);
    model.setRestEntities(restEntities);
    model.setRestCalls(restCalls);

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
