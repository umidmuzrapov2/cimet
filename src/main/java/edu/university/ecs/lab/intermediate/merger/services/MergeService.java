package edu.university.ecs.lab.intermediate.merger.services;

import edu.university.ecs.lab.common.models.*;
import edu.university.ecs.lab.rest.calls.services.RestModelService;
import java.io.File;
import java.util.List;
import java.util.Objects;

public class MergeService {
  public MsModel extractNewModel(String path) {

    File localFile = new File(path);

    JClass jClass = RestModelService.scanFile(localFile);

    // TODO Idk why this check is here but I copied the logic anyways
    if(Objects.isNull(jClass)) {
      return null;
    }

    MsModel model = new MsModel();
    model.setClassList(List.of(jClass));

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
