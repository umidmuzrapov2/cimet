package edu.university.ecs.lab.semantics.entity;

import lombok.Data;

import java.util.List;

import edu.university.ecs.lab.semantics.entity.graph.MsClass;
import edu.university.ecs.lab.semantics.entity.graph.MsField;
import edu.university.ecs.lab.semantics.entity.graph.MsMethod;
import edu.university.ecs.lab.semantics.entity.graph.MsRestCall;

@Data
public class MsFlow {
    private String module;
    // controller
    private MsClass controller;
    private MsMethod controllerMethod;
    private List<MsField> controllerServiceFields;
    // service
    private List<MsClass> services;
    private List<MsMethod> serviceMethods;
    private List<MsRestCall> restCall;
    // repository
    private List<MsClass> repositories;
    private List<MsMethod> repositoryMethods;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(controller.getClassName());
        sb.append(" -> ");
        sb.append(controllerMethod.getMethodName());
        return sb.toString();
    }
}
