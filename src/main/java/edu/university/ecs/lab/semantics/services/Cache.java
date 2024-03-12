package edu.university.ecs.lab.semantics.services;

import java.util.*;

import edu.university.ecs.lab.semantics.models.*;
import lombok.Getter;
import lombok.Setter;

/**
 * This class serves as a cache that is regularly referenced throughout the program to increase
 * efficiency.
 */

@Getter
@Setter
class Cache {
  // Init all lists by default to empty. Cannot be reinitialized
  private List<JClass> classList;
  private List<Method> methodList;
  private List<MethodCall> methodCallList;
  private List<RestCall> restCallList;
  private List<Field> fieldList;
  private List<Flow> flowList;
  private List<CodeClone> codeCloneList;

    public Cache() {
        this.classList = new ArrayList<>();
        this.methodList = new ArrayList<>();
        this.methodCallList = new ArrayList<>();
        this.restCallList = new ArrayList<>();
        this.fieldList = new ArrayList<>();
        this.flowList = new ArrayList<>();
        this.codeCloneList = new ArrayList<>();
    }

  //    public final static List<FlowEntity> msFlows;

  //    public final static Map<String, Set<String>> msDependents;
  //
  //    public final static List<MsDependencyEntity> msExtendedDependents;

  // ContextEntities Mapping
  // public static Map<String, Entity> mappedEntities;

  // Cannot be invoked, singleton class

}
