package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Flow {
  private MsModel model;
  private JClass controller;
  private Method controllerMethod;
  private MethodCall serviceMethodCall;
  private Field controllerServiceField;
  private JClass service;
  private Method serviceMethod;
  private MethodCall repositoryMethodCall;
  private Field serviceRepositoryField;
  private JClass repository;
  private Method repositoryMethod;

}
