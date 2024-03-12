package edu.university.ecs.lab.semantics.models;

import java.util.List;

public interface EntitySimilarityCheckStrategy {

  public double calculateSimilarity(
      String packageName1, String packageName2, String variableName1, String variableName2);

  public double calculateArgumentsSimilarity(
      List<Parameter> aArguments, List<Parameter> bArguments);
}
