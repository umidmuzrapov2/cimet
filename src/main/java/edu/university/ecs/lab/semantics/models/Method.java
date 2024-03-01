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
  private String methodId;
  private String classId;
  private int line;
  private List<Parameter> parameterList;
  private String mapping;
  private String mappingPath;
  private List<Annotation> annotations;

  public Method() {
    this.parameterList = new ArrayList<>();
  }

  public void addArgument(Parameter parameter) {
    parameterList.add(parameter);
  }

  public void setIds() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.getPackageName());
    sb.append(".");
    sb.append(this.getClassName());
    sb.append(".");
    sb.append(this.getMethodName());
    this.methodId = sb.toString();
    sb = new StringBuilder();
    sb.append(this.getPackageName());
    sb.append(".");
    sb.append(this.getClassName());
    this.classId = sb.toString();
  }

  @Override
  public String toString() {
    return " [L " + this.getLine() + "] " + this.getMethodId();
  }
}
