package edu.university.ecs.lab.semantics.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.university.ecs.lab.semantics.entity.*;
import edu.university.ecs.lab.semantics.entity.graph.*;
import edu.university.ecs.lab.semantics.entity.inconsistencies.MsInconsistencies;
import edu.university.ecs.lab.semantics.util.entitysimilarity.Entity;

public class MsCache {
    public static List<MsClass> msClassList;
    public static List<MsMethod> msMethodList;
    public static List<MsMethodCall> msMethodCallList;
    public static List<MsRestCall> msRestCallList;
    public static List<MsField> msFieldList;
    public static MsInconsistencies msInconsistencies;
    public static List<String> modules;
    public static List<MsFlowEntity> msFlows;
    
    //ContextEntities Mapping
    public static Map<String, Entity> mappedEntities;

    public static void init(){
        modules = new ArrayList<>();
        msClassList = new ArrayList<>();
        msMethodList = new ArrayList<>();
        msMethodCallList = new ArrayList<>();
        msInconsistencies = new MsInconsistencies();
        msRestCallList = new ArrayList<>();
        msFieldList = new ArrayList<>();
        msInconsistencies = new MsInconsistencies();
    }

    public static void addMsClass(MsClass msClass) {
        msClassList.add(msClass);
    }

    public static void addMsMethod(MsMethod msMethod) {
        msMethodList.add(msMethod);
    }

    public static void addMsMethodCall(MsMethodCall msMethodCall) {
        msMethodCallList.add(msMethodCall);
    }

    public static void addMsField(MsField msField) {
        msFieldList.add(msField);
    }

    public static void addMsRestMethodCall(MsRestCall msRestCall){
        msRestCallList.add(msRestCall);
    }

    public static void addMsFlow(MsFlowEntity msFlow) {
        msFlows.add(msFlow);
    }

    public static void print(){
//        System.out.printf("");
        System.out.println();
    }
}
