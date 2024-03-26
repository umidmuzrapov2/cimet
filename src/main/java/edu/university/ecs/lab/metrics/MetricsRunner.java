package edu.university.ecs.lab.metrics;

import edu.university.ecs.lab.common.models.JClass;
import edu.university.ecs.lab.common.models.MsModel;
import edu.university.ecs.lab.intermediate.merge.models.Change;
import edu.university.ecs.lab.intermediate.merge.models.Delta;
import edu.university.ecs.lab.intermediate.merge.utils.IRParserUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MetricsRunner {
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.err.println(
          "Invalid # of args, 2 expected: <path/to/old/intermediate-json>  <path/to/delta>");
      return;
    }

    Map<String, MsModel> systemMap = IRParserUtils.parseIRSystem(args[0]).getServiceMap();
    List<Delta> deltas = IRParserUtils.parseDelta(args[1]);

    int differences = 0;

    // iterate through delta changes
    for (Delta delta : deltas) {
      System.out.println("Analyzing delta: " + delta.getLocalPath());
      String localPath = delta.getLocalPath();

      switch (delta.getChangeType()) {
        case "ADD":
        case "DELETE":
          differences++;
          break;
        case "MODIFY":
          MsModel currentModel = systemMap.get(localPath);

          // todo
          if (Objects.isNull(currentModel)) {
            continue;
          }

          Change change = delta.getChange();
          differences += findChange(change.getControllers(), currentModel.getControllers());
          differences += findChange(change.getServices(), currentModel.getServices());
          differences += findChange(change.getDtos(), currentModel.getDtos());
          differences += findChange(change.getRepositories(), currentModel.getRepositories());
          differences += findChange(change.getEntities(), currentModel.getEntities());

          break;
      }
    }

    System.out.println(
        "% Changed: "
            + (differences / systemMap.values().stream().mapToInt(MsModel::getModelSize).sum()));
  }

  private static int findChange(
      List<? extends JClass> classList, List<? extends JClass> systemList) {
    int numChanges = 0;

    for (JClass jClass : classList) {
      JClass matchingClass =
          systemList.stream()
              .filter(c -> c.getClassName().equals(jClass.getClassName()))
              .findFirst()
              .orElse(null);

      if (matchingClass == null) {
        continue;
      }

      // not all attributes match
      if (!matchingClass.equals(jClass)) {
        numChanges++;
      }
    }

    return numChanges;
  }
}
