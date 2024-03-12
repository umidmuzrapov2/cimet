package edu.university.ecs.lab.semantics.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** An object representing a method declaration in code */
@Data
public class Method {
  private Id id;
  private String protection;
  private String returnType;
  private String methodName;
  private String className;
  private String packageName;
  private int line;
  private List<Parameter> parameterList;
  private String mapping;
  private String mappingPath;
  private List<Annotation> annotations;
  private String apiEndpoint;

  public Method() {
    this.parameterList = new ArrayList<>();
  }

  public void addArgument(Parameter parameter) {
    parameterList.add(parameter);
  }
}
