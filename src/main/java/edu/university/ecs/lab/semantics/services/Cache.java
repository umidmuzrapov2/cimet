package edu.university.ecs.lab.semantics.services;

import java.util.*;

import edu.university.ecs.lab.semantics.models.*;
import lombok.Getter;

/**
 * This class serves as a cache that is regularly referenced throughout the program to increase
 * efficiency.
 */

@Getter
public class Cache {
    // Init all lists by default to empty. Cannot be reinitialized
    private final List<JClass> classList;
    private final List<Method> methodList;
    private final List<MethodCall> methodCallList;
    private final List<RestCall> restCallList;
    private final List<Field> fieldList;
    private final List<Flow> flowList;

    public Cache() {
        this.classList = new ArrayList<>();
        this.methodList = new ArrayList<>();
        this.methodCallList = new ArrayList<>();
        this.restCallList = new ArrayList<>();
        this.fieldList = new ArrayList<>();
        this.flowList = new ArrayList<>();
    }

    //    public final static List<FlowEntity> msFlows;

//    public final static Map<String, Set<String>> msDependents;
//
//    public final static List<MsDependencyEntity> msExtendedDependents;

    // ContextEntities Mapping
    // public static Map<String, Entity> mappedEntities;

    // Cannot be invoked, singleton class

}
