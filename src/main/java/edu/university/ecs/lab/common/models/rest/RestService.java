package edu.university.ecs.lab.common.models.rest;

import edu.university.ecs.lab.common.models.JavaMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RestService {
  private String className;
  private String sourceFile;
  private List<JavaMethod> methods = new ArrayList<>();
  private List<String> dtos = new ArrayList<>();

  public void addMethod(JavaMethod method) {
    methods.add(method);
  }
}
