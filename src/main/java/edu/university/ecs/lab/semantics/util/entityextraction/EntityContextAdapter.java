package edu.university.ecs.lab.semantics.util.entityextraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.baylor.ecs.cloudhubs.jparser.component.Component;
import edu.baylor.ecs.cloudhubs.jparser.component.context.AnalysisContext;
import edu.baylor.ecs.cloudhubs.jparser.component.impl.AnnotationComponent;
import edu.baylor.ecs.cloudhubs.jparser.component.impl.ClassComponent;
import edu.baylor.ecs.cloudhubs.jparser.component.impl.FieldComponent;
import edu.baylor.ecs.cloudhubs.jparser.component.impl.ModuleComponent;
import edu.university.ecs.lab.semantics.util.entitysimilarity.Entity;


public class EntityContextAdapter {

	public static Map<String, Entity> getMappedEntityContext(String path){
		List<String> msFullPaths = new ArrayList<>();
		List<String> allPaths = Arrays.asList(DirectoryUtils.getMsFullPaths(path));

        // filter paths to only those projects that contain Java
        List<String> validPaths = allPaths.stream().filter(DirectoryUtils::hasJava).collect(Collectors.toList());
        msFullPaths.addAll(validPaths);

		
        JParserUtils jParserUtils = JParserUtils.getInstance();
        AnalysisContext analysisContext = jParserUtils.createAnalysisContextFromMultipleDirectories(msFullPaths);
        Map<String, Entity> mappedEntities = EntityContextAdapter.getSystemContext(analysisContext, msFullPaths.toArray(new String[msFullPaths.size()]));
        return mappedEntities;
    }
	
    /**
     * Retrieves entity model context from a system based on JParser representation
     * @param context JParser AnalysisContext
     * @return SystemContext
     */
    public static Map<String, Entity> getSystemContext(AnalysisContext context, String[] msPaths) {
    	
    	Map<String, Entity> mappedEntities = new HashMap<>();
    	
//        Set<Module> modules = new HashSet<>();
        HashMap<String, Set<ClassComponent>> clusters = clusterClassComponents(context.getModules(), msPaths);
        
        HashMap<String, Integer> tempNumberCollection = new HashMap();
//        Set<String> temoDatabaseEntities = new HashSet();
//        Set<String> temoDataEntities = new HashSet();
        
        int serviceNumber = 0;
        
        for (Map.Entry<String, Set<ClassComponent>> entry : clusters.entrySet()) {
        	tempNumberCollection.put("@Entity", 0);
            tempNumberCollection.put("@Document", 0);
            tempNumberCollection.put("@Data", 0);
            
            Set<String> tempDatabaseEntities = new HashSet();
            Set<String> tempDataEntities = new HashSet();
            
            serviceNumber++;
            
//            Module module_n = new Module();
//            Set<Entity> entities = new HashSet<>();
            for (ClassComponent clazz : entry.getValue()) {
                List<Component> classAnnotations = clazz.getAnnotations();
                if (classAnnotations != null){
                    for (Component cmp: classAnnotations) {
                        AnnotationComponent ac = (AnnotationComponent) cmp;
                        if (ac.getAsString().equals("@Entity") || ac.getAsString().equals("@Document") || ac.getAsString().equals("@Data")){
                        	
                        	// To get rid of the class with multiple data annotations (prioritize the Entity over the Data)
                        	if(ac.getAsString().equals("@Entity") || ac.getAsString().equals("@Document") ) {
                        		if (tempDataEntities.contains(clazz.getClassName())) {
                        			tempDataEntities.remove(clazz.getClassName());
                        			tempNumberCollection.put("@Data", tempNumberCollection.get("@Data") - 1);
                        		}
                        		tempNumberCollection.put(ac.getAsString(), tempNumberCollection.get(ac.getAsString()) + 1);
                        		tempDatabaseEntities.add(clazz.getClassName());
                        		
                        	} else {
                        		if (!tempDatabaseEntities.contains(clazz.getClassName())) {
                        			tempDataEntities.add(clazz.getClassName());
                        			tempNumberCollection.put(ac.getAsString(), tempNumberCollection.get(ac.getAsString()) + 1);
                        		}
                        	}
                        	
                        	
                        	
                            Set<EntityField> fields = new HashSet<>();
                            for (FieldComponent field : clazz.getFieldComponents()) {
//                                Field field_n = new Field();
//                                field_n.setName(new Name(field.getFieldName()));
//                                if (isCollection(field.getType())){
//                                    String s = field.getType();
//                                    String entityRef = s.substring(s.indexOf("<") + 1, s.indexOf(">"));
//                                    field_n.setType(entityRef);
//                                    field_n.setCollection(true);
//                                } else {
//                                    field_n.setType(field.getType());
//                                    field_n.setCollection(false);
//                                }
//                            	System.out.println("-----------Context----------");
//                            	System.out.println(clazz.getPackageName() + "...>" + clazz.getClassName() + " ---> " + ac.getAsString());
//                            	System.out.println("---------------------");
                            	
                            	
                                Set<Annotation> annotations = new HashSet<>();
                                for (Component annotation : field.getAnnotations()) {
                                    Annotation ann = new Annotation();
                                    ann.setStringValue(annotation.asAnnotationComponent().getAnnotationValue());
                                    ann.setName(annotation.asAnnotationComponent().getAsString());
                                    annotations.add(ann);
                                }
                                List<EntityField> subFields = new ArrayList();
                            	subFields = getSubFields(field.getFieldName(), annotations, field.getType(), subFields);
//                                field_n.setAnnotations(annotations);
                            	for (EntityField field_n : subFields) {
                            		for (Annotation a: field_n.getAnnotations()){
                                        if (a.getName().equals("@ManyToOne") || a.getName().equals("@OneToMany" )
                                                || a.getName().equals("@OneToOne") || a.getName().equals("@ManyToMany")) {
                                            //field_n.setEntityReference();
                                            field_n.setReference(true);
                                            field_n.setEntityRefName(field_n.getType());
                                        }
                                    }
                                    fields.add(field_n);
                            	}
                            }
                            Entity entity = new Entity();
                            entity.setEntityName(clazz.getClassName());
                            entity.setFields(fields);
//                            System.err.println(clazz.getPackageName());
//                            System.err.println("food.entity".split("\\.").length);
                            String entityNameKey = clazz.getPackageName().split("\\.")[0] + "." +  clazz.getClassName().toLowerCase();
//                            System.err.println(clazz.getPackageName());
                            mappedEntities.put(entityNameKey, entity); /// TODO: Change the key to the whole package name or concatenate with the service name
//                            mappedEntities.put(clazz.getPackageName() + "." + clazz.getClassName(), entity);
//                            entities.add(entity);
                        }
                    }
                }
            }
            
//            module_n.setName(new Name(entry.getKey()));
//            module_n.setEntities(entities);
//            modules.add(module_n);
            
            
//            System.out.println("-----------Context----------");
//        	System.out.println(serviceNumber +" "+ entry.getKey() + "--->" + "   #Documents=" + tempNumberCollection.get("@Document") + "   #Entity = " + tempNumberCollection.get("@Entity") + "   #Data=" + tempNumberCollection.get("@Data"));
//        	System.out.println("---------------------");
        }
        return mappedEntities;

//        return new SystemContext(context.getRootPath(), modules);
    }



    /**
     * Cluster classes by their presence in respective ms modules
     * @param moduleComponents
     * @param msPaths
     * @return
     */
    public static HashMap<String, Set<ClassComponent>> clusterClassComponents(List<ModuleComponent> moduleComponents,
                                                                       String[] msPaths){
        HashMap<String, Set<ClassComponent>> clusters = new HashMap<>();
        for (String path: msPaths){
            clusters.put(path, new HashSet<ClassComponent>());
        }
        for (ModuleComponent mc: moduleComponents) {
            String mcPath = mc.getPath();
            String msPath = Arrays.stream(msPaths).filter(mcPath::contains).findFirst().orElse(null);
            if (msPath != null){
                Set<ClassComponent> valueSet = clusters.get(msPath);
                valueSet.addAll(mc.getClasses());
                clusters.put(msPath, valueSet);
            }
        }
        return clusters;
    }

    
    public static List<EntityField> getSubFields(String name, Set<Annotation> annotations, String type, List<EntityField> fields) {
    	EntityField field_n = new EntityField();
//    	field_n.setName(new Name(name));
    	field_n.setName(name);
    	field_n.setAnnotations(annotations);
        if (type.contains("Map") || type.contains("HashMap")) {
        	field_n.setType(type);
        	field_n.setCollection(true);
        	String valuePart = type.substring(type.indexOf(",") + 1, type.length() - 1);
        	fields.add(field_n);
            return getSubFields(name, annotations, valuePart, fields);
        } else if (isCollection(type)){
//            String entityRef = type.substring(type.indexOf("<") + 1, type.indexOf(">"));
        	String entityRef = type.substring(type.indexOf("<") + 1, type.length() - 1);
            field_n.setType(entityRef);
            field_n.setCollection(true);
            fields.add(field_n);
            return getSubFields(name, annotations, entityRef, fields);
        } else if (!type.isEmpty()){
            field_n.setType(type);
            field_n.setCollection(false);
            fields.add(field_n);
            return fields;
        } else {
        	return fields;
        }
    }

    public static boolean isCollection(String type){
        if (type.contains("Set") ){
            return true;
        } else if (type.contains("Collection")){
            return true;
        } else return type.contains("List");
    }
}