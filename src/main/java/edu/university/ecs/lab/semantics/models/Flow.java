package edu.university.ecs.lab.semantics.models;

import lombok.Data;

import java.util.List;

@Data
public class Flow {
  private Class controller;
  private Method controllerMethod;
  private MethodCall serviceMethodCall;
  private Field controllerServiceField;
  private Class service;
  private Method serviceMethod;
  private MethodCall repositoryMethodCall;
  private Field serviceRepositoryField;
  private Class repository;
  private List<RestCall> restCalls;
  private Method repositoryMethod;

  public Flow(Class controller, Method controllerMethod) {
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

  //    @Override
  //    public String toString() {
  //        StringBuilder sb = new StringBuilder();
  //        sb.append(controller.getClassName());
  //        sb.append(" -> ");
  //        sb.append(controllerMethod.getMethodName());
  //        return sb.toString();
  //    }
}
