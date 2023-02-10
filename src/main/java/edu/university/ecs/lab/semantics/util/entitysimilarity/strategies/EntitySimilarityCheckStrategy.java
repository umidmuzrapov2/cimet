package edu.university.ecs.lab.semantics.util.entitysimilarity.strategies;

import java.util.List;

import edu.university.ecs.lab.semantics.entity.graph.MsArgument;

public interface EntitySimilarityCheckStrategy {
	
	public double calculateSimilarity(String packageName1, String packageName2, String variableName1, String variableName2);
	public double calculateArgumentsSimilarity(List<MsArgument> aArguments, List<MsArgument> bArguments);

}
