package edu.university.ecs.lab.semantics.util.entitysimilarity.strategies;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.university.ecs.lab.semantics.entity.graph.MsArgument;
import edu.university.ecs.lab.semantics.util.MsCache;
import edu.university.ecs.lab.semantics.util.entityextraction.EntityField;
import edu.university.ecs.lab.semantics.util.entitysimilarity.Entity;
import edu.university.ecs.lab.semantics.util.entitysimilarity.SimilarityUtils;
import edu.university.ecs.lab.semantics.util.entitysimilarity.SimilarityUtilsImpl;

public class EntitySematicSimilarityCheckStrategy implements EntitySimilarityCheckStrategy {
	
	private boolean useWuPalmer;
	private SimilarityUtils similarityUtils;

	public EntitySematicSimilarityCheckStrategy(boolean useWuPalmer) {
		this.useWuPalmer = useWuPalmer;
		this.similarityUtils = new SimilarityUtilsImpl();
	}

	@Override
	public double calculateSimilarity(String packageName1, String packageName2, String variableName1, String variableName2) {
		double similarityValue = 0;
    	
		variableName1 = variableName1.toLowerCase();
		variableName2 = variableName2.toLowerCase();
		
		String variableFullName1 = packageName1 + "." + variableName1;
		String variableFullName2 = packageName2 + "." + variableName2;
		
		if (MsCache.mappedEntities.containsKey(variableFullName1) && MsCache.mappedEntities.containsKey(variableFullName2)) {
    		Entity entity1 = MsCache.mappedEntities.get(variableFullName1);
    		Entity entity2 = MsCache.mappedEntities.get(variableFullName2);
    		similarityValue = this.similarityUtils.calculateSimilarity(entity1, entity2, true, this.useWuPalmer);
    	} if (variableName1.equals(variableName2)) {
    		similarityValue = 1.0;
    	} else {
    		similarityValue = this.similarityUtils.nameSimilarity(variableName1, variableName2, this.useWuPalmer);
    	}
    	return similarityValue;
	}
	
	@Override
	public double calculateArgumentsSimilarity(List<MsArgument> aArguments, List<MsArgument> bArguments) {
		
		if (aArguments.isEmpty() && bArguments.isEmpty()) {
			return 1.0;
		}
		
		Entity entity1 = new Entity();
		entity1.setEntityName("FakeEntity");
		Set<EntityField> aFields = convertToFields(aArguments);
		entity1.setFields(aFields);
		
		Entity entity2 = new Entity();
		entity2.setEntityName("FakeEntity");
		Set<EntityField> bFields = convertToFields(bArguments);
		entity2.setFields(bFields);
		
		double similarityValue = this.similarityUtils.calculateSimilarity(entity1, entity2, false, this.useWuPalmer);
		return similarityValue;
		
	}
	
	private Set<EntityField> convertToFields(List<MsArgument> aArguments) {
		Set<EntityField> fields = new HashSet<>();
		for (MsArgument arg : aArguments) {
			EntityField field = new EntityField();
			field.setName(arg.getReturnType());
			field.setType(arg.getReturnType());
			fields.add(field);
		}
		return fields;
	}
}
