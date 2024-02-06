package edu.university.ecs.lab.semantics.util;

import java.util.ArrayList;
import java.util.List;

import edu.university.ecs.lab.semantics.entity.*;
import edu.university.ecs.lab.semantics.entity.graph.*;

// import edu.university.ecs.lab.semantics.util.entitysimilarity.Entity;

/**
 * This class serves as a cache that is regularly referenced throughout the program to increase
 * efficiency.
 */
public class MsCache {
  public static List<MsClass> msClassList;
  public static List<MsMethod> msMethodList;
  public static List<MsMethodCall> msMethodCallList;
  public static List<MsRestCall> msRestCallList;
  public static List<MsField> msFieldList;
  public static List<String> modules;
  public static List<MsFlowEntity> msFlows;

  // ContextEntities Mapping
  // public static Map<String, Entity> mappedEntities;

  /** Initialize all cache to empty lists */
  public static void init() {
    modules = new ArrayList<>();
    msClassList = new ArrayList<>();
    msMethodList = new ArrayList<>();
    msMethodCallList = new ArrayList<>();
    msRestCallList = new ArrayList<>();
    msFieldList = new ArrayList<>();
  }

  /**
   * Adds msClass to msClassList
   *
   * @param msClass the MsClass to add
   */
  public static void addMsClass(MsClass msClass) {
    msClassList.add(msClass);
  }

  /**
   * Adds msMethod to msMethodList
   *
   * @param msMethod the MsMethod to add
   */
  public static void addMsMethod(MsMethod msMethod) {
    msMethodList.add(msMethod);
  }

  /**
   * Adds MsMethodCall to msMethodCallList
   *
   * @param msMethodCall the MsMethodCall to add
   */
  public static void addMsMethodCall(MsMethodCall msMethodCall) {
    msMethodCallList.add(msMethodCall);
  }

  /**
   * Adds msField to msFieldList
   *
   * @param msField the MsField to add
   */
  public static void addMsField(MsField msField) {
    msFieldList.add(msField);
  }

  /**
   * Adds msRestCall to msRestCallList
   *
   * @param msRestCall the MsRestCall to add
   */
  public static void addMsRestMethodCall(MsRestCall msRestCall) {
    msRestCallList.add(msRestCall);
  }

  /**
   * Adds addMsFlow to msFlows
   *
   * @param msFlow the addMsFlow to add
   */
  public static void addMsFlow(MsFlowEntity msFlow) {
    msFlows.add(msFlow);
  }
}
