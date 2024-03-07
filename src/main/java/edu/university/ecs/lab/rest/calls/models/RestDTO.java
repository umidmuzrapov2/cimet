package edu.university.ecs.lab.rest.calls.models;

import edu.university.ecs.lab.common.models.JavaMethod;
import edu.university.ecs.lab.common.models.JavaVariable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestDTO {
  private String className;
  private String sourceFile;

  private List<JavaVariable> dtoVariables = new ArrayList<>();
  private List<JavaMethod> dtoMethods = new ArrayList<>();

  public void addVariable(JavaVariable variable) {
    dtoVariables.add(variable);
  }

  public void addMethod(JavaMethod method) {
    dtoMethods.add(method);
  }
}
