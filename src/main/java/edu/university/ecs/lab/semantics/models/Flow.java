package edu.university.ecs.lab.semantics.models;

import lombok.Data;

import java.util.List;
import edu.university.ecs.lab.semantics.models.*;
import lombok.EqualsAndHashCode;

@Data
public class Flow {
    private JClass controller;
    private Method controllerMethod;
    private MethodCall serviceMethodCall;
    private Field controllerServiceField;
    private JClass service;
    private Method serviceMethod;
    private MethodCall repositoryMethodCall;
    private Field serviceRepositoryField;
    private JClass repository;
    private List<RestCall> restCalls;
    private Method repositoryMethod;


    public Flow(JClass controller, Method controllerMethod) {
        this.controller = controller;
        this.controllerMethod = controllerMethod;
    }

    public Flow(Method n) {
        this.controllerMethod = n;
    }

  public String getPackageName() {
    if (controller != null) {
      return controller.getPackageName().split("\\.")[0];
    } else if (controller != null) {
      return service.getPackageName().split("\\.")[0];
    } else if (repository != null) {
      return repository.getPackageName().split("\\.")[0];
    }
    return "";
  }
}

