package edu.university.ecs.lab.common.models.rest;

import edu.university.ecs.lab.common.models.JavaClass;

public class RestEntity extends JavaClass {
  public RestEntity(JavaClass javaClass) {
    this.setClassName(javaClass.getClassName());
    this.setSourceFile(javaClass.getSourceFile());
    this.setVariables(javaClass.getVariables());
    this.setMethods(javaClass.getMethods());
  }
}