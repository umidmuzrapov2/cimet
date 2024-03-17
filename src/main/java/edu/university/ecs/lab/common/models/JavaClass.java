package edu.university.ecs.lab.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaClass {
  private String className;
  private String classPath;

  private List<JavaVariable> variables = new ArrayList<>();
  private List<JavaMethod> methods = new ArrayList<>();

  public void addVariable(JavaVariable variable) {
    variables.add(variable);
  }

  public void addMethod(JavaMethod method) {
    methods.add(method);
  }
}
